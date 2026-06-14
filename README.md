# eScrims - Sistema de Scrims Competitivos

TPO Final - Analisis y Diseno Orientado a Objetos  
Universidad Argentina de la Empresa (UADE)

## Descripcion

eScrims permite organizar scrims de eSports y gestionar su ciclo de vida:
creacion, postulaciones, confirmaciones, inicio, finalizacion, estadisticas y
notificaciones por canal.

La implementacion prioriza una arquitectura defendible para la catedra:

```text
Main / API REST -> Facade -> Controller -> Service -> Domain -> Infra
```

## Patrones Aplicados

| Patron | Paquete | Proposito |
| --- | --- | --- |
| Facade | `escrims.facade` | Expone una API simple para Main/tests y oculta el subsistema interno. |
| State | `escrims.domain.state` | Ciclo de vida del scrim: BUSCANDO -> LOBBY_ARMADO -> CONFIRMADO -> EN_JUEGO -> FINALIZADO/CANCELADO. |
| Strategy | `escrims.domain.matchmaking` | Algoritmos intercambiables de matchmaking: MMR, latencia, historial y composicion. |
| Strategy | `escrims.infra.notification` | Envio intercambiable de notificaciones por Email, Push o Discord. |
| Observer | `escrims.infra.events` | Publicacion de eventos de dominio y suscriptores desacoplados. |
| Abstract Factory | `escrims.infra.notification` | Familias DEV/PROD de notificadores. |
| Adapter | `escrims.infra.notification` | Adapta integraciones externas simuladas a `NotificadorStrategy`. |
| Builder | `escrims.domain.state.ScrimBuilder` | Construccion incremental de `ScrimContext` con validacion de invariantes. |

## Regla Sin Enums

Los enums fueron eliminados del codigo fuente.

- `Rol` ahora es un value object de dominio en `escrims.domain.model.Rol`.
- `EstadoPostulacion` y `EstadoNotificacion` son interfaces con implementaciones concretas simples.
- Los eventos se distinguen por polimorfismo de clases concretas y `getTipo(): String`, sin `TipoEvento`.
- La configuracion de canales no usa `CanalNotificacion`; la fachada expone metodos explicitos como `configurarNotificacionesEmail()` y `configurarNotificacionesPush()`.

## Estructura

```text
src/main/java/escrims/
├── ApiApplication.java
├── Main.java
├── controller/
│   ├── ScrimController.java
│   └── api/
│       ├── ApiConfig.java
│       ├── ApiDtos.java
│       ├── ApiExceptionHandler.java
│       ├── OpenApiConfig.java
│       ├── ScrimRestController.java
│       ├── UsuarioApiRepository.java
│       └── UsuarioRestController.java
├── service/
│   ├── ScrimService.java
│   └── ScrimSchedulerService.java
├── facade/
│   └── ScrimFacade.java
├── domain/
│   ├── model/
│   │   ├── Usuario.java
│   │   ├── Rol.java
│   │   ├── Equipo.java
│   │   ├── Postulacion.java
│   │   ├── EstadoPostulacion.java
│   │   ├── Confirmacion.java
│   │   ├── Estadistica.java
│   │   ├── Notificacion.java
│   │   └── EstadoNotificacion.java
│   ├── state/
│   │   ├── ScrimContext.java
│   │   ├── ScrimBuilder.java
│   │   ├── ScrimState.java
│   │   ├── BuscandoState.java
│   │   ├── LobbyArmadoState.java
│   │   ├── ConfirmadoState.java
│   │   ├── EnJuegoState.java
│   │   ├── FinalizadoState.java
│   │   └── CanceladoState.java
│   └── matchmaking/
│       ├── MatchmakingStrategy.java
│       ├── ByMMRStrategy.java
│       ├── ByLatencyStrategy.java
│       ├── ByHistoryStrategy.java
│       └── CompositeMatchmakingStrategy.java
└── infra/
    ├── events/
    │   ├── DomainEvent.java
    │   ├── DomainEventBus.java
    │   ├── Subscriber.java
    │   ├── NotificationSubscriber.java
    │   ├── ScrimCreadoEvent.java
    │   └── ScrimStateChangedEvent.java
    └── notification/
        ├── NotificadorStrategy.java
        ├── EmailNotificador.java
        ├── PushNotificador.java
        ├── DiscordNotificador.java
        ├── SendGridEmailAdapter.java
        ├── FirebasePushAdapter.java
        ├── DiscordWebhookAdapter.java
        ├── NotificadorFactory.java
        ├── DevNotificadorFactory.java
        └── ProdNotificadorFactory.java
```

