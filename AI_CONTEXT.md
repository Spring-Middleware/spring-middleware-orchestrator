# AI_CONTEXT.md — Spring Middleware Orchestrator

## 📌 Objetivo

Construir un **motor de orquestación de flujos (Flow Orchestrator)** desacoplado de infraestructura, reutilizable como framework dentro de Spring Middleware.

Origen:
- Basado en un sistema real (Triggle EventHandler)
- Evolucionado a arquitectura limpia + modelo declarativo

---

## 🏗️ Arquitectura

### Módulos

- `orchestrator-core`
    - lógica pura (sin Spring)
- `orchestrator-infra`
    - wiring Spring, Mongo, loaders, schedulers

---

## 📦 Estructura conceptual

### core.domain → DEFINICIÓN (qué se ejecuta)
- `FlowDefinition`
- `FlowActionDefinition`
- `ActionDefinition`
- `NextActionDefinition`
- `TimeoutDefinition`
- `FlowId`

👉 Representa el JSON de los flows

---

### core.engine → COMPORTAMIENTO (cómo se ejecuta)
- `FlowExecutor` ✅ (movido aquí)
- `ExecutionContextManager`
- `NextActionResolver`
- `TimeoutRedirectResolver`
- `TimeoutRedirectResolverRegistry`
- `FlowDefinitionRegistry`
- `FlowDefinitionLoader`

👉 Orquesta todo el motor

---

### core.runtime → ESTADO DE EJECUCIÓN
- `FlowExecution`
- `ExecutionContext`
- `ActionExecution`
- `ExecutionStatus`
- `FlowExecutionTimeout`
- `FlowTrigger`

👉 Representa estado vivo/persistido

---

### core.engine.port → PUERTOS (ports)
- `ActionRegistry`
- `FlowExecutionRegistry`
- `ExecutionContextRegistry`
- `ExecutionContextStore`
- `TimeoutScheduler`

👉 Interfaces desacopladas de infra

---

## 🔌 Infraestructura

### infra.config
- Beans Spring
- wiring de:
    - `FlowExecutor`
    - `ExecutionContextManager`
    - registries
    - schedulers

---

### infra.engine.loader
- `JsonFlowDefinitionLoader`
  → carga flows desde:
```
classpath*:flows/*.json
```

---

### infra.engine.registry
- `InMemoryFlowDefinitionRegistry`
- `MongoFlowExecutionRegistry`
- `PersistedExecutionContextStore`
- `InMemoryExecutionContextRegistry`
- `DefaultTimeoutRedirectResolverRegistry`

---

### infra.engine.scheduler
- `InMemoryTimeoutScheduler`

---

### infra.engine.repository
- Mongo:
    - `FlowExecutionDocument`
    - `ExecutionContextPersistedDocument`
- Spring Data repositories

---

## 🧠 Conceptos clave

### 1. FlowExecutor

Motor principal:

- inicia ejecución (`start`)
- continúa (`resume`)
- ejecuta acciones
- resuelve siguientes pasos
- delega en registries/ports

---

### 2. ExecutionContextManager

Sustituye a `EventContextHandler`

Responsabilidades:

- memoria → `ExecutionContextRegistry`
- persistencia → `ExecutionContextStore`
- timeouts → `TimeoutScheduler`

👉 Coordinador central

---

### 3. Separación crítica

ANTES:
```
EventContextHandler = todo mezclado
```

AHORA:
```
ExecutionContextRegistry  → memoria
ExecutionContextStore     → persistencia
TimeoutScheduler          → timeouts
ExecutionContextManager   → coordinación
```

---

### 4. Modelo declarativo

ANTES:
```
clazzName → reflection
```

AHORA:
```
name → registry → bean
```

Ejemplo:
```json
{
  "timeout": {
    "resolver": "errorTimeout"
  }
}
```

---

### 5. TimeoutRedirectResolver

```java
interface TimeoutRedirectResolver {
    String redirectAction();
    Object get();
}
```

Implementación:
```java
@ResolverName("errorTimeout")
class ErrorTimeoutRedirectResolver
```

---

## ⚠️ Decisiones importantes

### ❌ NO usar
- `Class.forName`
- `clazzName`
- lógica Spring en core

---

### ✅ SÍ usar
- registries
- nombres lógicos
- separación core / infra

---

## 🔄 Flujo de ejecución

1. `FlowTrigger`
2. `FlowExecutor.start()`
3. crea `FlowExecution`
4. obtiene `FlowDefinition`
5. ejecuta acción
6. `NextActionResolver`
7. puede:
    - continuar
    - persistir contexto
    - programar timeout
8. `TimeoutScheduler`
9. timeout expirado
10. `ExecutionContextManager.load()`
11. `TimeoutRedirectResolver`
12. siguiente acción
13. continúa ejecución

---

## ⚙️ Estado actual

✔ Arquitectura limpia completa  
✔ Core desacoplado de Spring  
✔ Sin reflection  
✔ Registries implementados  
✔ Timeout modelado  
✔ Persistencia Mongo  
✔ ExecutionContext desacoplado  
✔ Separación de responsabilidades correcta

---

## 🚀 Próximo foco (IMPORTANTE)

### 1. FlowExecutor end-to-end
- start → execute → persist → timeout → resume → end

---

### 2. Timeout real
- polling o scheduler:
```java
@Scheduled(...)
```

---

### 3. Integración completa de resolvers
- `TimeoutRedirectResolverRegistry` en ejecución real

---

### 4. Test de flujo completo
- escenario real con JSON
- persistencia + timeout + resume

---

### 5. API pública clara

```java
flowExecutor.start(...)
flowExecutor.resume(...)
```

---

## 🧠 Filosofía

- Framework, no aplicación
- Declarativo > imperativo
- Sin magia oculta
- Control explícito
- Pensado para microservicios complejos

---

## 👤 Autor

Fernando Guardiola Ruiz  
Spring Middleware Platform

---