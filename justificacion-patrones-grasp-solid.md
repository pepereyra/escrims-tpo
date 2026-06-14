# eScrims — Justificación de Patrones de Diseño, GRASP y SOLID

---

## 1. PATRONES DE DISEÑO

Los patrones obligatorios según el enunciado son: **State, Strategy, Observer y Facade**.
El proyecto también aplica **Abstract Factory**, **Adapter** y **Builder** como patrones adicionales.

---

### 1.1 Patrón FACADE — Punto de entrada simplificado al subsistema

**Problema que resuelve:**
El subsistema de scrims está compuesto por múltiples clases con responsabilidades específicas:
`DomainEventBus` (Observer), `NotificadorFactory` (Abstract Factory), `ScrimBuilder` (Builder)
y `ScrimController` (GRASP Controller). El cliente no debería conocer ni instanciar ninguna
de estas clases directamente — hacerlo generaría un acoplamiento altísimo y haría que cualquier
cambio interno impacte en el cliente.

**Cómo se aplica:**
- `ScrimFacade` encapsula la creación y configuración de todos los subsistemas internos
  (`DomainEventBus`, `DevNotificadorFactory`, `ScrimService`, `ScrimController`) en su constructor.
- Expone únicamente los métodos de alto nivel que el cliente necesita:
  `crearScrim()`, `postular()`, `confirmar()`, `iniciar()`, `finalizar()`,
  `cancelar()`, `configurarNotificacionesEmail()`, `configurarNotificacionesPush()`,
  `configurarNotificacionesDiscord()`, `procesarScrimsProgramados()`,
  `registrarEstadisticas()`.
- El cliente (`Main`) solo conoce `ScrimFacade`. La API REST tambien delega en
  `ScrimFacade`, por lo que mantiene el mismo punto de entrada al subsistema.
  No importa ni instancia
  `DomainEventBus`, `NotificadorFactory`, `ScrimBuilder` ni `ScrimController`.

**Ejemplo de uso real (en Main.java):**
```java
// El cliente solo conoce ScrimFacade — no sabe nada del subsistema interno
ScrimFacade facade = new ScrimFacade();
ScrimContext scrim = facade.crearScrim("Valorant", "2v2", "SA", 1400, 1700, 80, ...);
facade.configurarNotificacionesEmail(List.of(alpha, bravo));
facade.postular(scrim.getId(), alpha, Rol.DUELIST);
```

**Relación con otros patrones:**
Internamente, `ScrimFacade` coordina:
- `ScrimController` (GRASP Controller) → coordina los casos de uso
- `ScrimService` (Service) → orquesta la lógica de aplicación
- `ScrimSchedulerService` (Service) → procesa inicios automáticos por `fechaHora`
- `ScrimBuilder` (Builder) → construye y valida `ScrimContext`
- `NotificadorFactory` (Abstract Factory) → crea notificadores según entorno
- `DomainEventBus` (Observer) → publica eventos de dominio
- Estados concretos (State) → implementan la lógica de cada fase del scrim

---

### 1.2 Patrón STATE — Ciclo de vida del Scrim

**Problema que resuelve:**
El `ScrimContext` tiene un comportamiento radicalmente distinto según en qué estado se encuentre.
Sin este patrón, tendríamos enormes bloques `if/else` o `switch` en cada método
(`postular`, `confirmar`, `iniciar`, etc.) chequeando el estado actual, lo que viola SRP y OCP.

**Cómo se aplica:**
- `ScrimState` es la interfaz que declara todas las operaciones posibles sobre un scrim:
  `postular()`, `confirmar()`, `iniciar()`, `finalizar()`, `cancelar()`.
- `ScrimContext` es el contexto: mantiene una referencia a la instancia actual de `ScrimState`
  y delega **todas** las operaciones a ella. El contexto **nunca** toma decisiones de
  comportamiento por sí mismo.
- Cada estado concreto (`BuscandoState`, `LobbyArmadoState`, `ConfirmadoState`,
  `EnJuegoState`, `FinalizadoState`, `CanceladoState`) implementa `ScrimState` y define
  qué hace (o qué lanza como excepción) ante cada operación.
