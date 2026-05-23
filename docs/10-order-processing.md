# Order Processing System for E-commerce

## Overview

This document describes the design of a scalable order processing system for a large e-commerce platform.

The system must support:

- millions of users
- high traffic during sales
- reliable payment handling
- inventory updates
- notification delivery
- asynchronous order processing

The architecture focuses on scalability, reliability, fault tolerance, and high availability.

---

# Functional Requirements

The system should support:

- placing orders
- payment processing
- inventory updates
- sending notifications
- updating analytics
- retrying failed operations
- handling large traffic spikes during sales

---

# Non-Functional Requirements

The system should provide:

- high availability
- fault tolerance
- scalability
- low latency for user actions
- reliable message delivery
- eventual consistency for non-critical operations

---

# Sync vs Async Boundaries

Some operations must happen synchronously while others should happen asynchronously.

## Synchronous Operations

These operations happen immediately during checkout:

- validating cart
- checking inventory availability
- processing payment
- creating order record

These are synchronous because the user must receive an immediate response confirming whether the order succeeded or failed.

Example:

```text
User clicks Buy Now
↓
payment succeeds
↓
order created
↓
response returned to user
```

# Asynchronous Operations

These operations happen after the order is successfully placed:

- sending email notifications
- updating analytics
- recommendation updates
- shipment tracking updates
- invoice generation

These are asynchronous because they are not required before responding to the user.

Example:

```text
Order created
↓
publish order.created event
↓
workers process notifications and analytics independently
````

Using asynchronous processing improves:

* response latency
* throughput
* scalability

---

# Queue Choice

RabbitMQ is used as the message broker.

## Why RabbitMQ

RabbitMQ was chosen because:

* simple setup
* reliable delivery
* durable queues
* retry support
* dead letter queue support
* good for task processing systems

The order processing pipeline mainly requires reliable event delivery rather than extremely high throughput.

Kafka is better for:

* large streaming pipelines
* analytics systems
* log aggregation

RabbitMQ is more suitable for transactional workflows like order processing.

---

# Replication and Delivery Flow

## Order Flow

```text
User places order
↓
Order Service validates payment
↓
Order stored in database
↓
order.created event published
↓
Notification Worker sends confirmation
↓
Inventory Worker updates stock
↓
Analytics Worker updates metrics
```

---

# Delivery Semantics

Different parts of the system require different delivery guarantees.

---

## At-Most-Once

Used for:

* analytics tracking
* recommendation metrics

Reason:

Losing a few analytics events is acceptable.

No retries are needed.

---

## At-Least-Once

Used for:

* notifications
* shipment updates
* inventory updates

Reason:

These operations should not be lost.

Duplicate processing is handled using idempotency.

Example:

```text
same event processed twice
↓
inventory updated only once
```

---

## Exactly-Once

Used for:

* payment processing

Reason:

A payment must never be charged twice.

This is handled using:

* transaction IDs
* payment idempotency keys
* database transactions

---

# Idempotency

Consumers use event IDs to prevent duplicate processing.

Example:

```text
eventId = abc123
```

If the same event is received again:

```text
duplicate skipped
```

This prevents:

* double notifications
* duplicate inventory reduction
* duplicate shipment creation

---

# Retry Strategy

Temporary failures are retried automatically.

Example failures:

* network timeout
* database unavailable
* third-party API timeout

Retry strategy:

* retry 3 times
* exponential backoff between retries

Example:

```text
retry after:
1 second
2 seconds
4 seconds
```

---

# Dead Letter Queue (DLQ)

If an event fails repeatedly after retries:

```text
move event to DLQ
```

The Dead Letter Queue stores failed events for later inspection.

Example causes:

* corrupted payload
* invalid order data
* permanently failing service

Benefits:

* prevents infinite retry loops
* isolates poison messages
* improves system stability

---

# Backpressure Handling

During flash sales, event producers may become faster than consumers.

Example:

```text
10000 orders per second
↓
notification workers cannot keep up
```

To prevent overload:

* bounded processing queues are used
* slow consumers apply load shedding
* excess events may be delayed or dropped for non-critical systems

Critical services like payments are never dropped.

Implemented strategy in this project:

* slow consumer simulated using `Thread.sleep(5000)`
* pending event counter used for bounded processing
* if pending events exceed threshold, events are dropped
* prevents worker overload and resource exhaustion

Example:

```text
Current pending events in feed worker: 6
Backpressure triggered. Dropping event
```

---

# Hot Partition Mitigation

Some products may become extremely popular during sales.
This can overload a single database shard.

Mitigation strategies:

* consistent hashing
* virtual nodes
* partition spreading
* caching hot products
* read replicas

---

# Scaling Strategy

The system scales horizontally using:

* multiple workers
* replicated queues
* sharded databases
* distributed caching

Consumers can be increased independently depending on workload.

Example:

```text
increase notification workers
without affecting payment service
```

---

# Consistency Choice

The system uses mixed consistency.

## Strong Consistency

Used for:

* payments
* inventory
* order creation

Reason:

Incorrect values can cause financial problems.

---

## Eventual Consistency

Used for:

* analytics
* recommendations
* notifications

Reason:

- Small delays are acceptable.
- This improves scalability and availability.

---

# Conclusion

The system combines synchronous and asynchronous processing to achieve:

* scalability
* reliability
* fault tolerance
* low latency

RabbitMQ enables reliable event-driven communication between services.

Features implemented:

* message queues
* retries
* idempotency
* dead letter queues
* backpressure handling
* asynchronous workers

This architecture supports large-scale e-commerce workloads while maintaining high system reliability.

```
```

