# Secure Login System

# Functional Requirements

The system should:

- support secure signup and login
- generate JWT access and refresh tokens
- protect APIs using authentication middleware
- authorize users using RBAC
- rate limit abusive requests
- process async events using RabbitMQ
- expose logs and metrics
- support distributed tracing

---

# Non-Functional Requirements

The system should provide:

## Scalability

- horizontally scalable workers
- asynchronous event processing
- distributed caching support

## Reliability

- at-least-once delivery
- idempotent consumers
- retry mechanisms
- dead letter queues

## Security

- hashed passwords
- JWT signature verification
- RBAC authorization
- brute-force protection

## Availability

- circuit breaker protection
- graceful failure handling
- backpressure handling

## Observability

- structured JSON logging
- request tracing
- metrics collection
- distributed tracing

## Performance

- low request latency
- concurrent request handling
- caching for feeds

---

# Authentication Flow

The authentication system uses:

- access tokens
- refresh tokens
- password hashing
- JWT verification middleware

Flow:

User Login
↓
Credentials verified
↓
Access token generated
↓
Refresh token generated
↓
Client stores tokens
↓
Protected APIs require JWT

---

# Password Storage

Passwords are never stored in plain text.

The system uses:

- BCrypt password hashing

Example:

```java
String encodedPassword =
    passwordEncoder.encode(password);
```

Why BCrypt:

- adaptive hashing
- salted hashes
- resistant to rainbow table attacks
- industry standard

Even if the database is leaked, raw passwords remain protected.

---

# JWT Structure

JWT consists of three parts:

Header.Payload.Signature

Example:

```text
xxxxx.yyyyy.zzzzz
```

## Header

Contains:

- algorithm
- token type

Example:

```json
{
  "alg":"HS256",
  "typ":"JWT"
}
```

## Payload

Contains claims:

```json
{
  "sub":"harsha",
  "role":"ADMIN",
  "exp":"..."
}
```

## Signature

Generated using:

- secret key
- header
- payload

The server verifies the signature before trusting the token.

---

# Access Token Strategy

Access tokens are short-lived.

Current expiry:

```text
15 minutes
```

Purpose:

- authenticate API requests
- reduce risk if token leaks

Used on protected routes:

```http
Authorization: Bearer <token>
```

---

# Refresh Token Strategy

Refresh tokens are long-lived.

Current expiry:

```text
24 hours
```

Purpose:

- generate new access tokens
- avoid forcing frequent logins

Flow:

Access token expires
↓
Client sends refresh token
↓
Server validates refresh token
↓
New access token generated

---

# JWT Verification Middleware

Custom JWT middleware verifies:

- token existence
- Bearer format
- signature validity
- expiration

If valid:

- username attached to request
- role attached to request

If invalid:

- request rejected with 401

---

# RBAC (Role-Based Access Control)

Implemented roles:

- USER
- ADMIN

## USER

Can:

- create posts
- view feed

Cannot:

- delete arbitrary posts

## ADMIN

Can:

- access all USER actions
- delete any post

Example:

```java
if (!"ADMIN".equals(role)) {
    return "Unauthorized";
}
```

RBAC prevents unauthorized actions.

---

# Brute Force Protection

The system implements a token bucket rate limiter.

Applied on:

```http
POST /api/post
```

Limit:

```text
5 requests per minute
```

Purpose:

- prevent spam
- reduce abuse
- mitigate brute force attacks

Implementation:

- custom token bucket
- per-user buckets
- automatic refill

Example:

```text
6th request blocked
```

---

# Circuit Breaker Protection

Circuit breaker protects the system from cascading failures.

States:

- CLOSED
- OPEN
- HALF_OPEN

Flow:

Repeated DB failures
↓
Circuit opens
↓
Requests blocked temporarily
↓
Recovery test request allowed
↓
Circuit closes on success

Benefits:

- prevents overload
- improves resilience
- avoids retry storms

---

# Session Revocation

JWTs are stateless by default.

Possible revocation strategies:

- blacklist tokens
- rotate refresh tokens
- maintain session store
- invalidate tokens on logout

Production systems usually store revoked refresh tokens in Redis.

---

# Security Vulnerabilities and Mitigation

## Common JWT Vulnerability

Weak secret keys.

Risk:

- attackers forge tokens

Mitigation:

- long random secret keys
- rotate secrets
- use environment variables

Another risk:

- storing JWT in localStorage

Mitigation:

- HTTP-only cookies
- secure cookies
- short-lived access tokens

---

# Observability and Security Monitoring

The platform includes structured observability.

Features:

- request IDs
- trace IDs
- latency metrics
- structured JSON logs

Example log:

```json
{
  "requestId":"abc123",
  "method":"POST",
  "path":"/api/post",
  "status":200,
  "latencyMs":45
}
```

Trace IDs propagate through async RabbitMQ workers.

Benefits:

- easier debugging
- security auditing
- request tracing
- incident analysis

---

# Dead Letter Queue (DLQ)

Repeatedly failing events are moved to a DLQ.

Purpose:

- isolate poison messages
- prevent infinite retries
- improve reliability

Example failures:

- corrupted payload
- invalid events
- processing exceptions

---

# Conclusion

The secure login architecture combines:

- JWT authentication
- refresh token flow
- RBAC authorization
- rate limiting
- observability
- distributed tracing
- circuit breakers
- DLQ resilience

This design provides:

- scalability
- fault tolerance
- secure authentication
- production-style backend architecture