- Las transiciones se realizan **desde dentro del estado concreto**, llamando a
  `ctx.setState(new NuevoEstado())`. El contexto siempre se pasa como parámetro al estado
  para que este pueda modificarlo.

**Ejemplo de transición correcta (en BuscandoState):**
```java
// Al completarse los cupos, el propio estado decide la transición
if (ctx.cuposDisponibles() == 0) {
    ctx.setState(new LobbyArmadoState());
    ctx.publicarEvento(new ScrimStateChangedEvent(..., "LOBBY_ARMADO"));
}
```

**Errores comunes evitados:**
- ❌ `cambiarEstado(): void` NO está en `ScrimState` — las transiciones las decide cada estado concreto.
- ❌ El contexto NO se pasa como campo del estado — se pasa como parámetro en cada método.
- ✅ Cada estado concreto implementa **todos** los métodos de la interfaz (los que no aplican
  lanzan `IllegalStateException` con mensaje descriptivo).

---

### 1.3 Patrón STRATEGY — Matchmaking y canal de notificación intercambiable

**Problema que resuelve:**
El sistema debe soportar algoritmos de matchmaking intercambiables y múltiples canales de
notificación (Email, Push, Discord). Sin Strategy, el estado `BuscandoState` o el
`NotificationSubscriber` terminarían con condicionales de rango, latencia, historial o canal,
violando OCP y SRP.

**Cómo se aplica:**
- `MatchmakingStrategy` define `esCompatible(Usuario, ScrimContext)`.
- `ByMMRStrategy`, `ByLatencyStrategy`, `ByHistoryStrategy` y
  `CompositeMatchmakingStrategy` encapsulan criterios de emparejamiento.
- `BuscandoState` delega en `ctx.getMatchmakingStrategy().esCompatible(usuario, ctx)`.
- `NotificadorStrategy` define el contrato:
  `getCanal(): String` y
  `enviar(Usuario destinatario, Notificacion notificacion): void`.
- `EmailNotificador`, `PushNotificador` y `DiscordNotificador` son las estrategias concretas,
  cada una con su propio algoritmo de envío encapsulado.
- `NotificationSubscriber` es el contexto: mantiene una referencia a `NotificadorStrategy`
  y delega el envío en ella. No sabe si está enviando por email, push o Discord.

**Ejemplo de uso real (en BuscandoState):**
```java
if (!ctx.getMatchmakingStrategy().esCompatible(u, ctx)) {
    throw new IllegalArgumentException("El usuario no cumple con las reglas de matchmaking.");
}
```

**Beneficio clave (OCP):**
Agregar un nuevo canal (ej: `SlackNotificador`) solo requiere:
1. Crear `SlackNotificador implements NotificadorStrategy`
2. Agregar `crearSlackNotificador()` a `NotificadorFactory`
3. Exponer un método explícito en la fachada si se quiere una API de alto nivel

No se modifica `NotificationSubscriber`, `DomainEventBus` ni ninguna clase existente.

---

### 1.4 Patrón OBSERVER — Notificaciones de eventos de dominio

**Problema que resuelve:**
Cuando el estado de un scrim cambia, múltiples subsistemas deben reaccionar (enviar push,
email, Discord). Si `ScrimContext` conociera directamente a `EmailNotificador`, `PushNotificador`
y `DiscordNotificador`, el acoplamiento sería altísimo y agregar un nuevo canal requeriría
modificar el dominio → viola DIP y OCP.

**Cómo se aplica:**
- `DomainEventBus` es el Subject: mantiene una lista de `Subscriber` y publica eventos
  con `publicar(DomainEvent)`.
- `Subscriber` es la interfaz Observer: define `onEvent(DomainEvent)`.
- `NotificationSubscriber` implementa `Subscriber`: recibe el evento, construye el mensaje
  y lo delega a su `NotificadorStrategy` (Email, Push o Discord).
- `ScrimContext` solo conoce a `DomainEventBus` (abstracción), no a los notificadores concretos.
- Los eventos concretos (`ScrimStateChangedEvent`, `ScrimCreadoEvent`) implementan `DomainEvent`.

**Flujo completo:**
```
Estado concreto → ctx.publicarEvento(ScrimStateChangedEvent)
    → DomainEventBus.publicar(evento)
        → NotificationSubscriber(email).onEvent(evento) → EmailNotificador.enviar()
        → NotificationSubscriber(push).onEvent(evento)  → PushNotificador.enviar()
```

