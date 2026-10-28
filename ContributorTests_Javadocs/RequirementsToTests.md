# Requirements-to-tests list

Source: the supplied **Contributor (Phase Two)** story and **Contributor (Phase Three)** AI-writing epic, together with Tasks 3, 4, 6, and 7. Core information is interpreted here as title/problem/solution, matching the supplied Task 3/4 code; confirm any stakeholder-specific changes before team sign-off. Experience records capture what/how/time/individual team effort as narrative text. Admin is the other-role test fixture; a curator-specific role and actual Phase Three AI are not implemented.

## Primary story acceptance matrix (Task 7.1-7.3)

Every row names at least two executable scenarios. Both links name the exact documenting file, **ContributorStoryTests.html**, and its individual test-method anchor. Each method's Javadoc identifies the requirement, the implementation strategy, and the conditions for Success. Negative tests pass when rejection occurs and saved data is preserved. C03 uses edit and deletion as its two distinct scenarios; not every requirement requires invalid input.

| Requirement | Acceptance criterion | Named positive / first test | Named negative / second test | Manual procedure |
| --- | --- | --- | --- | --- |
| C01 | Add valid core information under the contributor's ownership | [testCreateValid](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testCreateValid()) | [testCreateInvalid](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testCreateInvalid()) | MT-01, MT-05 |
| C02 | List and examine only the contributor's lessons; deny foreign IDs | [testOwnListAndRead](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testOwnListAndRead()) | [testForeignAccessDenied](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testForeignAccessDenied()) | MT-09 |
| C03 | Reflect permitted other-role edits and deletions of contributed content | [testOtherRoleEditVisible](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testOtherRoleEditVisible()) | [testOtherRoleDeletionVisible](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testOtherRoleDeletionVisible()) | MT-13 |
| C04 | Show field/whole locks and prohibit locked-field edits or contributor lock changes | [testUnlockedEdit](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testUnlockedEdit()) | [testLockedEditsAndIndicators](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testLockedEditsAndIndicators()) | MT-10 |
| C05 | Permit unlocked deletion; prohibit deletion with any active lock | [testUnlockedDelete](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testUnlockedDelete()) | [testLockedDeletionDenied](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testLockedDeletionDenied()) | MT-10 |
| C06 | Hide and preserve other-role additions while allowing core edits | [testMetadataHidden](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testMetadataHidden()) | [testMetadataPreserved](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testMetadataPreserved()) | MT-11 |
| C07 | Append what/how/time/team-effort experiences after repeated attempts; preserve history | [testExperienceAppend](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testExperienceAppend()) | [testExperienceRejected](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testExperienceRejected()) | MT-12 |
| C08 | Persist core content, locks, privacy, and experience history across executions | [testRestartRestoresState](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testRestartRestoresState()) | [testRestartKeepsRestrictions](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testRestartKeepsRestrictions()) | MT-06, MT-10 |
| C09 | Support lesson collection operations and non-destructive keyword subsets | [testListOperations](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testListOperations()) | [testListEdges](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testListEdges()) | MT-04, MT-07 |
| C10 | Privileged bulk lock changes preserve individual locks and roll back failures | [testBatchLocks](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testBatchLocks()) | [testBatchRollback](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testBatchRollback()) | MT-10 |
| C11 | Validate required core fields and optional experience at defined boundaries | [testBoundaryAccepted](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testBoundaryAccepted()) | [testBoundaryRejected](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testBoundaryRejected()) | MT-05 |
| C12 | Accommodate Phase Three search/time/effort/visibility attributes without exposing restricted additions | [testRestartRestoresState](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testRestartRestoresState()) | [testRestartKeepsRestrictions](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testRestartKeepsRestrictions()) | Design review; C06 privacy checks |

