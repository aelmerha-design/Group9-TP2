package guiLessons;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.nio.file.Path;
import applicationMain.FoundationsMain;
import entityClasses.Lesson;
import entityClasses.LessonList;
import entityClasses.User;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;

/*******
 * <p> Title: LessonTestingAutomation Class. </p>
 * <p>Runs contributor CRUD, validation, list, subset, and restart tests through
 * the real JavaFX controller and H2 database. Case numbers correspond to the
 * requirements matrix in ContributorTests_Javadocs/RequirementsToTests.md.</p>
 * <p>Run twice in separate JVMs: the first execution writes a restart marker;
 * the second verifies persistence and removes that marker. Set
 * {@code contributor.test.home} to an isolated directory and optionally set
 * {@code contributor.test.evidence} to save PNG snapshots. Exit status is zero
 * only when every assertion in that execution passes. UI actions run on the
 * JavaFX Application Thread; this is not a headless test.</p>
 * <p>Task 3 is checked through entity validation, persistent lesson CRUD, and
 * collection operations. Task 4 is checked through the real MVC form, dialogs,
 * buttons, and subset display. As in PasswordEvaluationTestingAutomation,
 * each assertion reports its expected behavior and a pass/fail counter.</p>
 * @author Ahmad El-Merhaby
 */

public class LessonTestingAutomation {

	/** Number of successful assertions in this JVM, following the Foundations testbed. */
	static int numPassed = 0;
	/** Number of failed assertions or unexpected test errors in this JVM. */
	static int numFailed = 0;
	/** IDs shared between separate executions to establish real disk persistence. */
	private static File restartFile;
	/** Text read from the real dialog rather than inferred from a validation method. */
	private static String actualError;
	/** Contributor identity used to isolate the fixture's owner-filtered list. */
	private static int testOwnerId;
	/** Optional output directory for visible application evidence. */
	private static Path evidenceDirectory;
	/** Prevents later dialogs from overwriting this execution's first error screenshot. */
	private static boolean errorCaptured;

	/** Creates a runner; initialization happens in {@link #start(Stage)}. */
	public LessonTestingAutomation() { }
	private static final String titleError = "Enter a title that is between 1-100 characters";
	private static final String problemError = "Enter a problem that is between 1-2000 characters";
	private static final String solutionError = "Enter a solution that is between 1-2000 characters";
	private static final String experienceError = "Experience has max 2000 characters";
	private static final String selectionError = "Select a lesson that exists";
	private static final String title = "Making 4 CRUD Diagrams";
	private static final String problem = "The homework asks for four diagrams with two main data classes.";
	private static final String solution = "Make a separate diagram for Create, Read, Update, and Delete.";
	private static final String experience = "A team member organized the classes in Astah in 20 minutes.";