---

### 1.5 Patrón ABSTRACT FACTORY — Creación de notificadores por entorno

**Problema que resuelve:**
En desarrollo (dev) no queremos enviar notificaciones reales (Firebase, SendGrid, Discord).
Necesitamos poder intercambiar familias completas de objetos relacionados (Push + Email + Discord)
según el entorno, sin que el código cliente sepa qué implementación concreta está usando.

**Cómo se aplica:**
- `NotificadorFactory` es la interfaz de fábrica abstracta: declara
  `crearEmailNotificador()`, `crearPushNotificador()`, `crearDiscordNotificador()`.
- `DevNotificadorFactory` crea notificadores que simulan el envío en consola (para dev y tests).
- `ProdNotificadorFactory` crea adapters de integración:
  `SendGridEmailAdapter`, `FirebasePushAdapter`, `DiscordWebhookAdapter`.
- `ScrimService` recibe `NotificadorFactory` por inyección de dependencia en el constructor.
- `ScrimFacade` decide qué fábrica usar (por defecto `DevNotificadorFactory`), ocultando
  esta decisión al cliente.

**Beneficio clave:**
Cambiar de entorno dev a prod es cambiar una sola línea en `ScrimFacade`:
```java
NotificadorFactory factory = new ProdNotificadorFactory(); // antes: DevNotificadorFactory
```
Todo el resto del código permanece igual.

---

### 1.6 Patrón ADAPTER — Integraciones externas de notificación

**Problema que resuelve:**
Los proveedores externos como SendGrid, Firebase o Discord Webhook no tienen por qué exponer
el mismo contrato que nuestro dominio necesita. El sistema interno quiere trabajar con
`NotificadorStrategy`, pero cada proveedor real suele tener SDKs, parámetros y payloads propios.

**Cómo se aplica:**
- `SendGridEmailAdapter`, `FirebasePushAdapter` y `DiscordWebhookAdapter` implementan
  `NotificadorStrategy`.
- Cada adapter traduce la notificación interna (`Usuario`, `Notificacion`) al formato que
  usaría el proveedor externo.
- En esta entrega las llamadas se simulan por consola para no depender de credenciales ni red,
  pero el punto de integración queda aislado.
- `ProdNotificadorFactory` crea estos adapters, manteniendo al cliente desacoplado de los
  detalles de infraestructura.

**Beneficio clave:**
Si mañana se reemplaza SendGrid por JavaMail o Discord por Slack, se cambia el adapter o la
fábrica concreta, no el dominio, ni `NotificationSubscriber`, ni `ScrimContext`.

---

### 1.7 Patrón BUILDER — Construcción incremental de Scrim

**Problema que resuelve:**
`ScrimContext` tiene múltiples parámetros en su constructor. Construirlo directamente es
propenso a errores (parámetros en orden incorrecto, valores nulos o inválidos). El Builder
permite construir el objeto paso a paso con una interfaz fluida y garantiza que el
`ScrimContext` resultante siempre sea válido.

**Cómo se aplica:**
- `ScrimBuilder` acumula los parámetros con métodos encadenados (fluent interface):
  `juego("Valorant").formato("2v2").rango(1400, 1700).build()`.
- El método `build()` llama a `validarInvariantes()` antes de crear el `ScrimContext`,
  garantizando que el objeto resultante siempre sea válido
  (ej: `rangoMin < rangoMax`, `fechaHora` en el futuro, `cuposTotales > 0` y par).
- `ScrimController` usa `ScrimBuilder` para crear scrims, nunca instancia `ScrimContext`
  directamente.
- Al finalizar `build()`, el Builder también publica un `ScrimCreadoEvent` en el
  `DomainEventBus`.

**Ejemplo de uso real (en ScrimController):**
```java
ScrimContext scrim = new ScrimBuilder(eventBus)
    .juego("Valorant")
    .formato("2v2")
    .region("SA")
    .rango(1400, 1700)
    .latenciaMax(80)
    .fechaHora(LocalDateTime.now().plusHours(2))
    .duracionMinutos(30)
    .cuposTotales(4)
    .build();
```

