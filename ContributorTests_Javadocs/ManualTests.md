# Manual test procedures and visual review

## Completed visual review

Executor: Codex, 2026-10-27, Windows, JDK 24.0.2 / JavaFX 24.0.2. The following checks were performed by opening and visually inspecting the actual PNGs produced by the executed tests. These are manual visual checks, separate from the programmatic assertions.

| ID | Procedure | Expected and observed result | Status / evidence |
| --- | --- | --- | --- |
| MV-01 | Inspect the created/read scene | Two distinct list entries; first selected; four fields show the shared sample input; labels and CRUD buttons readable | PASS — [01](screenshots/01-created-and-read.png) |
| MV-02 | Inspect the validation dialog | Error icon, complete title-length explanation, readable OK button | PASS — [02](screenshots/02-validation-error.png) |
| MV-03 | Inspect the empty-list scene | Centered `No Lessons` placeholder and blank form; action buttons remain visible | PASS — [03](screenshots/03-empty-list.png) |
| MV-04 | Inspect the large-subset scene | Generated lesson rows and vertical scrollbar visible; form cleared after subset removal | PASS — [04](screenshots/04-large-subset.png) |
| MV-05 | Inspect the scene captured in the second JVM | Selected entry and title field show `Saved after restart`; problem and solution restored; experience blank | PASS — [05](screenshots/05-after-restart.png) |

The image cannot prove that all 999 rows exist; the database/UI count assertions in the logs supply that evidence.

## Human interaction checklist

Launch `Run-Tests.ps1 -Manual` as described in README.md. Start with a new manual database for MT-01–05; if reusing one, remove previous test lessons through the UI. Use the shared inputs in RequirementsToTests.md. Record date, tester, observed result, and screenshot filename after each procedure. The checked-in status for MT-01–13 is **NOT RUN by a human**; the equivalent controller and button behavior was exercised by the semi-automated suite. Do not report these procedures as human passes based on automated logs.

| ID / cases | Steps | Expected result |
| --- | --- | --- |
| MT-01 / 1, 2, 18 | Type the sample fields; click Create. Create a second lesson titled `Organizing diagrams`. Select the first and click Read. Press F12. | Two distinct IDs; all four original fields restored; selection is visible. |
| MT-02 / 3, 4, 13, 18 | Select the first lesson; edit title/problem/solution; click Update; reselect. Replace experience and save; then clear experience and save again. | Same ID and edited values on reread; second lesson unchanged; empty experience accepted. |
| MT-03 / 5, 8, 18 | Select the second lesson and Delete; verify the first remains. Delete the first. Press F12. | Only selected records disappear; final list shows `No Lessons`; form clears. |
| MT-04 / 9, 18 | Create three lessons. Ctrl-click two rows; click Show selected. Select one; click Remove from subset. Click Show all. | Two, then one, then three visible entries; removal did not delete saved data; form clears on subset change. |
| MT-05 / 10–15 | Try Create with each required field empty and spaces only. Try exact minimum/maximum lengths, then maximum+1 for each field. Try experience empty/2000/2001. On an existing lesson try invalid updates and reread after dismissing the dialog. With no selection try Read/Update/Delete. | Messages exactly match the matrix; valid limits save; invalid creates add nothing; invalid updates preserve every saved field; no selection yields the selection message. |
| MT-06 / 6 | Create two lessons. Update one to `Saved after restart`, delete the other, then close the window. Relaunch with the same manual database and read the survivor. | Saved ID and edited fields persist; deleted lesson remains absent. |
| MT-07 / 8, 9 | After the automated run, use `Run-Tests.ps1 -Manual -Actor regression -ManualHome '<database path from results/environment.txt>'`. Scroll first to last row; Ctrl-select nonadjacent rows; Show selected; remove one; Show all. | All 999 surviving rows can be reached without clipped controls; selected subset and restoration work. Do this after retaining the recorded automated evidence. |
| MT-08 / 18 | Click Back from a fresh manual session. | Window shows `Manual test return page` and title `Manual test home`. Close the window. |
| MT-09 / C02 | Create a distinctive lesson as `-Manual -Actor owner`; close. Relaunch as `-Manual -Actor other`. Close and return to owner. | Other contributor's list excludes the lesson; owner can select/read it. Direct guessed-ID denial is separately demonstrated by testForeignAccessDenied. |
| MT-10 / C04, C05, C10 | As admin, select the owner's lesson and click the Title Lock button. Relaunch as owner; inspect [locked] and try editing/deleting. As admin unlock Title, lock the whole lesson, then check again as owner. Finally unlock all locks and delete as owner. Repeat for Problem, Solution, and Experience locks. | Locked fields are noneditable with explicit labels; Delete gives the lock message and preserves the row. Unlocked fields can still be edited under a different field lock. After every lock is removed deletion succeeds. Contributor lock controls are hidden. |
| MT-11 / C06 | Inspect the named metadata privacy tests' actual execution, then inspect owner detail fields and model projection Javadoc. | Contributor fields expose core information and locks only. PRIVATE CURATOR TERMS and numeric metadata are omitted from reads/lists and preserved during core edits. There is no manual metadata editor; this is a review of the executable fixture evidence, not a claim of an unimplemented GUI. |
| MT-12 / C07 | Select an owned lesson. Type what/how/time/each-member effort in Experience and click Add experience twice with different text. Click Experience history. Try blank input and then lock Experience as admin before another append. | Both attempts appear in order; later appends preserve earlier ones. Blank input gives the validation message; locked append gives the lock error and preserves history. |
| MT-13 / C03 | As admin, edit an owner's lesson. Relaunch as owner and reread. Close, delete the lesson as admin, then relaunch as owner. | Permitted edited core content is visible under the same ID; after deletion the lesson is absent. |

