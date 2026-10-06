# hello-k8s-sidecar

A Spring Boot hello-world built **spec-first**, deployed to Kubernetes with a
**log sidecar** that writes the app's logs to a **local disk mount** on the node.
It also doubles as a worked example of the Claude Code practices from the
"Claude Code best practices" talk.

## How it works

```
 GET /api/v1/hello?name=Satya
            │
 ┌──────────▼──────────────── Pod ─────────────────────────────┐
 │  hello-app (Spring Boot)          log-shipper (busybox)     │
 │  writes /var/log/app/app.log      tail -F app.log | tee     │
 │            │                         ▲          │           │
 │            └──► emptyDir (shared) ───┘          │           │
 │                                                 ▼           │
 │                              hostPath /node-logs/<pod>.log  │
 └─────────────────────────────────────────────────┼───────────┘
                                                   ▼
                     node disk /var/log/hello-app  (= ./node-logs on kind)
```

- The app only knows "write logs to a file". The sidecar owns where they go.
- `emptyDir` is the hand-off point; it lives as long as the pod.
- `hostPath` is the local disk on the node; it survives pod restarts.
- The sidecar is a **native sidecar** (`initContainers` + `restartPolicy: Always`),
  so it starts before the app and stops after it — no lost lines.

## Quick start

Prerequisites: Java 21, Maven, Docker, [kind](https://kind.sigs.k8s.io), kubectl.

```bash
make test                         # 1. spec tests (AC-1..AC-8)
make kind-up                      # 2. local cluster, node disk mapped to ./node-logs
make load deploy                  # 3. build image, load into kind, deploy
make verify                       # 4. call the API and show lines on node disk
make logs                         #    follow the sidecar
make clean                        #    tear down
```

Expected `make verify` output (abridged):

```
{"message":"Hello, Satya!","timestamp":"2026-10-05T20:31:12.481Z"}
invalid name -> HTTP 400
--- files on node disk (./node-logs) ---
hello-app-7c9d8-abcde.log
hello-app-7c9d8-fghij.log
... INFO ... GreetingService : greeting served name=Satya
... WARN ... GreetingService : greeting rejected reason=invalid_name length=3
```

## Spec-driven development, step by step

| Step | What you do | Where |
|------|-------------|-------|
| 1. Spec | Describe behaviour as numbered acceptance criteria | `specs/001-hello-greeting.md` |
| 2. Contract | Machine-readable API | `openapi/hello-api.yaml` |
| 3. Tests | One test per AC, named `AC-n: ...`; confirm they fail | `src/test/java/...` |
| 4. Commit | Lock the tests in before code exists | git |
| 5. Code | Minimum code to go green | `src/main/java/...` |
| 6. Trace | Map every AC to its test | spec §6 |

To add a feature: write `specs/002-....md`, then in Claude Code run
`/implement-spec 002-....md`.

## How the talk's slides map to this repo

| Slide | In this project |
|-------|-----------------|
| Optimize your setup / `/allowed-tools` | `.claude/settings.json` — safe commands allowed, `kubectl apply` asks, context switching denied |
| Codebase Q&A first | Try: *"How does a request flow from @src/main/java/com/example/hello/HelloController.java to the log file on the node?"* |
| Tools get things done | Claude runs `mvn test`, `docker build`, `kubectl` itself and checks the results |
| Steer tool use | `/implement-spec` = propose → confirm → implement; add "think hard" for tricky changes |
| Plug in team tools | `make` targets are the team CLI; document new ones in CLAUDE.md |
| Common workflows | Write tests → commit → code → iterate → commit (exactly what `/implement-spec` does) |
| More context | `CLAUDE.md` (always loaded), `.claude/commands/` (on demand), `@specs/...` mentions |
| CLAUDE.md layers | Project `CLAUDE.md` is checked in; put personal notes in `CLAUDE.local.md` (gitignored) |

## Production notes

- **hostPath is for demos and node-local debugging.** In production, let a node
  agent (Fluent Bit, Vector, the platform log pipeline) ship from stdout or the
  node path, and swap busybox for Fluent Bit if the sidecar must parse or forward.
- The sidecar runs as root only to write to the hostPath. Pre-create the node
  directory with the right owner and you can make it non-root.
- Native sidecars need Kubernetes ≥ 1.29. On older clusters, move `log-shipper`
  into `containers:` and accept that startup/shutdown ordering is not guaranteed.
- `emptyDir.sizeLimit: 100Mi` covers 5 × 10 MB rotated files; the pod is evicted
  if exceeded, which is the behaviour you want rather than filling the node.
