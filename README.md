# Spring Middleware Orchestrator

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-green.svg)](https://spring.io/projects/spring-boot)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.spring-middleware/bom.svg)](https://central.sonatype.com/artifact/io.github.spring-middleware/bom)
![Status](https://img.shields.io/badge/status-active%20development-brightgreen)
[![Architecture](https://img.shields.io/badge/Architecture-Microservices%20Platform-blueviolet.svg)](#architecture)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Lightweight, explicit flow orchestrator for Spring Boot microservices.  
Designed to model **real execution flows** (not just chains of calls) with support for:

- Dynamic routing
- Pause / Resume (ideal for async messaging like Kafka)
- External event continuation
- Timeout-based redirection
- Execution traceability

---

# Why this exists

Most systems end up implementing orchestration implicitly:

- `if / else` scattered across services
- ad-hoc Kafka consumers triggering logic
- retry / timeout logic duplicated everywhere
- no clear view of "the flow"

This orchestrator makes the flow:

> **Explicit, declarative, traceable and extensible**

---

# Mental Model

A flow is:

> A deterministic state machine with optional dynamic transitions

Execution:

1. Start at `firstAction`
2. Execute action
3. Resolve next action
4. Repeat until:
    - Final action
    - Pause (CONSUMER)
    - Error
    - Timeout

---

# Core Concepts

## Flow

A flow defines:

- Entry point (`firstAction`)
- Actions
- Transition logic

Example:

    {
      "flowId": { "value": "SIMPLE_FLOW" },
      "firstAction": "FIRST_ACTION",
      "actions": [ ... ]
    }

---

## Action

Each action:

- Has a name
- Has a type
- Can define next action
- Can define timeout

---

## Action Types

### FUNCTION

- Transforms input → output
- Pure computation / enrichment
- Executes sequentially until another type is encountered or flow ends

Used for:
- Build payloads
- Compute state
- Prepare context

---

### CONSUMER

- Side-effect action
- Does NOT return output

Used for:
- Kafka publish
- External API calls
- Notifications

Important behavior:

> If NOT `finalAction`, the flow **pauses automatically** and its state is persisted.

---

### RESUME

- Entry point for resumed flows

Used when:
- Flow was paused
- External event triggers continuation

---

# Control Flow

Every action execution must eventually resolve the next step in the flow. This is done through **Resolvers**. The orchestrator includes built-in resolvers, but you can also implement your own.

## The Built-in Fixed Resolver

The engine provides a default resolver (`FIXED_NEXT_ACTION`) that simply transitions to a pre-defined static action:

    {
      "resolver": "FIXED_NEXT_ACTION",
      "parameters": {
        "nextAction": "SECOND_ACTION"
      }
    }

---

## Custom Resolvers

As a developer, you can implement custom resolvers to decide the next action dynamically based on the execution context or external data.

Examples you can build:
- Probability-based routing (A/B testing)
- Conditional routing (e.g. `if context.amount > 100 then APPROVAL_ACTION else AUTO_APPROVE_ACTION`)
- External decision making

---

# Pause & Resume (Key Feature)

## How it works

1. Flow executes
2. Hits a CONSUMER (non-final)
3. Engine:
    - Persists execution context
    - Stops execution
4. External system triggers resume
5. Engine:
    - Loads context
    - Continues from RESUME action

---

## Real example

Flow:

    CREATE_CONTEXT_ACTION
        → SEND_CONTEXT_ACTION (CONSUMER → pause)
        → RESUME_CONTEXT_ACTION
        → LAST_CONTEXT_ACTION

---

## Resume trigger example

    flowExecutor.resumeFlow(flowExecutionId, "RESUME_CONTEXT_ACTION", null);

---

## Typical use cases

- Kafka event continuation
- External callbacks
- Approval flows
- Async workflows

---

# Timeout Handling

Flows can define a `timeout` explicitly on paused steps (i.e. **`CONSUMER`** actions). 

If a `CONSUMER` action pauses the flow but is not resumed within the specified time, the engine automatically triggers the timeout behavior. The timeout block **must specify a resolver** (built-in or custom) to dictate where the flow goes next.

    {
      "actionName": "WAIT_FOR_PAYMENT",
      "actionType": "CONSUMER",
      "timeout": {
        "seconds": 30,
        "resolver": "FIXED_NEXT_ACTION",
        "parameters": {
          "nextAction": "PAYMENT_TIMEOUT_ERROR_ACTION"
        }
      }
    }

---

## What it enables

- Expiration flows
- SLA enforcement
- Fallback logic
- Dead-letter style behavior

---

# Example Flows

## 1. Simple Flow

    FIRST_ACTION → SECOND_ACTION (final)

---

## 2. Chained Flow

    FIRST → SECOND → THIRD (final)

---

## 3. Resolver Flow (probability)

    FIRST → (resolver) → FIRST_PROB / SECOND_PROB

---

## 4. Context Flow (pause/resume)

    CREATE → SEND (pause)
           → RESUME → LAST

---

## 5. Timeout Flow

    CREATE → SEND (pause + timeout)
           → (resume OR timeout)
           → END / ERROR

---

# Execution Model

Each execution produces:

- flowExecutionId
- requestId
- executionStatus
- timestamps
- ordered action executions
- runtime context

Example:

    FIRST_ACTION → executed
    SECOND_ACTION → executed
    STATUS → EXECUTED

---

# What makes this different

This is NOT:

- A BPM engine
- A heavy workflow platform
- A black box

This IS:

- Explicit
- Minimal
- Composable
- Developer-friendly

---

# What you've validated (very important)

Your flows already prove:

✔ Fixed transitions  
✔ Multi-step chaining  
✔ Custom resolver execution  
✔ Pause & resume with persisted context  
✔ Kafka-driven resume  
✔ Timeout redirection  
✔ Error propagation

That is already **a serious orchestration engine**, not a toy.

---

# Architecture & Modules

The project is structured in modular components:

- `orchestrator-core`: The pure engine implementation (FlowExecutor, Resolvers, ExecutionContext)
- `orchestrator-infra`: Infrastructure bindings (MongoDB persistence, Kafka integrations, scheduling)
- `orchestrator-demo`: A ready-to-run reference application demonstrating patterns
- `examples`: JSON definitions showing various flow capabilities

---

# Getting Started

## 1. Add Dependencies

Add the core and infra modules to your Spring Boot project (assuming Maven):

```xml
<dependency>
    <groupId>io.github.spring.middleware</groupId>
    <artifactId>orchestrator-core</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>io.github.spring.middleware</groupId>
    <artifactId>orchestrator-infra</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 2. Implement your Actions

Implement the action interfaces corresponding to the step type:

```java
@Component
public class CreateOrderAction implements FunctionAction<OrderRequest, OrderContext> {
    @Override
    public OrderContext execute(OrderRequest input, ExecutionContext context) {
        // Business logic here
        return new OrderContext(input.getId(), "CREATED");
    }
}
```

## 3. Define the Flow

Create a flow definition mapping out the actions:

```json
{
  "flowId": { "value": "ORDER_CREATION_FLOW" },
  "firstAction": "CREATE_ORDER",
  "actions": [
    {
      "actionName": "CREATE_ORDER",
      "actionClazz": "com.example.actions.CreateOrderAction",
      "actionType": "FUNCTION",
      "nextActionDefinition": {
        "nextActionSupplierClazz": "com.example.eval.OrderCreatedEvaluator",
        "nextActionSupplierParams": { "nextActionSupplierParamsType": "VOID" }
      }
    }
  ]
}
```

## 4. Trigger Execution

Inject the `FlowExecutor` naturally in your services to start a flow:

```java
@Service
public class OrderService {
    private final FlowExecutor flowExecutor;

    public OrderService(FlowExecutor flowExecutor) {
        this.flowExecutor = flowExecutor;
    }

    public void startOrder(OrderRequest request) {
        flowExecutor.startFlow("ORDER_CREATION_FLOW", request);
    }
}
```

---

# Running the Demo Application

The repository includes a ready-to-run demo application (`orchestrator-demo` module) that showcases the engine's capabilities. 
To run the demo locally, you'll need the supporting infrastructure.

## 1. Infrastructure Setup

A `docker-compose.yml` file is provided to easily spin up the required databases (like MongoDB) and message brokers. Here is the configuration needed for MongoDB:

```yaml
services:
  mongo:
    image: mongo:6.0
    container_name: mongo
    profiles: ["infra"]
    restart: always
    ports:
      - "27018:27017" # Exposed for local access
    environment:
      MONGO_INITDB_DATABASE: catalog
    volumes:
      - mongo-data:/data/db
    networks:
      - middleware-net

networks:
  middleware-net:
    driver: bridge

volumes:
  mongo-data:
```

Launch the infrastructure in the background using the `infra` profile:

```bash
docker-compose --profile infra up -d
```

## 2. Start the Demo

Once the database and necessary components are running, you can start the orchestrator demo via Maven:

```bash
# Build the project first
mvn clean install -DskipTests

# Run the demo application
cd orchestrator-demo
mvn spring-boot:run
```

---

# Next interesting improvements

If you want to evolve this:

### Control flow
- Conditional resolver
- Parallel branches
- Loop support

### Reliability
- Idempotent resume
- Retry policies
- Deduplication

### Engine
- Async action support (CompletableFuture)
- Better state persistence model

### Observability
- Metrics
- Tracing
- Flow visualization