---

## 2. PATRONES GRASP

---

### 2.1 Experto en Información (Information Expert)

**Principio:** Asignar la responsabilidad a la clase que tiene la información necesaria para cumplirla.

| Responsabilidad | Clase asignada | Justificación |
|---|---|---|
| Saber si hay cupos disponibles | `ScrimContext` | Conoce `cuposTotales` y la lista de postulaciones aceptadas |
| Saber si todos confirmaron | `ScrimContext` | Conoce la lista de confirmaciones y `cuposTotales` |
| Saber si un usuario está en cooldown | `Usuario` | Conoce `cooldownHasta` y puede comparar con la fecha actual |
| Validar compatibilidad del candidato | `MatchmakingStrategy` | Encapsula criterios intercambiables de MMR, latencia e historial |
| Calcular el KDA | `Estadistica` | Conoce `kills`, `deaths` y `assists` |
| Determinar el MVP | `ScrimService` | Orquesta el caso de uso y tiene acceso a todas las estadísticas calculadas |

---

### 2.2 Creador (Creator)

**Principio:** Asignar la responsabilidad de crear un objeto A a la clase B si B contiene,
agrega, registra o usa objetos de tipo A.

| Objeto creado | Clase creadora | Justificación |
|---|---|---|
| `ScrimContext` | `ScrimBuilder` | El Builder acumula todos los datos necesarios para construir el ScrimContext |
| `Postulacion` | `BuscandoState` | El estado Buscando procesa la postulación y tiene todos los datos para crearla |
| `Confirmacion` | `BuscandoState` | Al aceptar la postulación, crea la Confirmacion pendiente asociada |
| `ScrimStateChangedEvent` | Los estados concretos | Cada estado sabe qué evento publicar al transicionar |
| `Notificacion` | `NotificationSubscriber` | Tiene el evento y el canal, puede construir la notificación |
| Notificadores/adapters concretos | `NotificadorFactory` | Es la responsabilidad explícita de la fábrica |

---

### 2.3 Controlador (Controller)

**Principio:** Asignar la responsabilidad de recibir y coordinar operaciones del sistema
a una clase que represente el sistema global o un caso de uso.

- `ScrimController` recibe las peticiones y las delega en `ScrimService`.
- `ScrimService` orquesta los casos de uso, usa `ScrimBuilder`, obtiene los scrims y
  configura notificaciones con `NotificadorFactory`.
- `ScrimSchedulerService` procesa scrims confirmados cuya `fechaHora` ya fue alcanzada y
  delega el inicio en `ScrimService`.
- Para crear un scrim, `ScrimService` delega en `ScrimBuilder` (Builder), nunca instancia
  `ScrimContext` directamente desde el controller.
- Para configurar notificaciones, `ScrimService` delega en `NotificadorFactory`
  (Abstract Factory), nunca instancia notificadores concretos.
- `ScrimFacade` actúa como una capa adicional sobre el Controller, ocultando incluso la
  existencia del Controller al cliente final.
- `ScrimRestController` y `UsuarioRestController` son controllers de entrada HTTP:
  convierten JSON/DTOs en llamadas a la fachada o al repositorio de usuarios de la API.
  No contienen reglas de negocio del scrim.

**Lo que el Controller NO hace:**
- No accede directamente a `ScrimContext` para cambiar su estado (eso lo hace el patrón State).
- No instancia `EmailNotificador`, `PushNotificador` ni `DiscordNotificador` directamente.
- No contiene validaciones de negocio (esas están en `ScrimBuilder`, estados concretos y estrategias).
- No duplica el flujo REST dentro del dominio: la API llama a `ScrimFacade`, que conserva
  la secuencia `Controller -> Service -> Domain`.

---

### 2.4 Bajo Acoplamiento (Low Coupling)

**Principio:** Asignar responsabilidades de modo que el acoplamiento entre clases sea mínimo.

Decisiones de diseño que reducen el acoplamiento:

1. **`ScrimContext` no conoce a los notificadores**: solo conoce a `DomainEventBus`.
   Los notificadores están completamente desacoplados del dominio.
2. **`NotificationSubscriber` no conoce la fábrica**: recibe un `NotificadorStrategy`
   ya construido. No sabe si es dev o prod.
