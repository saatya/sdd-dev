# SPEC-001: Hello Greeting Service with Sidecar Log Shipping

| Field   | Value                         |
|---------|-------------------------------|
| Status  | Approved                      |
| Owner   | API Platform Engineering      |
| Version | 1.0                           |

> This spec is the source of truth. Code and tests are derived from it.
> Every acceptance criterion (AC-n) must have at least one test that names it.
> Change the spec first, then the tests, then the code.

## 1. Purpose

A minimal Spring Boot service that returns a greeting. It is deployed to
Kubernetes with a **sidecar container** that reads the app's log file from a
shared volume and writes it to a **local disk mount on the node** (hostPath).

The point of the project is to demonstrate the pattern, not the business logic.

## 2. API contract

The machine-readable contract is `openapi/hello-api.yaml`. It must stay in sync
with this section.

### `GET /api/v1/hello`

| Query param | Required | Rules                                                      |
|-------------|----------|------------------------------------------------------------|
| `name`      | no       | 1–50 chars; letters, spaces, hyphens, apostrophes only     |

**200 OK** — `application/json`

```json
{ "message": "Hello, World!", "timestamp": "2026-10-05T20:27:00Z" }
```

**400 Bad Request** — `application/problem+json` (RFC 9457)

```json
{ "type": "about:blank", "title": "Invalid name", "status": 400,
  "detail": "name must be 1-50 characters: letters, spaces, hyphens or apostrophes" }
```

## 3. Acceptance criteria — application

- **AC-1** `GET /api/v1/hello` with no `name` returns 200 and message `Hello, World!`.
- **AC-2** `GET /api/v1/hello?name=Satya` returns 200 and message `Hello, Satya!`.
- **AC-3** Leading/trailing whitespace in `name` is trimmed before use.
- **AC-4** A `name` that is blank, longer than 50 chars, or contains other
  characters returns 400 as `application/problem+json`, with title `Invalid name`.
- **AC-5** Every response body includes an ISO-8601 UTC `timestamp`.
- **AC-6** Every successful greeting writes one INFO log line containing
  `greeting served name=<name>` to the log file set by `logging.file.name`.
- **AC-7** Rejected input writes one WARN log line containing `greeting rejected`.
  The rejected value itself is **not** logged (avoids log injection).
- **AC-8** `/actuator/health/liveness` and `/actuator/health/readiness` return 200 `UP`.

## 4. Acceptance criteria — deployment

- **AC-9**  The app container writes logs to `/var/log/app/app.log` on a shared
  `emptyDir` volume, and also to stdout.
- **AC-10** A sidecar container mounts the same volume read-only and appends
  every line to `/node-logs/<pod-name>.log` on a hostPath mount.
- **AC-11** The sidecar starts **before** the app and stops **after** it
  (Kubernetes native sidecar: `initContainers` + `restartPolicy: Always`, K8s ≥ 1.29),
  so no log lines are lost at startup or shutdown.
- **AC-12** Each replica writes to its own file (pod name via Downward API),
  so replicas never interleave writes.
- **AC-13** Both containers run with `allowPrivilegeEscalation: false`, all
  capabilities dropped, and a read-only root filesystem. The app runs as non-root.
- **AC-14** Liveness and readiness probes use the endpoints in AC-8.
- **AC-15** The app log file rotates at 10 MB, keeping 5 files; the sidecar
  follows the file by name across rotation (`tail -F`).

## 5. Out of scope

- Shipping logs off the node (that is the job of a node agent such as
  Fluent Bit or the platform's log pipeline).
- Authentication, TLS, ingress.

## 6. Traceability

| AC        | Verified by                                              |
|-----------|----------------------------------------------------------|
| AC-1..5   | `HelloControllerTest`                                    |
| AC-6, 7   | `GreetingLogFileTest`                                    |
| AC-8      | `HealthProbesTest`                                       |
| AC-9..15  | `k8s/deployment.yaml` review + `make verify` on kind     |
