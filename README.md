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
| Observer | `escrims.infra.events` | Publicacion de eventos de dominio y suscriptores desacoplados para notificaciones, alertas y auditoria. |
| Abstract Factory | `escrims.infra.notification` | Familias DEV/PROD de notificadores. |
| Adapter | `escrims.infra.notification` | Adapta integraciones externas simuladas a `NotificadorStrategy`. |
| Adapter | `escrims.infra.notification` | Adapta la publicacion RabbitMQ a la interfaz interna `NotificationMessagePublisher`. |
| Adapter | `escrims.infra.calendar` | Genera invitaciones iCal desde el modelo interno de scrims. |
| Repository | `escrims.service` / `escrims.infra.persistence` | Desacopla el dominio de la persistencia JPA sobre H2 o MySQL. |
| Builder | `escrims.domain.state.ScrimBuilder` | Construccion incremental de `ScrimContext` con validacion de invariantes. |
| Command | `escrims.domain.command` | Encapsula acciones de gestion de roles y suplentes del scrim, incluyendo undo. |
| Chain of Responsibility | `escrims.domain.moderation` | Procesa reportes con auto-resolver, bot y moderador humano. |
| Template Method | `escrims.domain.rules` | Define el flujo comun de validacion y delega reglas especificas por juego. |

## Regla Sin Enums

Los enums fueron eliminados del codigo fuente.

- `Rol` ahora es un value object de dominio en `escrims.domain.model.Rol`.
- `EstadoPostulacion`, `EstadoNotificacion` y `EstadoModeracion` son interfaces con implementaciones concretas simples.
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
│       ├── AuditRestController.java
│       ├── BusquedaFavoritaRestController.java
│       ├── CatalogoRestController.java
│       ├── FrontendRestController.java
│       ├── ModeracionRestController.java
│       ├── OpenApiConfig.java
│       ├── ScrimRestController.java
│       ├── UsuarioApiRepository.java
│       └── UsuarioRestController.java
├── service/
│   ├── ScrimRepository.java
│   ├── FeedbackRepository.java
│   ├── ReporteConductaRepository.java
│   ├── BusquedaFavoritaRepository.java
│   ├── AlertaBusquedaRepository.java
│   ├── AuditLogRepository.java
│   ├── BusquedaFavoritaService.java
│   ├── AuditService.java
│   ├── ScrimReminderService.java
│   ├── InMemoryScrimRepository.java
│   ├── ModeracionService.java
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
│   │   ├── Feedback.java
│   │   ├── ReporteConducta.java
│   │   ├── BusquedaFavorita.java
│   │   ├── AlertaBusqueda.java
│   │   ├── AuditLog.java
│   │   ├── RecordatorioScrim.java
│   │   ├── EstadoModeracion.java
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
│   ├── command/
│   │   ├── ScrimCommand.java
│   │   ├── CambiarRolCommand.java
│   │   ├── IntercambiarRolesCommand.java
│   │   └── MoverASuplenteCommand.java
│   ├── moderation/
│   │   ├── ReporteModeracionHandler.java
│   │   ├── AutoResolveReporteHandler.java
│   │   ├── BotModerationReporteHandler.java
│   │   └── HumanModerationReporteHandler.java
│   ├── rules/
│   │   ├── GameRulesTemplate.java
│   │   ├── GameRulesValidator.java
│   │   ├── ValorantGameRulesTemplate.java
│   │   ├── LeagueOfLegendsGameRulesTemplate.java
│   │   ├── Cs2GameRulesTemplate.java
│   │   ├── GenericGameRulesTemplate.java
│   │   └── GameRulesRegistry.java
│   └── matchmaking/
│       ├── MatchmakingStrategy.java
│       ├── ByMMRStrategy.java
│       ├── ByLatencyStrategy.java
│       ├── ByHistoryStrategy.java
│       └── CompositeMatchmakingStrategy.java
└── infra/
    ├── calendar/
    │   ├── CalendarAdapter.java
    │   └── ICalCalendarAdapter.java
    ├── events/
    │   ├── DomainEvent.java
    │   ├── DomainEventBus.java
    │   ├── Subscriber.java
    │   ├── NotificationSubscriber.java
    │   ├── ScrimCreadoEvent.java
    │   └── ScrimStateChangedEvent.java
    ├── notification/
        ├── NotificadorStrategy.java
        ├── EmailNotificador.java
        ├── PushNotificador.java
        ├── DiscordNotificador.java
        ├── NotificationDispatcher.java
        ├── QueuedNotificationDispatcher.java
        ├── NotificationQueueMessage.java
        ├── NotificationQueueNames.java
        ├── NotificationMessagePublisher.java
        ├── RabbitNotificationDispatcher.java
        ├── RabbitNotificationMessagePublisher.java
        ├── RabbitNotificationConsumer.java
        ├── RabbitNotificationConfig.java
        ├── SendGridEmailAdapter.java
        ├── FirebasePushAdapter.java
        ├── DiscordWebhookAdapter.java
        ├── NotificadorFactory.java
        ├── DevNotificadorFactory.java
        └── ProdNotificadorFactory.java
    └── persistence/
        ├── UsuarioJpaEntity.java
        ├── ScrimJpaEntity.java
        ├── PostulacionJpaEntity.java
        ├── ConfirmacionJpaEntity.java
        ├── EstadisticaJpaEntity.java
        ├── FeedbackJpaEntity.java
        ├── ReporteConductaJpaEntity.java
        ├── SpringDataUsuarioJpaRepository.java
        ├── SpringDataScrimJpaRepository.java
        ├── SpringDataFeedbackJpaRepository.java
        ├── SpringDataReporteConductaJpaRepository.java
        ├── JpaFeedbackRepositoryAdapter.java
        ├── JpaReporteConductaRepositoryAdapter.java
        ├── JpaBusquedaFavoritaRepositoryAdapter.java
        ├── JpaAlertaBusquedaRepositoryAdapter.java
        ├── JpaAuditLogRepositoryAdapter.java
        └── JpaScrimRepositoryAdapter.java
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