3. **Los adapters aíslan proveedores externos**: SendGrid/Firebase/Discord quedan en
   infraestructura, no en dominio.
4. **`ScrimFacade` oculta el subsistema al cliente**: si el subsistema interno cambia
   (ej: se reemplaza `DomainEventBus` por otro bus), el cliente no se ve afectado.
5. **`BuscandoState` no conoce criterios concretos de matchmaking**: delega en
   `MatchmakingStrategy`.
6. **`ScrimService` depende de abstracciones**: `NotificadorFactory`,
   `NotificadorStrategy` y `MatchmakingStrategy`.
7. **La API REST no conoce servicios internos**: `ScrimRestController` delega en
   `ScrimFacade`; si cambia la implementacion de `ScrimService`, el contrato HTTP no
   necesita cambiar.

---

### 2.5 Alta Cohesión (High Cohesion)

**Principio:** Asignar responsabilidades de modo que cada clase tenga un conjunto de
responsabilidades fuertemente relacionadas.

| Clase | Responsabilidad única |
|---|---|
| `ScrimFacade` | Solo simplifica el acceso al subsistema de scrims |
| `ScrimRestController` | Solo traduce requests HTTP a operaciones de la fachada |
| `UsuarioRestController` | Solo administra usuarios de prueba para la API REST |
| `BuscandoState` | Solo maneja la lógica del estado "buscando jugadores" |
| `EmailNotificador` | Solo envía notificaciones por email |
| `ScrimBuilder` | Solo construye y valida instancias de `ScrimContext` |
| `DomainEventBus` | Solo gestiona la suscripción y publicación de eventos |
| `Estadistica` | Solo almacena y calcula métricas de rendimiento de un jugador |

**God Class evitada:** Sin estos patrones, `ScrimContext` sería una God Class que manejaría
estados, notificaciones, construcción y validación. Con el diseño propuesto, cada
responsabilidad está delegada a su clase correspondiente.

---

## 3. PRINCIPIOS SOLID

---

### 3.1 Single Responsibility Principle (SRP)

**Una clase debe tener una sola razón para cambiar.**

- `ScrimContext` cambia solo si cambia la estructura del scrim (atributos, cupos, equipos).
  No cambia si cambia la lógica de notificaciones.
- `EmailNotificador` cambia solo si cambia la forma de enviar emails.
  No cambia si cambia el formato de la notificación.
- `ScrimBuilder` cambia solo si cambia la forma de construir un scrim.
  No cambia si cambia la lógica de negocio de los estados.
- `ScrimFacade` cambia solo si cambia la API pública del subsistema.
  No cambia si cambia la implementación interna.
- `ScrimRestController` cambia solo si cambia el contrato HTTP.
  No cambia si cambia la logica interna de estados, matchmaking o notificaciones.

**Violación evitada:** Si `ScrimContext` tuviera métodos como `enviarEmailAJugadores()` o
`validarRango()`, tendría múltiples razones para cambiar → violación de SRP.

---

### 3.2 Open/Closed Principle (OCP)

**Abierto para extensión, cerrado para modificación.**

- **Nuevos canales de notificación**: crear `SlackNotificador implements NotificadorStrategy`
  y agregar `crearSlackNotificador()` a `NotificadorFactory`. No se modifica
  `NotificationSubscriber` ni `DomainEventBus`.
- **Nuevos algoritmos de matchmaking**: crear otra clase que implemente
  `MatchmakingStrategy` y configurarla en `ScrimBuilder`.
- **Nuevos tipos de eventos**: crear `ScrimPostulacionRechazadaEvent implements DomainEvent`.
  No se modifica `DomainEventBus`.
- **Nuevos estados**: crear `SuspendidoState implements ScrimState`.
  No se modifica `ScrimContext`.
- **Nuevo entorno**: crear `StagingNotificadorFactory implements NotificadorFactory`.
  No se modifica `ScrimController` ni `ScrimFacade`.

---

### 3.3 Liskov Substitution Principle (LSP)

**Una subclase puede reemplazar a su clase base sin alterar el correcto funcionamiento.**

- Cualquier `ScrimState` concreto puede reemplazar a otro en `ScrimContext` sin romper el
  sistema. El contexto siempre llama a los mismos métodos de la interfaz.
