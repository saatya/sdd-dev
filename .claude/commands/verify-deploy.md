Verify the deployment acceptance criteria (AC-9 to AC-15) in @specs/001-hello-greeting.md
against the running kind cluster.

1. Confirm the current kube context is `kind-hello`. If not, stop.
2. Run `make verify`.
3. Check each AC and report PASS/FAIL with evidence:
   - AC-9/10: lines from the app appear in `node-logs/<pod-name>.log`
   - AC-11: `kubectl -n hello get pod -o jsonpath` shows log-shipper as an initContainer with restartPolicy Always
   - AC-12: one log file per pod name
   - AC-13: securityContext of both containers
   - AC-14: probes configured and pods Ready
4. Do not change any files. If something fails, propose a fix and wait for me.
