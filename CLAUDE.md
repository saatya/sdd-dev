# hello-k8s-sidecar

Spring Boot 3.5 / Java 21 greeting service, deployed to Kubernetes with a
native sidecar that copies the app's log file to a hostPath on the node.

## Spec-driven workflow (always follow this order)

1. **Spec**  `specs/*.md` is the source of truth; `openapi/hello-api.yaml` is the contract.
2. **Tests** Write or update tests first. Each test's `@DisplayName` starts with its AC id (e.g. `AC-4:`).
3. **Commit** the failing tests before writing production code.
4. **Code**  Change the minimum code needed to make the tests pass.
5. **Trace** Update the traceability table in the spec.

Never weaken or delete a test to make it pass. If a test looks wrong, stop and
ask; the fix may belong in the spec.

## Commands

- `mvn -B test` — run all spec tests
- `mvn -B test -Dtest=HelloControllerTest` — one class
- `make build load deploy verify` — full local run on kind (needs Docker, kind, kubectl)
- `make logs` — follow the sidecar output

## Layout

- `src/main/java/com/example/hello/` — controller → `GreetingService` (all rules) → `ApiExceptionHandler`
- `k8s/` — Kustomize; `deployment.yaml` holds both containers
- `kind-config.yaml` — maps node `/var/log/hello-app` to `./node-logs`

## Conventions

- Business rules live in `GreetingService`, not the controller.
- Errors are RFC 9457 `ProblemDetail` with `application/problem+json`.
- Never log raw user input that failed validation (AC-7).
- Log through SLF4J only; log file location comes from `LOGGING_FILE_NAME`, never hard-coded.
- Containers: non-root where possible, read-only root FS, drop ALL capabilities.

## Guardrails

- Only use the `kind-hello` kube context. Never run kubectl against any other context.
- Do not `kubectl delete` anything outside namespace `hello`.