	/**
	 * Starts JavaFX after isolating H2 storage from normal application data.
	 * @param args unused command-line arguments; configuration uses system properties
	 */
	public static void main(String[] args) {
		// For H2 it uses user.home. which is a separate folder keeps tests away from real accounts/lessons
		File testFolder = new File(System.getProperty("contributor.test.home",
				new File(System.getProperty("user.home"), "HW2-Test-Database").getPath()));
		if (!testFolder.isDirectory() && !testFolder.mkdirs()) {
			System.out.println("Could not create the test database folder.");
			System.exit(1);
		}
		System.setProperty("user.home", testFolder.getAbsolutePath());
		String evidence = System.getProperty("contributor.test.evidence");
		if (evidence != null) evidenceDirectory = Path.of(evidence);
		restartFile = new File(testFolder, "restart-test.txt");
		Platform.startup(() -> new LessonTestingAutomation().start(new Stage()));
	}
	/**
	 * Opens the real lesson view and schedules the appropriate test phase.
	 * @param stage the JavaFX stage used by the tests
	 */
	public void start(Stage stage) {
		try {
			FoundationsMain.database.connectToDatabase();
			User user = new User("HW2 Tests", "", "", "", "", "", "", false, false, false, 0);
			testOwnerId = user.getUserId();
			stage.setScene(new javafx.scene.Scene(new javafx.scene.layout.Pane(), 800, 600));
			stage.setTitle("Contributor test home");
			ViewLessons.displayLessons(stage, user);
			// Run after JavaFX starts so error dialogs can be opened and checked.
			Platform.runLater(() -> {
				try {
					System.out.println("\nHW2 Lesson Testing Automation");
					if (restartFile.exists()) testRestart();
					else runTests();
				} catch (Exception e) {
					numFailed++;
					e.printStackTrace();
				} finally {
					System.out.println("\nNumber of tests passed: " + numPassed);
					System.out.println("Number of tests failed: " + numFailed);
					FoundationsMain.database.closeConnection();
					Platform.exit();
					System.exit(numFailed == 0 ? 0 : 1);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			Platform.exit();
			System.exit(1);
		}
	}
	/**
	 * Exercises cases 1-5 and 7-20, then saves case 6's restart fixture.
	 * @throws Exception if a database, evidence, or marker operation fails
	 */
	private static void runTests() throws Exception {
		// Clear only the separate test database so list counts are repeatable.
		for (Lesson lesson : ModelLessons.readLessons(testOwnerId).getLessons())
			ModelLessons.deleteLesson(lesson.getId());
		ViewLessons.refreshList();

		testButtons();
		// CASE 01: A contributor can create multiple lessons with distinct persistent IDs.
		fields(title, problem, solution, experience);
		ControllerLessons.createLesson();
		int firstId = ViewLessons.list_Lessons.getItems().get(0).getId();
		fields("Organizing diagrams", problem, solution, "");
		ControllerLessons.createLesson();
		int secondId = ViewLessons.list_Lessons.getItems().get(1).getId();
		
		performTestCase(1, "Create two lessons with different IDs",
				firstId != secondId && ViewLessons.list_Lessons.getItems().size() == 2);
		select(firstId);
		ControllerLessons.readLesson();
		
		// CASE 02: Reading must restore all four stored fields, not just the list title.
		performTestCase(2, "Read all stored fields", formMatches(title, problem, solution, experience));
		capture("01-created-and-read");
		// CASE 03: Editing one lesson must retain its identity and preserve its neighbor.
		fields("Updated diagrams", "Need four CRUD diagrams.", "Use one diagram per operation.", experience);
		ControllerLessons.updateLesson();
		Lesson updated = ModelLessons.readLesson(firstId);
		
		performTestCase(3, "Update fields, keep ID and other lesson",
				updated.getId() == firstId && updated.getTitle().equals("Updated diagrams")
				&& updated.getProblem().equals("Need four CRUD diagrams.")
				&& updated.getSolution().equals("Use one diagram per operation.")
				&& ModelLessons.readLesson(secondId).getTitle().equals("Organizing diagrams"));
		// CASE 04: Optional experience can be added, replaced, and cleared independently.
		for (String text : new String[] {experience, "The diagrams took 30 minutes.", ""}) {
			select(firstId);
			ViewLessons.text_Experience.setText(text);
			ControllerLessons.updateLesson();
			performTestCase(4, "Save experience: " + (text.isEmpty() ? "blank" : text),
					ModelLessons.readLesson(firstId).getExperience().equals(text));
		}
		// CASE 05: Persistent deletion must affect only the chosen ID.
		select(secondId);
		ControllerLessons.deleteLesson();
		
		performTestCase(5, "Delete selected lesson only",
				ModelLessons.readLesson(secondId) == null && ModelLessons.readLesson(firstId) != null
				&& ViewLessons.list_Lessons.getItems().size() == 1);

		// CASE 07: Collection CRUD must preserve the entries that were not targeted.
		LessonList list = new LessonList();
		list.addLesson(new Lesson(1, testOwnerId, title, problem, solution, ""));
		list.addLesson(new Lesson(2, testOwnerId, "Other lesson", problem, solution, ""));
		performTestCase(7, "Add and read a list entry", list.getLesson(1).getTitle().equals(title));
		performTestCase(7, "Update chosen list entry", list.updateLesson(new Lesson(1, testOwnerId, "Changed", problem, solution, ""))
				&& list.getLesson(1).getTitle().equals("Changed") && list.getLesson(2).getTitle().equals("Other lesson"));
		performTestCase(7, "Remove chosen list entry", list.removeLesson(1)
				&& list.getLesson(1) == null && list.getLesson(2) != null);

		// CASE 10: Both empty and whitespace-only required fields must be rejected.
		for (String blank : new String[] {"", "   "}) {
			testCreateError(10, blank, problem, solution, "", titleError);
			testCreateError(10, title, blank, solution, "", problemError);
			testCreateError(10, title, problem, blank, "", solutionError);
		}
		// CASE 11: Exact lower and upper required-field boundaries are inclusive.
		testValid(11, "A", "B", "C", "");
		testValid(11, "T".repeat(100), "P".repeat(2000), "S".repeat(2000), "");
		// CASE 12: One character above each maximum must fail without inserting a row.
		testCreateError(12, "T".repeat(101), problem, solution, "", titleError);
		testCreateError(12, title, "P".repeat(2001), solution, "", problemError);
		testCreateError(12, title, problem, "S".repeat(2001), "", solutionError);
		// CASE 13: Experience is optional, but its nonblank content has a length limit.
		testValid(13, title, problem, solution, "");
		testValid(13, title, problem, solution, "E".repeat(2000));
		testCreateError(13, title, problem, solution, "E".repeat(2001), experienceError);

		// CASE 14: Compare full database snapshots to detect partial writes after rejection.
		for (String[] invalid : new String[][] {
				{"", problem, solution, "", titleError},
				{"T".repeat(101), problem, solution, "", titleError},
				{title, "", solution, "", problemError},
				{title, "P".repeat(2001), solution, "", problemError},
				{title, problem, "", "", solutionError},
				{title, problem, "S".repeat(2001), "", solutionError},
				{title, problem, solution, "E".repeat(2001), experienceError}}) {
			select(firstId);
			Lesson before = ModelLessons.readLesson(firstId);
			fields(invalid[0], invalid[1], invalid[2], invalid[3]);
			testError(14, ControllerLessons::updateLesson, invalid[4]);
			Lesson after = ModelLessons.readLesson(firstId);
			performTestCase(14, "Invalid edit preserves all saved fields", sameLesson(before, after));
		}
		// CASE 15: No selection and a stale selected ID must both yield a helpful error.
		ViewLessons.list_Lessons.getSelectionModel().clearSelection();
		testError(15, ControllerLessons::readLesson, selectionError);
		testError(15, ControllerLessons::updateLesson, selectionError);
		testError(15, ControllerLessons::deleteLesson, selectionError);
		// this keeps an entry displayed while deleting its record to reproduce a stale selection
		select(firstId);
		ModelLessons.deleteLesson(firstId);
		testError(15, ControllerLessons::readLesson, selectionError);
		testError(15, ControllerLessons::updateLesson, selectionError);
		testError(15, ControllerLessons::deleteLesson, selectionError);
		for (Lesson lesson : ModelLessons.readLessons(testOwnerId).getLessons())
			ModelLessons.deleteLesson(lesson.getId());
		ViewLessons.refreshList();
		// CASE 08: Empty, singleton, and generated large lists must have exact counts.
		performTestCase(8, "Empty list displays No lessons", ViewLessons.list_Lessons.getItems().isEmpty()
				&& ((Label) ViewLessons.list_Lessons.getPlaceholder()).getText().equals("No Lessons"));
		capture("03-empty-list");
		// CASE 09: A subset changes the display without deleting persistent lessons.
		ControllerLessons.showSelectedLessons();
		performTestCase(9, "Empty subset", ViewLessons.displayedLessons.getLessons().isEmpty());
		fields(title, problem, solution, "");
		ControllerLessons.createLesson();
		int savedId = ViewLessons.list_Lessons.getItems().get(0).getId();
		performTestCase(8, "List with one lesson", ModelLessons.readLessons(testOwnerId).getLessons().size() == 1);
		select(savedId);
		ControllerLessons.showSelectedLessons();
		performTestCase(9, "Subset with one lesson", ViewLessons.displayedLessons.getLessons().size() == 1);
		select(savedId);
		ControllerLessons.removeFromSubset();
		performTestCase(9, "Subset removal preserves database record",
				ViewLessons.list_Lessons.getItems().isEmpty() && ModelLessons.readLesson(savedId) != null);
		// the generated variations of the sample test a large list without typing 1,000 forms
		for (int i = 1; i < 1000; i++)
			ModelLessons.createLesson(new Lesson(0, testOwnerId, "CRUD diagram lesson " + i, problem, solution, ""));
		ViewLessons.refreshList();
		performTestCase(8, "All 1,000 entries can be read", ModelLessons.readLessons(testOwnerId).getLessons().size() == 1000
				&& ViewLessons.list_Lessons.getItems().size() == 1000);
		ViewLessons.list_Lessons.getSelectionModel().selectAll();
		ControllerLessons.showSelectedLessons();
		performTestCase(9, "Subset with 1,000 lessons", ViewLessons.displayedLessons.getLessons().size() == 1000);
		select(savedId);
		ControllerLessons.removeFromSubset();
		performTestCase(9, "Large subset removal preserves database record",
				ViewLessons.list_Lessons.getItems().size() == 999 && ModelLessons.readLesson(savedId) != null);
		capture("04-large-subset");
		testContributorLists();
		testListSearch();
		testEntityPersistence();
		testDatabaseErrors();
		ViewLessons.refreshList();
		select(savedId);
		ViewLessons.text_Title.setText("Saved after restart");
		ControllerLessons.updateLesson();
		int deletedId = ViewLessons.list_Lessons.getItems().get(999).getId();
		select(deletedId);
		ControllerLessons.deleteLesson();
		try (PrintWriter output = new PrintWriter(restartFile)) {
			output.println(savedId);
			output.println(deletedId);
		}
		System.out.println("\nTest 6: Run this class again to check a separate execution.");
	}
	/**
	 * Verifies case 6 in a fresh process using IDs recorded by the first execution.
	 * @throws Exception if database access or reading the marker fails
	 */
	private static void testRestart() throws Exception {
		// CASE 06: A new JVM must read saved data, not a previous process's cached list.
		try (Scanner input = new Scanner(restartFile)) {
			int savedId = input.nextInt();
			int deletedId = input.nextInt();
			Lesson saved = ModelLessons.readLesson(savedId);
			performTestCase(6, "Created and edited lesson survives a separate execution", saved != null
					&& saved.getId() == savedId && saved.getTitle().equals("Saved after restart")
					&& saved.getProblem().equals(problem) && saved.getSolution().equals(solution)
					&& saved.getExperience().isEmpty());
			performTestCase(6, "Deleted lesson stays deleted", ModelLessons.readLesson(deletedId) == null);
			performTestCase(6, "All remaining lessons survive", ModelLessons.readLessons(testOwnerId).getLessons().size() == 999);
			select(savedId);
			capture("05-after-restart");
		}
		if (!restartFile.delete()) System.out.println("Could not remove the restart test marker.");
	}
	/**
	 * Checks that a valid create stores every supplied field and adds one record.
	 * @param number requirement case number
	 * @param t title input
	 * @param p problem input
	 * @param s solution input
	 * @param e experience input
	 * @throws Exception if database access fails
	 */
	private static void testValid(int number, String t, String p, String s, String e) throws Exception {
		int count = ModelLessons.readLessons(testOwnerId).getLessons().size();
		fields(t, p, s, e);
		ControllerLessons.createLesson();
		Lesson saved = ViewLessons.list_Lessons.getItems().get(ViewLessons.list_Lessons.getItems().size() - 1);
		performTestCase(number, "Accept lengths " + t.length() + ", " + p.length() + ", " + s.length() + ", " + e.length(),
				ModelLessons.readLessons(testOwnerId).getLessons().size() == count + 1
				&& saved.getTitle().equals(t) && saved.getProblem().equals(p)
				&& saved.getSolution().equals(s) && saved.getExperience().equals(e));
	}
	/**
	 * Checks the actual validation dialog and verifies no record was added.
	 * @param number requirement case number
	 * @param t title input
	 * @param p problem input
	 * @param s solution input
	 * @param e experience input
	 * @param message expected dialog text
	 * @throws Exception if database access fails
	 */
	private static void testCreateError(int number, String t, String p, String s, String e, String message) throws Exception {
		int count = ModelLessons.readLessons(testOwnerId).getLessons().size();
		fields(t, p, s, e);
		testError(number, ControllerLessons::createLesson, message);
		performTestCase(number, "Invalid create adds no record", ModelLessons.readLessons(testOwnerId).getLessons().size() == count);
	}
	/**
	 * Reads and dismisses a real modal error dialog in its nested JavaFX event loop.
	 * @param number requirement case number
	 * @param action controller action expected to show an error
	 * @param expected exact expected error content
	 */
	private static void testError(int number, Runnable action, String expected) {
		actualError = "";
		// showAndWait processes this queued action and it reads the real dialog before it dismisses it
		Platform.runLater(() -> {
			for (Window window : Window.getWindows().toArray(new Window[0])) {
				if (window.getScene() != null && window.getScene().getRoot() instanceof DialogPane) {
					DialogPane pane = (DialogPane) window.getScene().getRoot();
					actualError = pane.getContentText();
					if (evidenceDirectory != null && !errorCaptured) {
						try {
							LessonTestEvidence.capture(pane, evidenceDirectory.resolve("02-validation-error.png"));
							errorCaptured = true;
						} catch (Exception failure) {
							numFailed++;
							failure.printStackTrace();
						}
					}
					pane.lookupButton(ButtonType.OK).fireEvent(new javafx.event.ActionEvent());
				}
			}
		});
		action.run();
		performTestCase(number, "Helpful error: " + expected, actualError.equals(expected));
		System.out.println("Actual error: " + actualError);
	}
	/** @param id ID to select in the currently displayed list */
	private static void select(int id) {
		ViewLessons.list_Lessons.getSelectionModel().clearSelection();
		for (Lesson lesson : ViewLessons.list_Lessons.getItems()) {
			if (lesson.getId() == id) {
				ViewLessons.list_Lessons.getSelectionModel().select(lesson);
				return;
			}
		}
	}
	/**
	 * Populates the form without saving it.
	 * @param t title input
	 * @param p problem input
	 * @param s solution input
	 * @param e experience input
	 */
	private static void fields(String t, String p, String s, String e) {
		ViewLessons.text_Title.setText(t);
		ViewLessons.text_Problem.setText(p);
		ViewLessons.text_Solution.setText(s);
		ViewLessons.text_Experience.setText(e);
	}
	/**
	 * Compares every visible field with expected stored text.
	 * @param t expected title
	 * @param p expected problem
	 * @param s expected solution
	 * @param e expected experience
	 * @return whether all four fields match
	 */
	private static boolean formMatches(String t, String p, String s, String e) {
		return ViewLessons.text_Title.getText().equals(t) && ViewLessons.text_Problem.getText().equals(p)
				&& ViewLessons.text_Solution.getText().equals(s) && ViewLessons.text_Experience.getText().equals(e);
	}
	/**
	 * Compares persisted identity, ownership, and text after a rejected edit.
	 * @param a snapshot before the attempted edit
	 * @param b snapshot after the attempted edit
	 * @return whether the complete stored record is unchanged
	 */
	private static boolean sameLesson(Lesson a, Lesson b) {
		return a != null && b != null && a.getOwnerId() == b.getOwnerId()
				&& a.getId() == b.getId() && a.getTitle().equals(b.getTitle()) && a.getProblem().equals(b.getProblem())
				&& a.getSolution().equals(b.getSolution()) && a.getExperience().equals(b.getExperience());
	}
	/**
	 * Reports one assertion; multiple assertions may belong to the same case.
	 * @param number requirement case number
	 * @param description assertion description
	 * @param passed whether the observed result met the expectation
	 */
	private static void performTestCase(int number, String description, boolean passed) {
		System.out.println("\nTest case " + number + ": " + description);
		if (passed) {
			System.out.println("Success");
			numPassed++;
		} else {
			System.out.println("Failure");
			numFailed++;
		}
	}

	/**
	 * Saves a rendered lesson scene when evidence output is enabled.
	 * @param name screenshot filename without its PNG extension
	 * @throws Exception if the screenshot cannot be written
	 */
	private static void capture(String name) throws Exception {
		if (evidenceDirectory != null)
			LessonTestEvidence.capture(ViewLessons.theStage.getScene().getRoot(), evidenceDirectory.resolve(name + ".png"));
	}

	/**
	 * Checks owner-filtered lists with an independently owned fixture (case 16).
	 * @throws Exception if database access fails
	 */
	private static void testContributorLists() throws Exception {
		// CASE 16: An independent owner fixture detects accidental unfiltered list reads.
		int otherOwnerId = 42;
		int otherId = ModelLessons.createLesson(new Lesson(0, otherOwnerId, "Other contributor", problem, solution, ""));
		ViewLessons.refreshList();
		performTestCase(16, "Contributor list excludes another owner's lesson",
				ViewLessons.list_Lessons.getItems().size() == 1000
				&& ViewLessons.list_Lessons.getItems().stream().noneMatch(lesson -> lesson.getId() == otherId)
				&& ModelLessons.readLessons(otherOwnerId).getLessons().size() == 1);
		ModelLessons.deleteLesson(otherId);
		ViewLessons.refreshList();
	}

	/** Checks duplicate IDs, missing IDs, defensive list copies, and keyword subsets (case 17). */
	private static void testListSearch() {
		// CASE 17: Exercise missing IDs, duplicate IDs, copy isolation, and all search fields.
		LessonList list = new LessonList();
		list.addLesson(new Lesson(1, testOwnerId, "Alpha", "ProblemToken", "SolutionToken", "ExperienceToken"));
		list.addLesson(new Lesson(1, testOwnerId, "Duplicate", "", "", ""));
		list.addLesson(new Lesson(2, testOwnerId, "Beta", "Other", "Other", ""));
		performTestCase(17, "Duplicate ID is ignored", list.getLessons().size() == 2 && list.getLesson(1).getTitle().equals("Alpha"));
		performTestCase(17, "Missing IDs return null or false", list.getLesson(99) == null
				&& !list.updateLesson(new Lesson(99, testOwnerId, "Absent", "", "", "")) && !list.removeLesson(99));
		list.getLessons().clear();
		performTestCase(17, "Changing returned list preserves original", list.getLessons().size() == 2);
		for (String keyword : new String[] {" ALPHA ", "problemtoken", "solutiontoken", "experiencetoken"}) {
			LessonList subset = list.searchByKeyword(keyword);
			performTestCase(17, "Keyword matches field: " + keyword,
					subset.getLessons().size() == 1 && subset.getLesson(1) != null && list.getLessons().size() == 2);
		}
		performTestCase(17, "Absent keyword yields empty subset", list.searchByKeyword("missing").getLessons().isEmpty());
		performTestCase(17, "Blank and null keywords match all", list.searchByKeyword("").getLessons().size() == 2
				&& list.searchByKeyword(null).getLessons().size() == 2);
	}

	/**
	 * Exercises actual button handlers, including subset restoration and Back (case 18).
	 * @throws Exception if database access fails
	 */
	private static void testButtons() throws Exception {
		// CASE 18: Fire installed button handlers so broken MVC bindings cannot pass silently.
		fields(title, problem, solution, experience);
		fireButton("Create");
		performTestCase(18, "Create button saves a lesson", ViewLessons.list_Lessons.getItems().size() == 1);
		int lessonId = ViewLessons.list_Lessons.getItems().get(0).getId();
		select(lessonId);
		fields("", "", "", "");
		fireButton("Read");
		performTestCase(18, "Read button restores every field", formMatches(title, problem, solution, experience));
		ViewLessons.text_Title.setText("Button update");
		fireButton("Update");
		performTestCase(18, "Update button saves the edited title", ModelLessons.readLesson(lessonId).getTitle().equals("Button update"));
		select(lessonId);
		fireButton("Show selected");
		performTestCase(18, "Show selected button clears the form", ViewLessons.list_Lessons.getItems().size() == 1
				&& formMatches("", "", "", ""));
		select(lessonId);
		fireButton("Remove from subset");
		performTestCase(18, "Remove button hides without deleting", ViewLessons.list_Lessons.getItems().isEmpty()
				&& ModelLessons.readLesson(lessonId) != null);
		fireButton("Show all");
		performTestCase(18, "Show all button restores the lesson", ViewLessons.list_Lessons.getItems().size() == 1);
		select(lessonId);
		fireButton("Delete");
		performTestCase(18, "Delete button removes the saved record", ModelLessons.readLesson(lessonId) == null
				&& ViewLessons.list_Lessons.getItems().isEmpty());
		javafx.scene.Scene lessonScene = ViewLessons.theStage.getScene();
		fireButton("Back");
		performTestCase(18, "Back button restores scene and title", ViewLessons.theStage.getScene() == ViewLessons.theHomeScene
				&& ViewLessons.theStage.getTitle().equals("Contributor test home"));
		ViewLessons.theStage.setScene(lessonScene);
		ViewLessons.theStage.setTitle("CSE 360: Lessons Learned");
	}

	/**
	 * Fires a visible button through its installed action handler.
	 * @param caption exact button text to find
	 * @throws IllegalStateException if the expected control is absent
	 */
	private static void fireButton(String caption) {
		ViewLessons.theStage.getScene().getRoot().applyCss();
		for (javafx.scene.Node node : ViewLessons.theStage.getScene().getRoot().lookupAll(".button")) {
			if (node instanceof javafx.scene.control.Button button && button.getText().equals(caption)) {
				button.fire();
				return;
			}
		}
		throw new IllegalStateException("Missing button: " + caption);
	}

	/**
	 * Checks Task 3's entity validation and persistence without going through the MVC controller.
	 * @throws Exception if the entity/database fixture fails
	 */
	private static void testEntityPersistence() throws Exception {
		// CASE 19: The entity itself must guard invalid writes, even without GUI validation.
		int originalCount = ModelLessons.readLessons(testOwnerId).getLessons().size();
		for (Lesson invalid : new Lesson[] {
				new Lesson(0, testOwnerId, null, problem, solution, null),
				new Lesson(0, testOwnerId, title, null, solution, ""),
				new Lesson(0, testOwnerId, title, problem, null, ""),
				new Lesson(0, testOwnerId, "T".repeat(101), problem, solution, ""),
				new Lesson(0, testOwnerId, title, "P".repeat(2001), solution, ""),
				new Lesson(0, testOwnerId, title, problem, "S".repeat(2001), ""),
				new Lesson(0, testOwnerId, title, problem, solution, "E".repeat(2001))}) {
			performTestCase(19, "Entity rejects invalid create without inserting",
					!invalid.isValid() && invalid.create(FoundationsMain.database) == -1
					&& ModelLessons.readLessons(testOwnerId).getLessons().size() == originalCount);
		}
		Lesson entity = new Lesson(0, testOwnerId, "  Entity title  ", " Problem ", " Solution ", null);
		int entityId = entity.create(FoundationsMain.database);
		Lesson stored = Lesson.read(FoundationsMain.database, entityId);
		performTestCase(19, "Entity create trims text, normalizes null experience, assigns ID",
				entityId > 0 && entity.getId() == entityId && stored != null
				&& stored.getTitle().equals("Entity title") && stored.getProblem().equals("Problem")
				&& stored.getSolution().equals("Solution") && stored.getExperience().isEmpty());
		entity.setTitle("   ");
		performTestCase(19, "Entity rejects invalid update and preserves stored snapshot",
				!entity.update(FoundationsMain.database) && sameLesson(stored, Lesson.read(FoundationsMain.database, entityId)));
		entity.setTitle(" Updated entity ");
		entity.setProblem(" Updated problem ");
		entity.setSolution(" Updated solution ");
		entity.setExperience(" Updated experience ");
		performTestCase(19, "Entity update persists normalized fields and unchanged identity",
				entity.update(FoundationsMain.database) && sameLesson(entity, Lesson.read(FoundationsMain.database, entityId)));
		LessonList ownLessons = LessonList.readLessons(FoundationsMain.database, testOwnerId);
		LessonList allLessons = LessonList.readAllLessons(FoundationsMain.database);
		performTestCase(19, "Persistent list APIs include the newly created entity",
				ownLessons.getLesson(entityId) != null && allLessons.getLesson(entityId) != null
				&& ownLessons.getLessons().size() == originalCount + 1);
		performTestCase(19, "Entity delete removes stored row and repeated delete returns false",
				entity.delete(FoundationsMain.database) && Lesson.read(FoundationsMain.database, entityId) == null
				&& !entity.delete(FoundationsMain.database));
		ViewLessons.refreshList();
	}

	/**
	 * Checks Task 4's helpful database error dialogs and recovery after reconnection.
	 * @throws Exception if preparing or restoring the database connection fails
	 */
	private static void testDatabaseErrors() throws Exception {
		// CASE 20: Close only the isolated connection to force genuine SQL failures.
		Lesson selected = ViewLessons.list_Lessons.getItems().get(0);
		select(selected.getId());
		int displayedCount = ViewLessons.list_Lessons.getItems().size();
		FoundationsMain.database.closeConnection();
		try {
			testError(20, ControllerLessons::createLesson, "Could not save the lesson. Please try again.");
			testError(20, ControllerLessons::readLesson, "Could not read the lesson. Please try again.");
			testError(20, ControllerLessons::updateLesson, "Could not update the lesson. Please try again.");
			testError(20, ControllerLessons::deleteLesson, "Could not delete the lesson. Please try again.");
			testError(20, ViewLessons::refreshList, "Could not load lessons. Please try again.");
			performTestCase(20, "Failed list read preserves visible rows", ViewLessons.list_Lessons.getItems().size() == displayedCount);
		} finally {
			FoundationsMain.database.connectToDatabase();
			ViewLessons.refreshList();
		}
		performTestCase(20, "Reconnection restores unchanged stored fields", sameLesson(selected, ModelLessons.readLesson(selected.getId()))
				&& ViewLessons.list_Lessons.getItems().size() == displayedCount);
	}
}
