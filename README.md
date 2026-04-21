# Spring Middleware Orchestrator

Lightweight, explicit flow orchestrator for Spring Boot microservices.  
Designed to model **real execution flows** (not just chains of calls) with support for:

- Dynamic routing
- Pause / Resume
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
- External calls
- Notifications

Important behavior:

> If NOT `finalAction`, the flow **pauses automatically**

---

### RESUME

- Entry point for resumed flows

Used when:
- Flow was paused
- External event triggers continuation

---

# Control Flow

## Fixed next action

    {
      "resolver": "FIXED_NEXT_ACTION",
      "parameters": {
        "nextAction": "SECOND_ACTION"
      }
    }

---

## Custom resolver

Resolvers decide the next action dynamically.

Examples:
- Probability-based routing
- Conditional routing
- External decision

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

Flows can define timeout on paused steps:

    {
      "timeout": {
        "seconds": 30,
        "resolver": "FIXED_NEXT_ACTION",
        "parameters": {
          "nextAction": "ERROR"
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

---

# When to use it

Use this when:

- You have async workflows
- You use Kafka/events heavily
- You need pause/resume
- You want explicit orchestration

Avoid if:
- You just need simple service chaining
- You want BPMN / visual modeling

---