## Ejecucion

El proyecto usa Maven con Java 17.

```bash
mvn test
mvn exec:java
```

Para levantar la API REST con Spring Boot:

```bash
mvn spring-boot:run
```

Base URL:

```text
http://localhost:8080/api
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Tambien se puede generar el jar ejecutable:

```bash
mvn package
java -jar target/escrims-tpo-1.0.0.jar
```

## API REST

La API REST funciona como adaptador de entrada: recibe JSON, resuelve DTOs y delega en
`ScrimFacade`. No saltea la arquitectura interna ni accede directo al dominio.

| Metodo | Endpoint | Uso |
| --- | --- | --- |
| POST | `/api/usuarios` | Crear usuario de prueba con juego, rango, latencia y verificacion. |
| GET | `/api/usuarios` | Listar usuarios cargados en memoria. |
| POST | `/api/scrims` | Crear scrim. |
| GET | `/api/scrims/{scrimId}` | Consultar estado del scrim. |
| POST | `/api/scrims/{scrimId}/postulaciones` | Postular usuario con rol. |
| POST | `/api/scrims/{scrimId}/confirmaciones` | Confirmar asistencia. |
| POST | `/api/scrims/scheduler` | Procesar inicio automatico por `fechaHora`. |
| POST | `/api/scrims/{scrimId}/finalizar` | Finalizar scrim en juego. |
| POST | `/api/scrims/{scrimId}/estadisticas` | Registrar resultados y calcular MVP. |
| POST | `/api/notificaciones/email` | Suscribir usuarios a notificaciones por email. |
| POST | `/api/notificaciones/push` | Suscribir usuarios a notificaciones push. |
| POST | `/api/notificaciones/discord` | Suscribir usuarios a notificaciones Discord. |

Ejemplo minimo para crear un usuario:

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H 'Content-Type: application/json' \
  -d '{"username":"Alpha","email":"alpha@mail.com","passwordHash":"hash123","region":"SA","juego":"Valorant","rango":1500,"latencia":30,"verificarEmail":true}'
```

## Demo del Main

`Main.java` demuestra:

1. Crear un scrim 2v2 de Valorant mediante `ScrimFacade`.
2. Configurar notificaciones por Email y Push.
3. Postular cuatro jugadores; `BuscandoState` delega compatibilidad en `MatchmakingStrategy`.
4. Transicionar automaticamente a `LOBBY_ARMADO`.
5. Confirmar a todos y pasar a `CONFIRMADO`.
6. Procesar el scheduler para iniciar automaticamente al llegar `fechaHora`.
7. Finalizar y registrar estadisticas.
8. Calcular MVP.
9. Mostrar una operacion invalida en estado terminal.

## Diagramas

| Archivo | Descripcion |
| --- | --- |
| `diagrama-clases-escrims.puml` | Diagrama de clases con estereotipos de patrones. |
| `diagrama-estados-scrim.puml` | Maquina de estados del scrim. |
| `diagrama-secuencia-postulacion.puml` | Secuencia de postulacion, State, Observer y Strategy. |

## Evidencias

Se verifico compilacion, tests, API REST y demo con Maven:

```bash
mvn test
mvn exec:java
```

Resultado actual de tests: 57 ejecutados, 0 fallas, 0 errores.
