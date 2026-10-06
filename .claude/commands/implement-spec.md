Implement the change described in spec file @specs/$ARGUMENTS using our spec-driven workflow.

1. Read the spec and `openapi/hello-api.yaml`. List every acceptance criterion
   (AC-n) that is new or changed, and any conflict between the spec and the contract.
   If anything is ambiguous, stop and ask me before continuing.
2. Write or update tests so each AC has a test whose `@DisplayName` starts with its id.
   Run `mvn -B test` and confirm the new tests FAIL for the right reason.
3. Commit the tests alone: `test: AC-x..y for <spec name>`.
4. Write the minimum production code to make the tests pass. Re-run `mvn -B test`
   until everything is green. Do not modify the tests committed in step 3.
5. Update the spec's traceability table.
6. Summarise: ACs covered, files changed, anything deferred.