Additional C07 scenarios are [testExperienceHistoryButton](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testExperienceHistoryButton()) and [testExperienceForeignOrLocked](javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#testExperienceForeignOrLocked()). They check actual GUI button bindings and denied history access/append.

## HTML and screenshot filenames

[StoryEvidence.md](StoryEvidence.md) lists the exact HTML filename and all source PNG filenames for each requirement's two named tests. This is the direct Task 7.1/9.3 evidence list.

- All primary tests: `javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html`.
- All regression cases below: `javadoc/FoundationsF26/guiLessons/LessonTestingAutomation.html`.
- Manual fixture and MT procedures: `javadoc/FoundationsF26/guiLessons/LessonManualTesting.html`.
- Screenshot implementation: `javadoc/FoundationsF26/guiLessons/LessonTestEvidence.html`.
- **[SourceScreenshots.md](SourceScreenshots.md)** explicitly lists the PNG and source-HTML filenames for each named method and each legacy case. Continue across adjacent pages for methods crossing a boundary.
- C04 visible lock state: `screenshots/09-contributor-locked-fields.png`; C07 history: `screenshots/10-experience-history.png`.
- Other application-state evidence: `01-created-and-read.png` through `05-after-restart.png` in screenshots.
- Javadoc evidence: `06-test-javadoc.png`, `07-helper-javadoc.png`, `08-manual-javadoc.png`, and `11-story-tests-javadoc.png`.

## Phase scope and all-TP2 inventory

C01-C08 are the Phase Two story acceptance requirements. C09-C11 also exercise the assignment's collection, validation, and failure-handling responsibilities. C12 tests persistence and contributor restrictions for future attributes; it does not claim AI tools, curator search, or effort aggregation are implemented. [SubmissionChecks.md](SubmissionChecks.md) lists the other TP2 deliverable requirements separately, with two manual inspections per item; these are not Java application acceptance tests.

## Supporting regression cases

| Case | Requirement / acceptance criterion | Setup and test input | Expected result / oracle | Execution and manual reference |
| --- | --- | --- | --- | --- |
| 1 | Create distinct persistent lessons | Create the sample lesson and `Organizing diagrams` | Two list entries with different generated IDs | `runTests`; MT-01 |
| 2 | Read every stored field | Select sample ID and invoke Read | Title, problem, solution, experience equal saved input | `runTests`, `formMatches`; MT-01; screenshot 01 |
| 3 | Update the selected lesson | Change title, problem, solution | Same ID, new field values, other lesson unchanged | `runTests`; MT-02 |
| 4 | Add, replace, and remove optional experience | Save initial experience, replacement, then empty text | Exact stored experience matches each edit | `runTests`; MT-02 |
| 5 | Delete only the selected lesson | Select second ID and delete | Deleted ID absent; first ID remains; list count drops | `runTests`; MT-03 |
| 6 | Persist create, update, and delete across restart | Record saved/deleted IDs; close first JVM; launch second | Edited fields and ID survive; deleted ID absent; 999 records remain | `testRestart`; MT-06; screenshot 05 |
| 7 | Support collection CRUD | Add IDs 1 and 2, replace 1, remove 1 | Intended entry changes; entry 2 is unchanged | `runTests`; programmatic collection test |
| 8 | Handle empty, single, and large lists | Load 0, 1, and 1,000 lessons | Exact model/UI counts; empty placeholder `No Lessons` | `runTests`; MT-03, MT-07; screenshots 03–04 |
| 9 | Produce and remove from subsets | Select 0, 1, and 1,000 lessons; remove an entry | Correct subset sizes; removal preserves saved lesson | `runTests`; MT-04; screenshot 04 |
| 10 | Reject blank required fields | Create with empty and space-only title/problem/solution | Exact field-specific error; database count unchanged | `testCreateError`, `testError`; MT-05; screenshot 02 |
| 11 | Accept required-field boundaries | Lengths 1/1/1 and 100/2000/2000 | One record added; all saved text matches | `testValid`; MT-05 |
| 12 | Reject required fields above maximum | Lengths 101 title, 2001 problem, 2001 solution | Exact field-specific error; no record added | `testCreateError`; MT-05 |
| 13 | Validate optional experience | Empty, 2000 characters, 2001 characters | First two accepted exactly; last rejected with no insert | `testValid`, `testCreateError`; MT-02, MT-05 |
| 14 | Rejected edits preserve saved records | Update with empty/overlong required fields and overlong experience | Correct dialog; ID, owner, and every saved field unchanged | `testError`, `sameLesson`; MT-05 |
| 15 | Reject missing or stale selections | Read/Update/Delete with no selection, then with an externally deleted selected ID | `Select a lesson that exists` for all six actions | `testError`; MT-05; stale fixture constructed programmatically |
| 16 | List only the active contributor's lessons | Insert another owner's lesson using owner 42 | Active list has 1000 own entries; excludes foreign ID; other owner's list has one | `testContributorLists`; programmatic fixture |
| 17 | Preserve collection identity and keyword behavior | Duplicate/missing IDs; clear returned copy; search title/problem/solution/experience; blank/null/missing keyword | Duplicate ignored; missing operations null/false; copy changes isolated; correct subsets; original list unchanged | `testListSearch`; programmatic collection test |
| 18 | Wire visible buttons to intended actions | Fire Create/Read/Update/Show selected/Remove/Show all/Delete/Back buttons | Eight assertions verify saved state, form reset, subset restoration, deletion, home scene/title | `testButtons`, `fireButton`; MT-01–04, MT-08 |
| 19 | Task 3 entities independently validate and persist data | Direct invalid creates, null/trim normalization, invalid and valid updates, owner/all-list reads, delete twice | Invalid writes preserve storage; valid normalized fields and generated ID round-trip; persistent list APIs contain the row; deleted row absent | `testEntityPersistence`; source/Javadoc evidence |
| 20 | Task 4 handles database failures with useful feedback and recovers | Close isolated connection; invoke create/read/update/delete/list reload; reconnect | Five exact operation-specific dialogs; visible list preserved; saved fields and list restored after reconnect | `testDatabaseErrors`, `testError`; programmatic fault fixture |

## Shared input

- Title: `Making 4 CRUD Diagrams`
- Problem: `The homework asks for four diagrams with two main data classes.`
- Solution: `Make a separate diagram for Create, Read, Update, and Delete.`
- Experience: `A team member organized the classes in Astah in 20 minutes.`

Lengths use Java `String.length()` after the entity constructor trims text. Boundary strings repeat `T`, `P`, `S`, or `E` for the relevant field. Generated large-list titles are `CRUD diagram lesson 1` through `CRUD diagram lesson 999`. Case 18 runs before case 1 and consumes an ID, so screenshots need not begin at ID 1. Case 6 records IDs rather than assuming consecutive values.

## Exact error text

| Invalid input | Expected content |
| --- | --- |
| Title | `Enter a title that is between 1-100 characters` |
| Problem | `Enter a problem that is between 1-2000 characters` |
| Solution | `Enter a solution that is between 1-2000 characters` |
| Experience | `Experience has max 2000 characters` |
| Missing/stale selection | `Select a lesson that exists` |