Switch identities by closing the window and relaunching with `-Manual -Actor owner`, `other`, or `admin`. Keep the same ManualHome for these procedures. Owner=101, other=102, admin=999. These switches are fixture selection and do not test login.

For long inputs, PowerShell can generate text for pasting, for example `('T' * 100) | Set-Clipboard`. Keep other required fields valid while varying one field. Take native screenshots of dialogs before clicking OK. F12 captures only the lesson scene while it has focus.

The manual fixture bypasses login to isolate contributor controls. It does not establish login/role dispatch behavior. Keyboard-only navigation, different DPI scales, and assistive technology remain outside the completed visual review.

## Execution record to complete during the interactive run

| Procedure | Tester / date | Actual result | Status | Screenshot filename |
| --- | --- | --- | --- | --- |
| MT-01 | Pending | Pending | NOT RUN | Pending |
| MT-02 | Pending | Pending | NOT RUN | Pending |
| MT-03 | Pending | Pending | NOT RUN | Pending |
| MT-04 | Pending | Pending | NOT RUN | Pending |
| MT-05 | Pending | Pending | NOT RUN | Pending |
| MT-06 | Pending | Pending | NOT RUN | Pending |
| MT-07 | Pending | Pending | NOT RUN | Pending |
| MT-08 | Pending | Pending | NOT RUN | Pending |
| MT-09 | Pending | Pending | NOT RUN | Pending |
| MT-10 | Pending | Pending | NOT RUN | Pending |
| MT-11 | Pending | Pending | NOT RUN | Pending |
| MT-12 | Pending | Pending | NOT RUN | Pending |
| MT-13 | Pending | Pending | NOT RUN | Pending |

The [manual launcher Javadoc](javadoc/FoundationsF26/guiLessons/LessonManualTesting.html) documents how these procedures exercise Task 3 through Task 4. [SourceScreenshots.md](SourceScreenshots.md) includes the commented launcher source as well as the semi-automated test implementations.
