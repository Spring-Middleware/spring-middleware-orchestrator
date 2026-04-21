# AI_CONTEXT.md — Spring Middleware Orchestrator

## 📌 Objetivo

Construir un **motor de orquestación de flujos** reutilizable dentro de Spring Middleware, con separación clara entre:

- **definición del flow**
- **motor de ejecución**
- **estado runtime**
- **adaptadores de infraestructura**

El origen conceptual sigue siendo el antiguo **EventHandler de Triggle**, pero la implementación actual ya está reformulada como un **framework declarativo** y desacoplado.

---

## 🏗️ Arquitectura actual

### Módulos

- `orchestrator-core`
    - dominio, runtime, contratos del motor
    - sin dependencias de Spring
- `orchestrator-infra`
    - Spring, Mongo, loaders JSON, repositorios, schedulers

---

## 📦 Estructura conceptual

### `core.domain` → definición del flujo
Representa **qué existe en el flow**, no su estado de ejecución.

Clases principales:
- `FlowDefinition`
- `ActionDefinition`
- `NextActionDefinition`
- `TimeoutDefinition`
- `FlowId`

Notas:
- `FlowDefinition` contiene:
    - `flowId`
    - `firstAction`
    - colección de `actions`
- `ActionDefinition` contiene:
    - `actionName`
    - `actionType`
    - `configuration`
    - `nextAction`
    - `timeout`
    - `finalAction`
    - `removeContextOnLoad`
- `NextActionDefinition` contiene:
    - `resolver`
    - `parameters`

Importante:
- ya no se usa `clazzName`
- la definición es **declarativa**
- las referencias son por **nombre lógico**

---

### `core.runtime` → estado de ejecución
Representa **qué está pasando realmente durante la ejecución**.

Clases principales:
- `FlowExecution`
- `ExecutionContext`
- `ActionExecution`
- `ActionExecutionContext`
- `ExecutionStatus`
- `FlowExecutionNextAction`
- `NextActionResolverResult`
- `FlowExecutionTimeout`
- `FlowTrigger`

Notas:
- `FlowExecution` representa la ejecución persistible del flow
- `ExecutionContext` representa el contexto vivo en memoria
- `ActionExecution` guarda trazabilidad de cada acción ejecutada
- `FlowTrigger<T>` sí puede ser genérico, porque es API en memoria
- `FlowExecutionDocument` **no debe ser genérico**

---

### `core.engine` → comportamiento del motor
Contiene el **cómo se ejecuta el flow**.

Clases principales:
- `FlowExecutor`
- `ExecutionContextManager`
- `FlowActionExecutor`
- `FlowNextActionResolver`
- `FlowExecutorRedirectScheduler`
- `CommonFlowActionExecutor`
- `FunctionFlowActionExecutor`
- `ConsumerFlowActionExecutor`
- `SupplierFlowActionExecutor`

También:
- `ActionExecutorUtils`

Notas importantes:
- `FlowExecutor` es el punto de entrada principal
- `FlowActionExecutor` delega por tipo de acción
- `Supplier` ya no significa “ejecutar un supplier Java”, sino **reanudar el flow con un payload ya recibido desde fuera**
- `FlowNextActionResolver` resuelve la siguiente acción a partir de:
    - flow definition
    - action actual
    - resultado de acción
    - resolver configurado

---

### `core.port` → puertos del motor
Contratos desacoplados de infraestructura.

Puertos principales:
- `ActionRegistry`
- `NextActionResolverRegistry`
- `FlowDefinitionRegistry`
- `FlowExecutionRegistry`
- `ExecutionContextRegistry`
- `ExecutionContextStore`
- `TimeoutScheduler`
- `TimeoutRedirectResolverRegistry`

---

## 🔁 Modelo de tipos de acción

### `FUNCTION`
- recibe un payload
- devuelve un resultado
- el resultado se usa para resolver la siguiente acción

Interfaz:
```java
public interface FunctionAction<T, R> extends Action {

    T parsePayload(Object payload) throws ActionException;

    R apply(
            ExecutionContext executionContext,
            ActionExecutionContext actionExecutionContext,
            T payload
    ) throws ActionException;
}
```

