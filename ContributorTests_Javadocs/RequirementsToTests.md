# Requirements-to-tests list

Baseline: `Lesson`, `LessonList`, `ModelLessons`, `ControllerLessons`, and `ViewLessons` in `HW2 2/src`, plus the supplied Task 3/4 and testing instructions. Cases 1–15 retain the original runner's numbering. Cases 16–20 add contributor list, collection edge, button wiring, direct entity, and database failure coverage. Each case may contain multiple assertions; totals count assertions rather than matrix rows. All automated cases use the actual application classes. The separate Contributor User Stories and team-specific TP3/Ed Discussion insights still need team review against this matrix; they were not supplied with the assignment excerpts.

Every commented case has a source-screenshot location in [SourceScreenshots.md](SourceScreenshots.md). Shared helpers and complete source are included there as well as in the generated Javadoc's source links.

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
