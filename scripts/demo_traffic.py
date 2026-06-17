#!/usr/bin/env python3
"""
Simulador de trafico demo para eScrims.

Usa la API que consume el frontend para:
- loguear usuarios demo
- crear busquedas favoritas
- crear scrims compatibles que disparan alertas
- configurar notificaciones para que los cambios de estado publiquen en RabbitMQ
- postular/confirmar/iniciar/finalizar scrims
- procesar recordatorios para generar mas mensajes

No requiere dependencias externas.
"""

from __future__ import annotations

import argparse
import json
import random
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timedelta
from typing import Any


GAMES = {
    "Valorant": ["DUELIST", "CONTROLLER", "SENTINEL", "INITIATOR", "SUPPORT"],
    "LoL": ["TOP", "JUNGLA", "MID", "ADC", "SUPPORT"],
    "CS2": ["IGL", "ENTRY", "AWP", "SUPPORT", "LURKER"],
}
GAME_FORMATS = {
    "Valorant": ["1v1", "2v2", "3v3", "5v5"],
    "LoL": ["1v1", "2v2", "5v5"],
    "CS2": ["1v1", "2v2", "5v5"],
}
SCRIM_TEMPLATES = [
    ("Valorant", "1v1"),
    ("LoL", "1v1"),
    ("CS2", "1v1"),
    ("Valorant", "2v2"),
    ("LoL", "2v2"),
    ("CS2", "2v2"),
    ("Valorant", "3v3"),
    ("Valorant", "5v5"),
    ("LoL", "5v5"),
    ("CS2", "5v5"),
]
REGIONS = ["SA", "BR", "NA", "EU"]
MODALITIES = ["CASUAL", "PRACTICA", "RANKED_LIKE"]
CORE_USERS = ["admin", "mod", "alpha", "bravo", "charlie", "delta", "echo", "foxtrot"]
DEFAULT_USERS = ",".join(CORE_USERS + [f"demo{i:03d}" for i in range(1, 61)])


class ApiError(RuntimeError):
    def __init__(self, method: str, path: str, status: int, payload: Any) -> None:
        self.method = method
        self.path = path
        self.status = status
        self.payload = payload
        super().__init__(f"{method} {path} -> {status}: {payload}")


class ApiClient:
    def __init__(
        self,
        base_url: str,
        timeout: float,
        client_ips: list[str],
        rate_limit_retries: int,
        rate_limit_wait: float,
    ) -> None:
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self.client_ips = client_ips
        self.rate_limit_retries = rate_limit_retries
        self.rate_limit_wait = rate_limit_wait
        self.request_count = 0

    def request(
        self,
        method: str,
        path: str,
        body: dict[str, Any] | None = None,
        token: str | None = None,
    ) -> Any:
        data = None
        headers = {"Content-Type": "application/json"}
        if self.client_ips:
            headers["X-Forwarded-For"] = self.client_ips[self.request_count % len(self.client_ips)]
            self.request_count += 1
        if token:
            headers["Authorization"] = f"Bearer {token}"
        if body is not None:
            data = json.dumps(body).encode("utf-8")

        for attempt in range(self.rate_limit_retries + 1):
            req = urllib.request.Request(
                f"{self.base_url}{path}",
                data=data,
                headers=headers,
                method=method,
            )
            try:
                with urllib.request.urlopen(req, timeout=self.timeout) as response:
                    payload = response.read().decode("utf-8")
                    return json.loads(payload) if payload else None
            except urllib.error.HTTPError as error:
                payload = error.read().decode("utf-8")
                try:
                    parsed = json.loads(payload)
                except json.JSONDecodeError:
                    parsed = {"error": payload}

                if error.code == 429 and attempt < self.rate_limit_retries:
                    wait = self.rate_limit_wait * (attempt + 1)
                    print(f"[demo] rate limit en {method} {path}; espero {wait:.1f}s y reintento")
                    time.sleep(wait)
                    continue

                raise ApiError(method, path, error.code, parsed) from error
            except urllib.error.URLError as error:
                raise RuntimeError(f"No se pudo conectar con {self.base_url}: {error}") from error

        raise RuntimeError(f"{method} {path} no pudo completarse")


