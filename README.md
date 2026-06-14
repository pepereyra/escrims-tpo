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
| Repository | `escrims.service` / `escrims.infra.persistence` | Desacopla el dominio de la persistencia JPA sobre H2 o MySQL. |
| Builder | `escrims.domain.state.ScrimBuilder` | Construccion incremental de `ScrimContext` con validacion de invariantes. |
| Command | `escrims.domain.command` | Encapsula acciones de gestion de roles y suplentes del scrim. |

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
│       ├── ModeracionRestController.java
│       ├── OpenApiConfig.java
│       ├── ScrimRestController.java
│       ├── UsuarioApiRepository.java
│       └── UsuarioRestController.java
├── service/
│   ├── ScrimRepository.java
│   ├── FeedbackRepository.java
│   ├── ReporteConductaRepository.java
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
    ├── notification/
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
```

Valores por defecto de MySQL:

```text
Database: escrims
User: escrims_user
Password: escrims_pass
Root password: root_pass
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

En modo API, usuarios, scrims, postulaciones, confirmaciones, estadisticas, feedback y reportes se persisten con
Spring Data JPA. Por defecto se usa H2 en memoria; con el perfil `mysql` se usa la base MySQL
levantada por Docker Compose. El dominio no esta anotado con JPA: se usa un adapter de
infraestructura que implementa `ScrimRepository`.

La seguridad basica de la API incluye hashing de contrasenas con PBKDF2 y salt, emision de
JWT para login, roles de sistema `USER`, `MOD` y `ADMIN`, y rate limiting fijo sobre `/api/**`.
Los endpoints de resolucion de moderacion requieren `Authorization: Bearer <token>` con rol
`MOD` o `ADMIN`.

| Metodo | Endpoint | Uso |
| --- | --- | --- |
| POST | `/api/auth/register` | Registrar usuario con contraseña hasheada y emitir JWT. |
| POST | `/api/auth/login` | Autenticar usuario y emitir JWT. |
| GET | `/api/auth/me` | Consultar usuario autenticado con `Authorization: Bearer <token>`. |
| POST | `/api/auth/me/verificar-email` | Pasar email de pendiente a verificado. |
| PUT | `/api/auth/me/perfil` | Editar perfil: juego, rango, roles, region, latencia y disponibilidad. |
| POST | `/api/usuarios` | Crear usuario de prueba con juego, rango, latencia y verificacion. |
| GET | `/api/usuarios` | Listar usuarios persistidos. |
| POST | `/api/scrims` | Crear scrim. |
| GET | `/api/scrims?juego=&formato=&region=&rangoMin=&rangoMax=&fecha=&latenciaMax=` | Buscar scrims por filtros. |
| GET | `/api/scrims/{scrimId}` | Consultar estado del scrim. |
| POST | `/api/scrims/{scrimId}/postulaciones` | Postular usuario con rol. |
| POST | `/api/scrims/{scrimId}/confirmaciones` | Confirmar asistencia. |
| POST | `/api/scrims/scheduler` | Procesar inicio automatico por `fechaHora`. |
| POST | `/api/scrims/{scrimId}/finalizar` | Finalizar scrim en juego. |
| POST | `/api/scrims/{scrimId}/roles/cambiar` | Cambiar el rol asignado a un jugador aceptado. |
| POST | `/api/scrims/{scrimId}/roles/intercambiar` | Intercambiar roles entre dos jugadores aceptados. |
| POST | `/api/scrims/{scrimId}/suplentes` | Mover un jugador aceptado a suplente y liberar un cupo. |
| POST | `/api/scrims/{scrimId}/estadisticas` | Registrar resultados y calcular MVP. |
| POST | `/api/scrims/{scrimId}/feedback` | Cargar rating y comentario entre participantes de un scrim finalizado. |
| GET | `/api/scrims/{scrimId}/feedback` | Listar feedback del scrim. |
| POST | `/api/feedback/{feedbackId}/aprobar` | Aprobar feedback pendiente. Requiere rol `MOD` o `ADMIN`. |
| POST | `/api/feedback/{feedbackId}/rechazar` | Rechazar feedback pendiente. Requiere rol `MOD` o `ADMIN`. |
| POST | `/api/scrims/{scrimId}/reportes` | Crear reporte de conducta entre participantes. |
| GET | `/api/scrims/{scrimId}/reportes` | Listar reportes del scrim. |
| POST | `/api/reportes/{reporteId}/aprobar` | Aprobar reporte, registrar sancion y sumar strike al reportado. Requiere rol `MOD` o `ADMIN`. |
| POST | `/api/reportes/{reporteId}/rechazar` | Rechazar reporte pendiente. Requiere rol `MOD` o `ADMIN`. |
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