---

### `CONSUMER`
- recibe un payload
- no devuelve resultado
- normalmente:
    - side effects
    - persistencia
    - envío de mensajes
    - log

Interfaz análoga a `FunctionAction`, con parseo de payload delegable.

---

### `SUPPLIER`
En el modelo actual, **no genera el payload desde dentro**.

Significado real:
- representa un **punto de reanudación**
- el payload ya viene de fuera:
    - JMS
    - Kafka
    - callback
    - evento externo
    - timeout redirect

Por tanto:
- el engine **no ejecuta un bean supplier**
- simplemente registra la acción como ejecutada
- usa el payload entrante para resolver la siguiente transición

Semánticamente, es más parecido a:
- `RESUME`
- `INCOMING_EVENT`
- `EXTERNAL_STEP`

Pero por ahora se mantiene el nombre `SUPPLIER` por continuidad conceptual con el sistema anterior.

---

## 🚀 FlowExecutor

### Responsabilidades

- `startFlow(...)`
- `resumeFlow(...)`
- `redirectFlow(...)`

### `startFlow(...)`
Flujo actual:
1. crea `FlowExecution`
2. crea `ExecutionContext`
3. añade el contexto al manager
4. obtiene `FlowDefinition`
5. construye `FlowExecutionActionRequest`
6. lanza ejecución **asíncrona**
7. devuelve inmediatamente `flowExecutionId`

Notas:
- el flow ya no se ejecuta de forma bloqueante
- se usa `CompletableFuture.runAsync(..., flowExecutorTaskExecutor)`
- el cleanup del `ExecutionContext` se hace en el `finally` del bloque async
- no debe hacerse `removeExecutionContext(...)` en el `finally` externo del método

---

### `resumeFlow(...)`
Flujo actual:
1. busca `FlowExecution`
2. obtiene `FlowDefinition`
3. obtiene la `ActionDefinition` actual
4. valida que sea una acción de reanudación (`SUPPLIER/RESUME` según el modelo)
5. carga `ExecutionContext` desde `ExecutionContextManager`
6. construye `FlowExecutionActionRequest`
7. lanza ejecución async
8. limpia el contexto al final del hilo async

Notas:
- `loadExecutionContext(...)` ya vuelve a inyectar el contexto en memoria
- no debe hacerse `removeExecutionContext(...)` antes de cargarlo
- si no existe contexto persistido, debe fallar explícitamente

---

### `redirectFlow(...)`
Responsabilidad:
- redirigir flows expirados por timeout

Flujo:
1. obtiene `resolver` desde `FlowExecutionTimeout.timeoutDefinition()`
2. busca `TimeoutRedirectResolver`
3. si no existe, usa el resolver default
4. llama a `resumeFlow(...)` con:
    - `flowExecutionId`
    - `redirectAction()`
    - `get()` como payload

---

## 🧠 ExecutionContextManager

Es el coordinador central del lifecycle del contexto.

Responsabilidades:
- memoria runtime
- carga/almacenamiento persistido
- gestión de timeouts

Internamente coordina:
- `ExecutionContextRegistry`
- `ExecutionContextStore`
- `TimeoutScheduler`

Métodos importantes:
- `addExecutionContext(...)`
- `removeExecutionContext(...)`
- `persistExecutionContext(...)`
- `loadExecutionContext(...)`
- `getFlowExecutionTimeoutByDateTime(...)`

### `loadExecutionContext(...)`
Comportamiento actual:
- carga desde `ExecutionContextStore`
- si `remove == true`:
    - elimina contexto persistido
    - elimina timeout programado
- vuelve a añadir el contexto a memoria
- devuelve el `ExecutionContext`

Importante:
- debe validar null antes de hacer `addExecutionContext(...)`

---

## 🔀 Resolución de siguiente acción

### `NextActionDefinition`
Contiene:
- `resolver`
- `parameters`

### `NextActionResolver`
Diseño actual orientado a extensibilidad real.

Interfaz aproximada:
```java
public interface NextActionResolver<T, P extends NextActionResolverParams> {

    P buildParams(Map<String, Object> params);

    NextActionResolverResult resolveNextAction(
            ExecutionContext executionContext,
            T actionResult,
            P nextActionParams
    );
}
```