def iso_local(dt: datetime) -> str:
    return dt.replace(microsecond=0).isoformat()


def cupos_por_formato(formato: str) -> int:
    left, _right = formato.lower().split("v", 1)
    return int(left) * 2


def formatos_para(game: str) -> list[str]:
    return GAME_FORMATS.get(game, ["1v1", "2v2", "3v3", "4v4", "5v5"])


def elegir_formato(game: str, cycle: int) -> str:
    formatos = formatos_para(game)
    return formatos[(cycle - 1) % len(formatos)]


def elegir_template_scrim(index: int) -> tuple[str, str]:
    return SCRIM_TEMPLATES[(index - 1) % len(SCRIM_TEMPLATES)]


def login(client: ApiClient, username: str, password: str) -> tuple[str, dict[str, Any]]:
    response = client.request(
        "POST",
        "/auth/login",
        {"username": username, "password": password},
    )
    return response["token"], response["usuario"]


def ensure_favorite(client: ApiClient, token: str, game: str, region: str, formato: str) -> None:
    client.request(
        "POST",
        "/busquedas-favoritas",
        {
            "juego": game,
            "formato": formato,
            "region": region,
            "rangoMin": 1000,
            "rangoMax": 2200,
            "latenciaMax": 120,
        },
        token,
    )


def configure_notifications(client: ApiClient, usernames: list[str]) -> None:
    batch = usernames[: min(8, len(usernames))]
    client.request("POST", "/notificaciones/email", {"usernames": batch})
    client.request("POST", "/notificaciones/push", {"usernames": batch})
    client.request("POST", "/notificaciones/discord", {"usernames": batch})


def create_scrim(
    client: ApiClient,
    index: int,
    game: str,
    formato: str,
    region: str,
    fecha_hora: datetime,
) -> dict[str, Any]:
    return client.request(
        "POST",
        "/scrims",
        {
            "juego": game,
            "formato": formato,
            "region": region,
            "rangoMin": 1000,
            "rangoMax": 2200,
            "latenciaMax": 120,
            "fechaHora": iso_local(fecha_hora),
            "duracionMinutos": 30 + (index % 4) * 15,
            "cuposTotales": cupos_por_formato(formato),
            "modalidad": MODALITIES[index % len(MODALITIES)],
        },
    )


def listar_scrims(client: ApiClient) -> list[dict[str, Any]]:
    return client.request("GET", "/scrims")


def listar_scrims_abiertos(client: ApiClient) -> list[dict[str, Any]]:
    scrims = listar_scrims(client)
    return [
        scrim for scrim in scrims
        if scrim.get("estado") == "BUSCANDO" and scrim.get("cuposDisponibles", 0) > 0
    ]


def usernames_postulados(scrim: dict[str, Any]) -> set[str]:
    return {postulacion["username"] for postulacion in scrim.get("postulaciones", [])}


def usuarios_compatibles(scrim: dict[str, Any], users: list[dict[str, Any]]) -> list[dict[str, Any]]:
    juego = scrim["juego"]
    existentes = usernames_postulados(scrim)
    compatibles = []
    for user in users:
        rango = (user.get("rangosPorJuego") or {}).get(juego)
        if rango is None:
            continue
        if user["username"] in existentes:
            continue
        if not (1000 <= rango <= 2200):
            continue
        if user.get("latenciaPromedio", 999) > 120:
            continue
        compatibles.append(user)
    return compatibles


def elegir_scrim_abierto(
    client: ApiClient,
    users: list[dict[str, Any]],
) -> dict[str, Any] | None:
    scrims = listar_scrims(client)
    candidatos = [
        scrim for scrim in scrims
        if scrim.get("estado") == "BUSCANDO" and scrim.get("cuposDisponibles", 0) > 0
        if usuarios_compatibles(scrim, users)
    ]
    if candidatos:
        candidatos.sort(key=lambda scrim: (scrim["cuposDisponibles"], scrim["fechaHora"]))
        return candidatos[0]

    lobbies = [scrim for scrim in scrims if scrim.get("estado") == "LOBBY_ARMADO"]
    if lobbies:
        lobbies.sort(key=lambda scrim: scrim["fechaHora"])
        return lobbies[0]

    return None


