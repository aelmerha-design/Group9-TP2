# Contributor test results

Executed on Windows with Java 24.0.2, JavaFX 24.0.2 (Windows), and H2 2.3.232. See [environment.txt](results/environment.txt) for the final run timestamp and isolated database path.

| Check | Observed result | Evidence |
| --- | --- | --- |
| Compile all application sources | PASS; one pre-existing module-name warning | [compile.txt](results/compile.txt) |
| First JVM, cases 1–5 and 7–20 | 99 assertions passed, 0 failed | [first-run.txt](results/first-run.txt) |
| Second JVM, case 6 | 3 assertions passed, 0 failed | [restart-run.txt](results/restart-run.txt) |
| Restart marker consumed | PASS; runner completed both processes | [environment.txt](results/environment.txt) |
| Generate private test Javadoc with doclint | PASS | [javadoc.txt](results/javadoc.txt), [index](javadoc/index.html) |
| Manual visual checks MV-01–05 | PASS after inspecting captured images | [ManualTests.md](ManualTests.md) |
| Human interaction checklist MT-01–08 | NOT RUN by a human; provided for repeatable execution | [ManualTests.md](ManualTests.md) |

Total executable assertions: **102 passed, 0 failed** across 20 requirement cases. Visual checks are reported separately and are not added to that total. Each phase returns a nonzero process exit status for an assertion failure or uncaught test/setup exception. Javadoc/compilation errors also stop the PowerShell runner.

## Screenshot evidence

| File | Meaning |
| --- | --- |
| [01-created-and-read.png](screenshots/01-created-and-read.png) | Real contributor view after creating two lessons and reading the first |
| [02-validation-error.png](screenshots/02-validation-error.png) | Actual modal dialog for an empty required title |
| [03-empty-list.png](screenshots/03-empty-list.png) | Empty list placeholder and cleared fields |
| [04-large-subset.png](screenshots/04-large-subset.png) | Scrollable generated list after removing one entry from a 1,000-entry subset |
| [05-after-restart.png](screenshots/05-after-restart.png) | Restored lesson selected in the second JVM |
| [06-test-javadoc.png](screenshots/06-test-javadoc.png) | Generated runner class documentation in a browser |
| [07-helper-javadoc.png](screenshots/07-helper-javadoc.png) | Generated evidence helper documentation in a browser |
| [08-manual-javadoc.png](screenshots/08-manual-javadoc.png) | Manual test launcher documentation and procedure purposes |
| [Commented source screenshots](SourceScreenshots.md) | Complete source of every test and helper, including the numbered case comments, with a case-to-screenshot index |

## Iterations and remaining review

1. Corrected the existing empty-placeholder test to match the view's `No Lessons` text. Expanded invalid-update checks to every field and added contributor and collection edge cases. The two-process run passed 75 assertions.
2. Added actual button-handler tests, including subset restoration and Back. The two-process run passed 83 assertions.
3. Applied the clarified Task 3/4 testing requirements: added numbered internal comments and direct entity-validation/persistence tests, then forced genuine database errors to check MVC feedback and recovery. The two-process run passed 102 assertions. Generated linked-source Javadoc and complete source screenshot evidence.

No new production defect was observed by these executed cases. The PowerShell runner was also corrected to retain native-tool warnings without treating them as terminating PowerShell errors; nonzero tool exit codes still stop execution.

Before team sign-off, execute and record the human interaction checklist and compare the separate Contributor User Stories and team-specific TP3/Ed Discussion insights with the matrix. Automated success does not supply those missing inputs or establish team agreement.

The test assertion for the empty placeholder originally expected `No lessons`; the application renders `No Lessons`. The assertion now matches the displayed text. No production behavior was changed for this mismatch.

The JDK emits a warning about the existing module name ending in digits. JavaFX's renderer also emits a `sun.misc.Unsafe` deprecation warning on this JDK. Neither warning caused a failed assertion or a documentation error; both are retained in the execution logs.

Coverage includes actual button handlers in case 18; most remaining GUI cases invoke controller methods directly and automatically dismiss inspected dialogs. The bulk fixture uses model inserts. The manual visual review establishes appearance at the captured 800×600 scene size. It does not establish a human interaction pass, performance targets, or authorization enforcement for arbitrary lesson IDs.