### Decisión importante
Se descartó usar un modelo cerrado con `@JsonSubTypes` en `NextActionResolverParams`, porque eso impediría que usuarios externos definan sus propios resolvers sin tocar el core.

La dirección actual es:
- `NextActionDefinition.parameters` viene como estructura genérica
- el propio resolver construye sus params concretos con `buildParams(...)`

Ventaja:
- sistema abierto a extensiones
- no obliga a registrar subtipos en el core
- no depende exclusivamente de JSON; puede cargarse desde cualquier origen

---

## 🧾 FlowExecutionActionRequest

Representa la unidad que circula por el motor entre acciones.

Campos:
- `executionContext`
- `flowDefinition`
- `actionDefinition`
- `payload`

Observación:
- `actionName` debe salir de `actionDefinition`
- se usa como objeto de paso entre executors

---

## ⚙️ Executors concretos

### `CommonFlowActionExecutor`
Extrae lógica común:
- `endFlow(...)`
- `addActionExecutionToFlowExecution(...)`
- `processActionException(...)`

Importante:
- tras reorganización, el cleanup del `ExecutionContext` debe vivir en `FlowExecutor`, no en cada action executor

---

### `FunctionFlowActionExecutor`
Flujo:
1. obtiene la action del `ActionRegistry`
2. configura la action con `ActionExecutorUtils`
3. parsea payload
4. ejecuta `apply(...)`
5. guarda `ActionExecution`
6. si no es final:
    - resuelve siguiente acción
    - devuelve nuevo `FlowExecutionActionRequest`
7. si es final:
    - marca flow como `EXECUTED`
    - devuelve `null`

Refactor importante:
- se redujo redundancia usando `try/catch/finally`
- `ActionExecution` se persiste en `finally`

---

### `ConsumerFlowActionExecutor`
Flujo:
1. obtiene action del registry
2. configura action
3. parsea payload
4. ejecuta `consume(...)`
5. si la acción no es final:
    - puede persistir contexto y timeout
6. registra `ActionExecution`
7. si es final:
    - cierra flow

---

### `SupplierFlowActionExecutor`
Flujo:
1. no ejecuta bean supplier
2. usa el payload ya entrante como resultado de la acción
3. registra `ActionExecution`
4. si es final:
    - cierra flow
5. si no:
    - resuelve siguiente acción con ese payload
    - devuelve siguiente `FlowExecutionActionRequest`

---

## 🧩 Parseo de payload

### Problema detectado
Cuando el payload entra desde JSON/Mongo/resume externo, muchas veces llega como:
- `Map<String,Object>`
- `LinkedHashMap`

y no como el tipo real esperado por la action.

### Dirección adoptada
Cada action puede exponer:
```java
T parsePayload(Object payload)
```

y el engine intenta:
1. resolver el tipo genérico esperado (`T`) mediante `ResolvableType`
2. si el payload ya es instancia del tipo esperado, usarlo tal cual
3. si no, delegar en `parsePayload(...)`

Se llegó a una utilidad base tipo:
```java
protected <T> T parsePayload(
        Object rawPayload,
        Action action,
        Class<?> targetClass,
        int genericIndex,
        Function<Object, T> payloadParser
)
```

La idea es:
- evitar casts ciegos
- permitir fallback controlado por la propia action
- no obligar a meter toda la lógica compleja dentro de la interfaz

Conclusión actual:
- sí tiene sentido un `default` en la interfaz
- pero delegando a utilidad central, no metiendo toda la infraestructura dentro de la interfaz

---

## 🧰 ActionExecutorUtils

Responsabilidad actual:
- configurar una action a partir de `ActionDefinition.configuration`

Implementación actual:
- introspección de setters
- asignación por nombre de propiedad

Reflexión importante:
esto **no sustituye la configuración de Spring**.

Tiene sentido solo para:
- parametrización por step
- reutilizar la misma action varias veces en un mismo flow con distinta configuración

No tiene sentido para:
- configuración estable/global del bean
- infraestructura del componente

