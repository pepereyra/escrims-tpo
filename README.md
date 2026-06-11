# eScrims — Sistema de Scrims Competitivos

TPO Final — Análisis y Diseño Orientado a Objetos  
Universidad Argentina de la Empresa (UADE)

---

## 📋 Descripción

eScrims es un sistema que permite a jugadores competitivos organizar **scrims** (partidas de práctica) entre equipos. El sistema gestiona el ciclo de vida completo de un scrim: desde la búsqueda de jugadores hasta el registro de estadísticas y MVP al finalizar.

---

## 🏗️ Patrones de Diseño Aplicados

| Patrón | Paquete | Propósito |
|--------|---------|-----------|
| **State** | `state/` | Ciclo de vida del Scrim (BUSCANDO → LOBBY_ARMADO → CONFIRMADO → EN_JUEGO → FINALIZADO/CANCELADO) |
| **Observer** | `observer/` | Notificaciones automáticas ante cambios de estado del Scrim |
| **Strategy** | `strategy/` | Canal de notificación intercambiable (Email, Push, Discord) |
| **Abstract Factory** | `factory/` | Creación de notificadores según entorno (DEV / PROD) |
| **Builder** | `state/ScrimBuilder.java` | Construcción de ScrimContext con validación de invariantes |

### Patrones GRASP aplicados
- **Controller** → `ScrimController`: punto de entrada único para todas las operaciones
- **Information Expert** → `BuscandoState`: conoce y aplica todas las reglas de postulación
- **Creator** → `BuscandoState`: crea `Postulacion` y `Confirmacion` (las contiene y registra)
- **Low Coupling** → `DomainEventBus`: desacopla emisores de receptores de eventos
- **High Cohesion** → cada clase tiene una única responsabilidad bien definida

### Principios SOLID aplicados
- **SRP** → cada clase tiene una sola razón para cambiar
- **OCP** → nuevos estados/notificadores se agregan sin modificar código existente
- **LSP** → todos los `ScrimState` son intercambiables en `ScrimContext`
- **ISP** → `Subscriber` y `NotificadorStrategy` son interfaces mínimas y específicas
- **DIP** → `ScrimContext` depende de `ScrimState` (interfaz), no de estados concretos

---

## 📁 Estructura del Proyecto

```
src/main/java/escrims/
├── Main.java                          # Punto de entrada — flujo completo 2v2
├── controller/
│   └── ScrimController.java           # GRASP Controller — orquesta todas las operaciones
├── dominio/
│   ├── Usuario.java                   # Entidad principal con verificación y cooldown
│   ├── Equipo.java                    # Agrupación de usuarios
│   ├── Postulacion.java               # Registro de postulación con rol
│   ├── Confirmacion.java              # Confirmación de asistencia al scrim
│   ├── Estadistica.java               # KDA y MVP por jugador
│   ├── Notificacion.java              # Registro de notificaciones enviadas
│   └── enums/                         # CanalNotificacion, Rol, EstadoPostulacion, etc.
├── state/                             # PATRON STATE
│   ├── ScrimState.java                # Interfaz del estado
│   ├── ScrimContext.java              # Contexto — delega en el estado actual
│   ├── ScrimBuilder.java              # PATRON BUILDER — construye ScrimContext validado
│   ├── BuscandoState.java             # Estado inicial — acepta postulaciones
│   ├── LobbyArmadoState.java          # Cupos completos — acepta confirmaciones
│   ├── ConfirmadoState.java           # Todos confirmaron — listo para iniciar
│   ├── EnJuegoState.java              # Scrim en curso
│   ├── FinalizadoState.java           # Scrim terminado — acepta estadísticas
│   └── CanceladoState.java            # Estado terminal — no acepta operaciones
├── observer/                          # PATRON OBSERVER
│   ├── DomainEvent.java               # Interfaz base de eventos de dominio
│   ├── DomainEventBus.java            # Bus de eventos — notifica a suscriptores
│   ├── Subscriber.java                # Interfaz del suscriptor
│   ├── NotificationSubscriber.java    # Suscriptor que delega en NotificadorStrategy
│   ├── ScrimStateChangedEvent.java    # Evento: cambio de estado del scrim
│   └── ScrimCreadoEvent.java          # Evento: scrim recién creado
├── strategy/                          # PATRON STRATEGY
│   ├── NotificadorStrategy.java       # Interfaz de notificación
│   ├── EmailNotificador.java          # Estrategia: envío por email
│   ├── PushNotificador.java           # Estrategia: push notification
│   └── DiscordNotificador.java        # Estrategia: mensaje de Discord
└── factory/                           # PATRON ABSTRACT FACTORY
    ├── NotificadorFactory.java        # Interfaz de la fábrica
    ├── DevNotificadorFactory.java     # Fábrica DEV — simula envíos en consola
    └── ProdNotificadorFactory.java    # Fábrica PROD — envíos reales

src/test/java/escrims/
└── ScrimFlowTest.java                 # 23 tests — cubre todos los estados y transiciones
```

---

## 📊 Diagramas

| Archivo | Descripción |
|---------|-------------|
| `diagrama-clases-escrims.puml` | Diagrama de clases UML completo del dominio |
| `diagrama-estados-scrim.puml` | Máquina de estados del ciclo de vida del Scrim |
| `diagrama-secuencia-postulacion.puml` | Secuencia: postulación → transición → notificación |

Abrir con [PlantUML](https://plantuml.com/) o el plugin de PlantUML en IntelliJ IDEA.

---

## 🚀 Cómo Ejecutar

### Requisitos
- Java 17+
- Maven 3.8+

### Compilar
```bash
mvn compile
```

### Ejecutar el Main (flujo completo 2v2)
```bash
mvn exec:java -Dexec.mainClass="escrims.Main"
```

### Correr los tests
```bash
mvn test
```

Resultado esperado: **23 tests, 0 failures, 0 errors**.

---

## 🎮 Flujo del Main

El `Main.java` demuestra el ciclo de vida completo de un scrim 2v2 de Valorant:

1. **Crear scrim** → estado `BUSCANDO` (rango 1400-1700, latencia ≤ 80ms)
2. **Configurar notificaciones** → Alpha/Bravo por Email, Charlie/Delta por Push
3. **Postular 4 jugadores** → al completarse los cupos, transición automática a `LOBBY_ARMADO`
4. **Confirmar todos** → transición automática a `CONFIRMADO`
5. **Iniciar** → estado `EN_JUEGO`
6. **Finalizar** → estado `FINALIZADO`
7. **Registrar estadísticas** → calcula KDA y determina MVP (Charlie con KDA 7.0)
8. **Demo de error** → intento de postular en estado `FINALIZADO` lanza `IllegalStateException`

---

## 📝 Justificación de Patrones

Ver el archivo [`justificacion-patrones-grasp-solid.md`](justificacion-patrones-grasp-solid.md) para la justificación detallada de cada patrón GRASP y principio SOLID aplicado.