- Cualquier `NotificadorStrategy` concreta puede reemplazar a otra en `NotificationSubscriber`.
  El subscriber siempre llama a `enviar(destinatario, notificacion)`.
- Cualquier `NotificadorFactory` concreta puede reemplazar a otra en `ScrimController`.
  El controller siempre llama a `crearEmailNotificador()`, etc.

**Nota sobre LSP en State:** Los estados que no soportan una operación (ej:
`FinalizadoState.postular()`) lanzan `IllegalStateException`, lo cual es comportamiento
esperado y documentado → no viola LSP porque el cliente sabe que puede ocurrir esta excepción.

---

### 3.4 Interface Segregation Principle (ISP)

**Los clientes no deben estar forzados a implementar interfaces que no usan.**

- `ScrimState` define exactamente los métodos que un estado necesita. No tiene métodos
  de notificación ni de construcción.
- `NotificadorStrategy` define solo `getCanal()` y
  `enviar(Usuario destinatario, Notificacion notificacion): void`.
  No tiene métodos de suscripción ni de eventos.
- `Subscriber` define solo `onEvent(DomainEvent)`. No tiene métodos de envío ni de
  construcción de notificaciones.
- `NotificadorFactory` define solo los métodos de creación de notificadores. No tiene
  métodos de negocio.

---

### 3.5 Dependency Inversion Principle (DIP)

**Las clases de alto nivel no deben depender de clases de bajo nivel. Ambas deben depender
de abstracciones.**

| Clase de alto nivel | Depende de (abstracción) | NO depende de (implementación concreta) |
|---|---|---|
| `ScrimFacade` | `NotificadorFactory` | Detalles internos del service/controller |
| `ScrimRestController` | `ScrimFacade` | `ScrimService`, estados concretos, factories concretas |
| `ScrimContext` | `DomainEventBus`, `ScrimState`, `MatchmakingStrategy` | `EmailNotificador`, criterios concretos de matchmaking |
| `NotificationSubscriber` | `NotificadorStrategy` | `PushNotificador`, `EmailNotificador`, `DiscordNotificador` |
| `ScrimService` | `NotificadorFactory`, `NotificadorStrategy` | `DevNotificadorFactory`, `ProdNotificadorFactory` |
| `ProdNotificadorFactory` | `NotificadorStrategy` | Cliente de aplicación y dominio |

**Aplicación de DIP en la fábrica:** `NotificationSubscriber` recibe un `NotificadorStrategy`
por inyección de dependencias (constructor). La decisión de qué `NotificadorStrategy` concreto
usar la toma `NotificadorFactory`, que está en la capa de infraestructura, no en el dominio.

---

## 4. RESUMEN DE DECISIONES DE DISEÑO

| Decisión | Patrón/Principio | Beneficio |
|---|---|---|
| El cliente accede al sistema por un único punto | **Facade** | Oculta la complejidad del subsistema, bajo acoplamiento cliente-dominio |
| El estado del scrim se delega a objetos estado | **State** | Elimina condicionales, cada estado es cohesivo |
| El matchmaking se delega a algoritmos intercambiables | **Strategy** | MMR, latencia e historial cambian sin tocar el estado |
| El canal de notificación es intercambiable | **Strategy + OCP** | Nuevos canales sin modificar código existente |
| Las notificaciones se desacoplan del dominio | **Observer + DIP** | El dominio no conoce los canales de notificación |
| Los notificadores se crean por fábrica según entorno | **Abstract Factory** | Intercambio dev/prod sin cambiar código cliente |
| Los proveedores externos se aíslan con adapters | **Adapter + DIP** | SendGrid/Firebase/Discord no contaminan dominio |
| El inicio por fechaHora se procesa fuera del estado | **Service + State** | Simula scheduler sin meter threads ni cron en el dominio |
| El scrim se construye con validaciones | **Builder + SRP** | Objeto siempre válido, construcción legible |
| Cada clase tiene una sola responsabilidad | **SRP + High Cohesion** | Fácil de mantener, testear y extender |
| Las dependencias apuntan a abstracciones | **DIP + Low Coupling** | Bajo acoplamiento entre capas |