La idea madura es distinguir:
- **configuración del bean** → Spring
- **configuración del step** → orchestrator

---

## ⏱️ Timeouts

### `TimeoutScheduler`
Responsable de almacenar timeouts activos.

### `InMemoryTimeoutScheduler`
Implementación actual:
- mantiene mapa en memoria
- devuelve expirados por fecha
- al devolverlos, los elimina del mapa

Método clave:
```java
getFlowExecutionTimeoutByDateTime(LocalDateTime dateTime)
```

Patrón actual:
- filtra expirados
- hace remove por `flowExecutionId`
- construye `FlowExecutionTimeout`
- filtra nulls

Eso evita reprocesar el mismo timeout varias veces.

---

### `FlowExecutorRedirectScheduler`
Scheduler Spring que:
1. consulta timeouts expirados
2. por cada uno llama a `flowExecutor.redirectFlow(...)`
3. encapsula cada redirect en su propio `try/catch`

Pendiente fino:
- asegurar robustez y logs por timeout individual

---

## 💾 Persistencia

### `MongoFlowExecutionRegistry`
Responsabilidades actuales:
- `createFlowExecution(...)`
- `findById(...)`
- `addActionExecutionToFlowExecution(...)`
- `updateFlowExecution(...)`

### `FlowExecutionDocument`
Importante:
- no debe ser genérico
- `context` debe ser:
    - `Object`
    - o `Map<String,Object>`
- no `T`

Se detectó que usar genéricos en documentos Mongo complica mucho la deserialización por type erasure.

### `updateFlowExecution(...)`
Dirección actual:
- recibe `FlowExecution` ya actualizado
- valida existencia si se desea
- persiste la versión mapeada a documento
- devuelve el aggregate

---

## 🌐 API REST de prueba

Se añadió un controller inicial:

- `POST /flows`
    - arranca flujo
    - devuelve `202 Accepted`
    - body con `UUID`
- `GET /flows/{flowExecutionId}`
    - devuelve estado actual de ejecución
    - `404` si no existe

Esto ya permite probar flows de punta a punta.

---

## ✅ Estado probado

Ya se probó un flow básico end-to-end:

### Flow ejemplo
- `FIRST_ACTION` → `FUNCTION`
- `SECOND_ACTION` → `CONSUMER`
- resolver intermedio:
    - `FIXED_NEXT_ACTION`

Ejemplo probado:
- la primera acción genera un objeto a partir de configuración
- la segunda lo consume / loguea
- el flow finaliza correctamente

Esto valida:
- carga de flow definition
- paso de payload entre acciones
- resolución de siguiente acción
- persistencia básica de ejecución
- cierre del flow

---

## 🔜 Próximos pasos naturales

### 1. Flow encadenado
`FUNCTION -> FUNCTION -> CONSUMER`
para validar transformaciones múltiples del payload

---

### 2. Error handling real
Flow donde una action lanza excepción y comprobar:
- `FlowExecution.executionStatus = ERROR`
- `ActionExecution.error`
- trazabilidad completa

---

### 3. Resume real
Flow con paso de espera externa:
- start
- persist context
- resume con payload externo
- continuar y terminar

---

### 4. Timeout + redirect
Probar:
- persistencia de contexto
- programación de timeout
- scheduler
- redirect
- resume automático por timeout

---

### 5. Reutilización de misma action con distinta configuración
Validar que la parametrización por step realmente aporta valor frente a la configuración estática de Spring

---

## 🧠 Decisiones importantes consolidadas

### ✅ Sí
- core sin Spring
- names + registries
- actions por tipo (`FUNCTION`, `CONSUMER`, `SUPPLIER`)
- `FlowExecutor` asíncrono
- payload parsing con fallback
- params de resolvers construidos por el propio resolver
- distinción bean config vs step config

### ❌ No
- `Class.forName`
- `clazzName` en flow definitions
- jerarquías cerradas con `@JsonSubTypes` para params de resolvers del usuario
- genéricos en documentos Mongo
- cleanup del contexto repartido por todos los action executors

---

## 👤 Autor

Fernando Guardiola Ruiz  
Spring Middleware Platform