Por defecto, la API usa H2 en memoria para facilitar pruebas rapidas.

Para levantar la API y MySQL juntos con Docker Compose:

```bash
docker compose up --build
```

Con Docker Compose, la API queda disponible en `http://localhost:8080/api` y usa MySQL
persistente automaticamente mediante el perfil `mysql`.

Si se quiere levantar solo la base MySQL y correr la API desde Maven:

```bash
docker compose up -d mysql
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

El `docker-compose.yml` expone:

```text
API: http://localhost:8080/api
Swagger UI: http://localhost:8080/swagger-ui.html
MySQL: localhost:3306
RabbitMQ AMQP: localhost:5672
RabbitMQ Management: http://localhost:15672
```

Valores por defecto de MySQL:

```text
Database: escrims
User: escrims_user
Password: escrims_pass
Root password: root_pass
```

Valores por defecto de RabbitMQ:

```text
User: escrims
Password: escrims_pass
Queue: escrims.notifications.dispatch
Exchange: escrims.notifications
Routing key: notification.dispatch
```

La configuracion del perfil MySQL esta en `src/main/resources/application-mysql.properties`.
Tambien se puede cambiar por variables de entorno:

```text
MYSQL_HOST
MYSQL_PORT
MYSQL_DATABASE
MYSQL_USER
MYSQL_PASSWORD
JWT_SECRET
RATE_LIMIT_MAX_REQUESTS
RATE_LIMIT_WINDOW_MILLIS
REMINDERS_SCHEDULER_ENABLED
REMINDERS_HORAS_ANTES
REMINDERS_INITIAL_DELAY_MILLIS
REMINDERS_FIXED_DELAY_MILLIS
DEMO_DATA_ENABLED
NOTIFICATIONS_QUEUE
SPRING_RABBITMQ_HOST
SPRING_RABBITMQ_PORT
SPRING_RABBITMQ_USERNAME
SPRING_RABBITMQ_PASSWORD
```

Docker Compose activa `DEMO_DATA_ENABLED=true`, por lo que al levantar la API se precargan
usuarios y scrims demo si la base esta vacia. Todos los usuarios demo tienen password
`12345678`:

```text
admin    rol ADMIN
mod      rol MOD
alpha    rol USER
bravo    rol USER
charlie  rol USER
delta    rol USER
echo     rol USER
foxtrot  rol USER
```

Tambien se crea un scrim confirmado de Valorant dentro de las proximas 2 horas para probar
recordatorios automaticos, iCal y flujo de participantes, y un scrim casual de LoL en estado
BUSCANDO.

Docker Compose tambien activa `NOTIFICATIONS_QUEUE=rabbit`. En ese modo, las notificaciones
se publican como mensajes JSON en RabbitMQ y un consumidor interno de Spring AMQP las procesa
de forma asincronica segun el canal (`EMAIL`, `PUSH` o `DISCORD`). Si no se configura Rabbit,
la aplicacion usa `QueuedNotificationDispatcher` en memoria para mantener tests y ejecucion
local simples.

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

H2 Console:

```text
http://localhost:8080/h2-console
```

Datos de conexion H2:

```text
JDBC URL: jdbc:h2:mem:escrims
User: sa
Password: <vacio>
```

Tambien se puede generar el jar ejecutable:

```bash
mvn package
java -jar target/escrims-tpo-1.0.0.jar
```

## API REST

La API REST funciona como adaptador de entrada: recibe JSON, resuelve DTOs y delega en
`ScrimFacade`. No saltea la arquitectura interna ni accede directo al dominio.

En modo API, usuarios, scrims, postulaciones, confirmaciones, estadisticas, feedback, reportes,
busquedas favoritas, alertas y auditoria se persisten con
Spring Data JPA. Por defecto se usa H2 en memoria; con el perfil `mysql` se usa la base MySQL
levantada por Docker Compose. El dominio no esta anotado con JPA: se usa un adapter de
infraestructura que implementa `ScrimRepository`.

La seguridad basica de la API incluye hashing de contrasenas con PBKDF2 y salt, emision de
JWT para login, roles de sistema `USER`, `MOD` y `ADMIN`, y rate limiting fijo sobre `/api/**`.
Los endpoints de resolucion de moderacion requieren `Authorization: Bearer <token>` con rol
`MOD` o `ADMIN`.

Las busquedas favoritas se registran por usuario autenticado. Cuando se publica un
`ScrimCreadoEvent`, `BusquedaFavoritaService` actua como Observer, compara el nuevo scrim contra
las busquedas guardadas y genera alertas persistidas. Las notificaciones usan una cola simulada
con reintentos exponenciales antes de marcar una notificacion como fallida. La auditoria registra
cambios de estado y acciones de moderacion.

El scrim modela modalidad (`RANKED_LIKE`, `CASUAL`, `PRACTICA`). Las reglas variables por juego
se concentran en templates de `escrims.domain.rules`: `GameRulesTemplate` fija el algoritmo de
validacion comun para creacion y postulacion, mientras Valorant, LoL y CS2 declaran formatos y
roles permitidos. Los juegos no registrados usan un template generico para mantener el sistema
multijuego extensible. `ScrimBuilder` valida creacion contra esos templates y `BuscandoState`
valida roles al postular.

Para calendario, se expone
un adapter iCal y un scheduler Spring que procesa recordatorios N horas antes y envia
notificaciones simuladas a los participantes confirmados. Tambien existe un endpoint manual
para disparar el procesamiento durante la demo. La moderacion de reportes pasa por una cadena
auto-resolver -> bot -> moderador humano; los casos auto-resueltos se aprueban o rechazan
sin intervencion manual y el resto queda pendiente con la etapa registrada.

| Metodo | Endpoint | Uso |
| --- | --- | --- |
| POST | `/api/auth/register` | Registrar usuario con contraseña hasheada y emitir JWT. |
| POST | `/api/auth/login` | Autenticar usuario y emitir JWT. |
| GET | `/api/auth/me` | Consultar usuario autenticado con `Authorization: Bearer <token>`. |
| POST | `/api/auth/me/verificar-email` | Pasar email de pendiente a verificado. |
| PUT | `/api/auth/me/perfil` | Editar perfil: juego, rango, roles, region, latencia y disponibilidad. |
| GET | `/api/dashboard/me` | Resumen autenticado para frontend: perfil, mis scrims, alertas y proximos scrims. |
| POST | `/api/busquedas-favoritas` | Guardar una busqueda favorita del usuario autenticado. |
| GET | `/api/busquedas-favoritas` | Listar busquedas favoritas del usuario autenticado. |
| GET | `/api/alertas` | Listar alertas generadas por scrims compatibles con las busquedas favoritas. |
| GET | `/api/catalogos` | Consultar juegos soportados, formatos, roles y modalidades para frontend. |
| POST | `/api/usuarios` | Crear usuario de prueba con juego, rango, latencia y verificacion. |
| GET | `/api/usuarios` | Listar usuarios persistidos. |
| POST | `/api/scrims` | Crear scrim. |
| GET | `/api/scrims?juego=&formato=&region=&rangoMin=&rangoMax=&fecha=&latenciaMax=` | Buscar scrims por filtros. |
| GET | `/api/scrims/mis-scrims` | Listar scrims donde participa el usuario autenticado, con rol y confirmacion. |
| GET | `/api/scrims/{scrimId}` | Consultar estado del scrim. |
| GET | `/api/scrims/{scrimId}/participantes` | Obtener lobby agrupado por aceptados, suplentes, pendientes y rechazados. |
| POST | `/api/scrims/{scrimId}/postulaciones` | Postular usuario con rol. |
| POST | `/api/scrims/{scrimId}/confirmaciones` | Confirmar asistencia. |
| GET | `/api/scrims/{scrimId}/ical` | Descargar/generar el contenido iCal del scrim. |
| POST | `/api/scrims/recordatorios` | Procesar recordatorios automaticos N horas antes para scrims confirmados. |
| POST | `/api/scrims/scheduler` | Procesar inicio automatico por `fechaHora`. |
| POST | `/api/scrims/{scrimId}/finalizar` | Finalizar scrim en juego. |
| POST | `/api/scrims/{scrimId}/roles/cambiar` | Cambiar el rol asignado a un jugador aceptado. |
| POST | `/api/scrims/{scrimId}/roles/intercambiar` | Intercambiar roles entre dos jugadores aceptados. |
| POST | `/api/scrims/{scrimId}/suplentes` | Mover un jugador aceptado a suplente y liberar un cupo. |
| POST | `/api/scrims/{scrimId}/comandos/undo` | Deshacer el ultimo comando de gestion de roles/suplentes del scrim. |
| POST | `/api/scrims/{scrimId}/estadisticas` | Registrar resultados y calcular MVP. |
| POST | `/api/scrims/{scrimId}/feedback` | Cargar rating y comentario entre participantes de un scrim finalizado. |
| GET | `/api/scrims/{scrimId}/feedback` | Listar feedback del scrim. |
| POST | `/api/feedback/{feedbackId}/aprobar` | Aprobar feedback pendiente. Requiere rol `MOD` o `ADMIN`. |
| POST | `/api/feedback/{feedbackId}/rechazar` | Rechazar feedback pendiente. Requiere rol `MOD` o `ADMIN`. |
| POST | `/api/scrims/{scrimId}/reportes` | Crear reporte de conducta entre participantes. |
| GET | `/api/scrims/{scrimId}/reportes` | Listar reportes del scrim. |
| POST | `/api/reportes/{reporteId}/aprobar` | Aprobar reporte, registrar sancion y sumar strike al reportado. Requiere rol `MOD` o `ADMIN`. |
| POST | `/api/reportes/{reporteId}/rechazar` | Rechazar reporte pendiente. Requiere rol `MOD` o `ADMIN`. |
| GET | `/api/audit` | Consultar logs de auditoria. Requiere rol `ADMIN`. |
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

Se verifico compilacion, tests, API REST, seguridad, alertas, auditoria, iCal,
recordatorios, moderacion automatica y demo con Maven:

```bash
mvn test
mvn exec:java
```

Resultado actual de tests: 77 ejecutados, 0 fallas, 0 errores.