def postular_usuarios(
    client: ApiClient,
    scrim: dict[str, Any],
    users: list[dict[str, Any]],
    max_postulaciones: int | None = None,
) -> list[str]:
    roles = GAMES[scrim["juego"]]
    cupos = scrim.get("cuposDisponibles", 0)
    cantidad = cupos if max_postulaciones is None else min(cupos, max_postulaciones)
    selected = usuarios_compatibles(scrim, users)[:cantidad]
    postulados = []
    for i, user in enumerate(selected):
        try:
            client.request(
                "POST",
                f"/scrims/{scrim['id']}/postulaciones",
                {"username": user["username"], "rol": roles[i % len(roles)]},
            )
            postulados.append(user["username"])
        except ApiError as error:
            if error.status not in (400, 409):
                raise
            print(f"[demo] no se pudo postular {user['username']} en {scrim['id']}: {error.payload}")
    return postulados


def confirmar_lobby(client: ApiClient, scrim: dict[str, Any]) -> dict[str, Any]:
    for postulacion in scrim.get("postulaciones", []):
        if postulacion.get("estado") != "ACEPTADA":
            continue
        client.request(
            "POST",
            f"/scrims/{scrim['id']}/confirmaciones",
            {"username": postulacion["username"]},
        )
    return client.request("GET", f"/scrims/{scrim['id']}")


def avanzar_si_corresponde(client: ApiClient, scrim: dict[str, Any], cycle: int) -> dict[str, Any]:
    if scrim["estado"] == "LOBBY_ARMADO" and cycle % 3 in (1, 2):
        scrim = confirmar_lobby(client, scrim)

    if scrim["estado"] == "CONFIRMADO" and cycle % 3 == 2:
        scrim = client.request("POST", f"/scrims/{scrim['id']}/iniciar")
        scrim = client.request("POST", f"/scrims/{scrim['id']}/finalizar")

    return scrim


def completar_scrim(
    client: ApiClient,
    scrim: dict[str, Any],
    users: list[dict[str, Any]],
    cycle: int,
    max_postulaciones: int | None = None,
) -> tuple[dict[str, Any], list[str]]:
    postulados = []
    if scrim["estado"] == "BUSCANDO" and scrim.get("cuposDisponibles", 0) > 0:
        postulados = postular_usuarios(client, scrim, users, max_postulaciones)
        scrim = client.request("GET", f"/scrims/{scrim['id']}")
    scrim = avanzar_si_corresponde(client, scrim, cycle)
    return scrim, postulados


def process_reminders(client: ApiClient, fecha_hora: datetime, horas_antes: int) -> None:
    client.request(
        "POST",
        "/scrims/recordatorios",
        {"ahora": iso_local(fecha_hora - timedelta(hours=horas_antes)), "horasAntes": horas_antes},
    )


def poll_ui_endpoints(client: ApiClient, token: str) -> tuple[int, int]:
    dashboard = client.request("GET", "/dashboard/me", token=token)
    alerts = client.request("GET", "/alertas", token=token)
    client.request("GET", "/scrims")
    return dashboard.get("alertasTotal", 0), len(alerts)


