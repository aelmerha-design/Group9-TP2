# Contributor tests and Javadoc

This folder contains a story-derived requirements-to-tests matrix, executable contributor tests, manual procedures, generated Javadoc, execution logs, and actual JavaFX screenshots.

- [Requirements-to-tests list](RequirementsToTests.md)
- [All-TP2 submission inspection checklist](SubmissionChecks.md)
- [Integration fixes and scope](IntegrationNotes.md)
- [Manual tests and visual review](ManualTests.md)
- [Recorded results and evidence](TestResults.md)
- [Commented test source screenshots](SourceScreenshots.md)
- [Requirement-to-HTML-and-screenshot filename list](StoryEvidence.md)
- [Generated test Javadoc](javadoc/index.html)
- [Test runner Javadoc](javadoc/FoundationsF26/guiLessons/LessonTestingAutomation.html)

## Reproduce the semi-automated tests

Use Windows, a graphical desktop, JDK 24, JavaFX 24.0.2 for Windows, and H2 2.3.232. These are the versions used for the recorded run. No JUnit or IDE configuration is required. JavaFX windows briefly open during the run; do not interact with them.

From the repository root, download the fixed dependencies once (or supply an existing directory with the same filenames using `-LibraryPath`):

```powershell
$library = 'ContributorTests_Javadocs/.work/lib'
New-Item -ItemType Directory -Force $library | Out-Null
foreach ($component in @('base', 'graphics', 'controls', 'fxml')) {
    Invoke-WebRequest -UseBasicParsing "https://repo.maven.apache.org/maven2/org/openjfx/javafx-$component/24.0.2/javafx-$component-24.0.2-win.jar" -OutFile "$library/javafx-$component.jar"
}
Invoke-WebRequest -UseBasicParsing 'https://repo.maven.apache.org/maven2/com/h2database/h2/2.3.232/h2-2.3.232.jar' -OutFile "$library/h2.jar"
./ContributorTests_Javadocs/Run-Tests.ps1 -JdkHome 'C:/Program Files/Java/jdk-24'
```

The script compiles all application sources, launches two separate JVMs, checks their exit codes, confirms the restart marker was consumed, and generates Javadoc for all four test classes. It stops on a failed phase. `results/first-run.txt` and `results/restart-run.txt` retain individual assertions and totals; `results/compile.txt`, `results/javadoc.txt`, and `results/environment.txt` record the build and environment.

After a successful test run, regenerate the required screenshots of the commented test code and Javadoc:

```powershell
./ContributorTests_Javadocs/New-SourceEvidence.ps1
```

This uses installed Chrome in headless mode; supply `-BrowserPath` for another Chrome or Edge executable. Source pages show all lines of the four test classes, original line numbers, and the source SHA-256. The generated `SourceScreenshots.md` maps the case comments to descriptively named PNGs. The original Java sources remain in `HW2 2/src/guiLessons`. Javadoc includes its own linked source HTML. Regenerate evidence after editing test code so the HTML, screenshots, and hashes agree.

Every full run uses a new `.work/database-<timestamp>` directory. `user.home` is changed **before** H2 is loaded, so `jdbc:h2:~/TP2Database` points into that directory. Only test-owned data is removed. Dependencies, compiled classes, and database files are ignored by Git. The generated documentation, logs, and PNG evidence are deliverables and remain tracked. A first execution alone does not establish restart persistence.

## Manual execution

```powershell
./ContributorTests_Javadocs/Run-Tests.ps1 -Manual -JdkHome 'C:/Program Files/Java/jdk-24'
```

For role/lock tests, close and relaunch with `-Manual -Actor owner`, `-Manual -Actor other`, or `-Manual -Actor admin`. These select test identities 101, 102, and 999; they do not test authentication. Use `-ManualHome <path>` to override the manual database location; `-Actor regression` selects owner zero for the legacy large-list database.

Follow [ManualTests.md](ManualTests.md). This launcher uses `.work/manual-database`, independently of the automated fixture, and retains data between launches. Press F12 on the lesson page to save `screenshots/manual-<timestamp>.png`. Close the window to end the session. A normal screenshot tool is needed to capture modal dialogs. Back opens a labeled return page for navigation verification.

## Evidence and scope

The five numbered lesson PNGs are snapshots of real rendered JavaFX scenes or dialog panes, captured during the tests. Browser screenshots show the generated Javadoc. Screenshots establish visible state; assertion logs establish counts and stored values. A scene snapshot does not establish keyboard accessibility, desktop scaling behavior, or successful human clicks. Manual visual reviews and human interaction procedures have separate recorded statuses.

The primary matrix now derives from the supplied Contributor stories. Named story tests challenge individual read/update/delete IDs, effective locks, restricted metadata, repeated experiences, and persistence across JVMs. Authentication, account administration, concurrent actors, actual AI integrations, and curator-specific UI are outside this suite. The team must still confirm any additional Ed Discussion or stakeholder requirements. Case 20 closes the isolated connection to check database failure handling.