def run(args: argparse.Namespace) -> None:
    random.seed(args.seed)
    client_ips = []
    if args.client_ips > 0:
        client_ips = [f"10.77.{i // 250}.{i % 250 + 1}" for i in range(args.client_ips)]

    client = ApiClient(
        args.base_url,
        args.timeout,
        client_ips,
        args.rate_limit_retries,
        args.rate_limit_wait,
    )
    start_at = datetime.fromisoformat(args.start_at)

    usernames = [name.strip() for name in args.users.split(",") if name.strip()]
    sessions: list[tuple[str, dict[str, Any]]] = []
    for username in usernames:
        token, user = login(client, username, args.password)
        sessions.append((token, user))

    users = [user for _, user in sessions]
    print(f"[demo] usuarios logueados: {', '.join(user['username'] for user in users)}")

    configure_notifications(client, [user["username"] for user in users])
    print("[demo] notificaciones configuradas; los cambios de estado van a Rabbit si NOTIFICATIONS_QUEUE=rabbit")

    for i, (token, _user) in enumerate(sessions):
        game, formato = elegir_template_scrim(i + 1)
        ensure_favorite(client, token, game, REGIONS[i % len(REGIONS)], formato)
    print("[demo] busquedas favoritas creadas; los nuevos scrims compatibles generan alertas")

    created_count = 0
    for cycle in range(1, args.cycles + 1):
        region = REGIONS[cycle % len(REGIONS)]
        fecha_hora = start_at + timedelta(minutes=args.scrim_spacing_minutes * cycle)

        debe_crear = args.create_every > 0 and (cycle - 1) % args.create_every == 0
        scrim = None if debe_crear else elegir_scrim_abierto(client, users)
        accion = "rellenar"
        max_postulaciones = None
        if scrim is None:
            created_count += 1
            game, formato = elegir_template_scrim(created_count)
            scrim = create_scrim(client, cycle, game, formato, region, fecha_hora)
            accion = "crear"
            max_postulaciones = max(1, scrim["cuposTotales"] // 2)

        scrim, postulados = completar_scrim(client, scrim, users, cycle, max_postulaciones)

        if cycle % args.reminder_every == 0:
            process_reminders(client, fecha_hora, args.reminder_hours)

        token = sessions[cycle % len(sessions)][0]
        alertas_total, alertas_listadas = poll_ui_endpoints(client, token)
        print(
            f"[cycle {cycle:03d}] accion={accion} scrim={scrim['id']} "
            f"game={scrim['juego']} formato={scrim['formato']} region={scrim['region']} "
            f"cupos={scrim['cuposDisponibles']}/{scrim['cuposTotales']} estado={scrim['estado']} "
            f"postulados={len(postulados)} fecha={scrim['fechaHora']} "
            f"alertasDashboard={alertas_total} alertasListadas={alertas_listadas}"
        )

        if args.delay > 0:
            time.sleep(args.delay)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Simula trafico demo contra eScrims.")
    parser.add_argument(
        "--base-url",
        default="http://localhost:3100/api",
        help="URL base de la API. Usar http://localhost:3100/api para pasar por el frontend/Nginx.",
    )
    parser.add_argument("--cycles", type=int, default=30, help="Cantidad de ciclos de trafico.")
    parser.add_argument("--delay", type=float, default=0.5, help="Pausa entre ciclos en segundos.")
    parser.add_argument("--timeout", type=float, default=10.0, help="Timeout HTTP en segundos.")
    parser.add_argument(
        "--create-every",
        type=int,
        default=4,
        help="Cada cuantos ciclos crea un scrim nuevo. En los demas intenta completar abiertos.",
    )
    parser.add_argument(
        "--client-ips",
        type=int,
        default=50,
        help="Cantidad de IPs sinteticas para rotar X-Forwarded-For. Usar 0 para una sola IP real.",
    )
    parser.add_argument(
        "--rate-limit-retries",
        type=int,
        default=3,
        help="Reintentos automaticos cuando la API responde 429.",
    )
    parser.add_argument(
        "--rate-limit-wait",
        type=float,
        default=5.0,
        help="Segundos base de espera entre reintentos por rate limit.",
    )
    parser.add_argument("--password", default="12345678", help="Password de usuarios demo.")
    parser.add_argument("--seed", type=int, default=42, help="Seed para repetir una demo.")
    parser.add_argument(
        "--start-at",
        default="2030-01-01T20:00:00",
        help="Fecha/hora base para crear scrims. Debe ser futura para el backend.",
    )
    parser.add_argument(
        "--scrim-spacing-minutes",
        type=int,
        default=10,
        help="Separacion en minutos entre scrims creados.",
    )
    parser.add_argument(
        "--reminder-hours",
        type=int,
        default=3,
        help="Horas antes usadas al procesar recordatorios.",
    )
    parser.add_argument(
        "--reminder-every",
        type=int,
        default=5,
        help="Cada cuantos ciclos invoca /scrims/recordatorios.",
    )
    parser.add_argument(
        "--users",
        default=DEFAULT_USERS,
        help="Usuarios separados por coma para simular trafico.",
    )
    return parser.parse_args()


if __name__ == "__main__":
    try:
        run(parse_args())
    except KeyboardInterrupt:
        print("\n[demo] detenido por usuario")
    except Exception as exc:
        print(f"[demo] error: {exc}", file=sys.stderr)
        sys.exit(1)
