package guiLessons;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
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
 * <p> Description: these test the documented HW2 cases using application methods and a separate test database. you have to run twice to check persistence. </p>
 * @author Ahmad El-Merhaby
 */

public class LessonTestingAutomation {

	static int numPassed = 0;
	static int numFailed = 0;
	private static File restartFile;
	private static String actualError;
	private static final String titleError = "Enter a title that is between 1-100 characters";
	private static final String problemError = "Enter a problem that is between 1-2000 characters";
	private static final String solutionError = "Enter a solution that is between 1-2000 characters";
	private static final String experienceError = "Experience has max 2000 characters";
	private static final String selectionError = "Select a lesson that exists";
	private static final String title = "Making 4 CRUD Diagrams";
	private static final String problem = "The homework asks for four diagrams with two main data classes.";
	private static final String solution = "Make a separate diagram for Create, Read, Update, and Delete.";
	private static final String experience = "A team member organized the classes in Astah in 20 minutes.";

	public static void main(String[] args) {
		// For H2 it uses user.home. which is a separate folder keeps tests away from real accounts/lessons
		File testFolder = new File(System.getProperty("user.home"), "HW2-Test-Database");
		if (!testFolder.isDirectory() && !testFolder.mkdirs()) {
			System.out.println("Could not create the test database folder.");
			return;
		}
		System.setProperty("user.home", testFolder.getAbsolutePath());
		restartFile = new File(testFolder, "restart-test.txt");
		Platform.startup(() -> new LessonTestingAutomation().start(new Stage()));
	}
	public void start(Stage stage) {
		try {
			FoundationsMain.database.connectToDatabase();
			User user = new User("HW2 Tests", "", "", "", "", "", "", false, false, false);
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
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			Platform.exit();
		}
	}
	private static void runTests() throws Exception {
		// Clear only the separate test database so list counts are repeatable.
		for (Lesson lesson : ModelLessons.readLessons().getLessons())
			ModelLessons.deleteLesson(lesson.getId());
		ViewLessons.refreshList();

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
		
		performTestCase(2, "Read all stored fields", formMatches(title, problem, solution, experience));
		fields("Updated diagrams", "Need four CRUD diagrams.", "Use one diagram per operation.", experience);
		ControllerLessons.updateLesson();
		Lesson updated = ModelLessons.readLesson(firstId);
		
		performTestCase(3, "Update fields, keep ID and other lesson",
				updated.getId() == firstId && updated.getTitle().equals("Updated diagrams")
				&& updated.getProblem().equals("Need four CRUD diagrams.")
				&& updated.getSolution().equals("Use one diagram per operation.")
				&& ModelLessons.readLesson(secondId).getTitle().equals("Organizing diagrams"));
		for (String text : new String[] {experience, "The diagrams took 30 minutes.", ""}) {
			select(firstId);
			ViewLessons.text_Experience.setText(text);
			ControllerLessons.updateLesson();
			performTestCase(4, "Save experience: " + (text.isEmpty() ? "blank" : text),
					ModelLessons.readLesson(firstId).getExperience().equals(text));
		}
		select(secondId);
		ControllerLessons.deleteLesson();
		
		performTestCase(5, "Delete selected lesson only",
				ModelLessons.readLesson(secondId) == null && ModelLessons.readLesson(firstId) != null
				&& ViewLessons.list_Lessons.getItems().size() == 1);

		LessonList list = new LessonList();
		list.addLesson(new Lesson(1, title, problem, solution, ""));
		list.addLesson(new Lesson(2, "Other lesson", problem, solution, ""));
		performTestCase(7, "Add and read a list entry", list.getLesson(1).getTitle().equals(title));
		performTestCase(7, "Update chosen list entry", list.updateLesson(new Lesson(1, "Changed", problem, solution, ""))
				&& list.getLesson(1).getTitle().equals("Changed") && list.getLesson(2).getTitle().equals("Other lesson"));
		performTestCase(7, "Remove chosen list entry", list.removeLesson(1)
				&& list.getLesson(1) == null && list.getLesson(2) != null);

		for (String blank : new String[] {"", "   "}) {
			testCreateError(10, blank, problem, solution, "", titleError);
			testCreateError(10, title, blank, solution, "", problemError);
			testCreateError(10, title, problem, blank, "", solutionError);
		}
		testValid(11, "A", "B", "C", "");
		testValid(11, "T".repeat(100), "P".repeat(2000), "S".repeat(2000), "");
		testCreateError(12, "T".repeat(101), problem, solution, "", titleError);
		testCreateError(12, title, "P".repeat(2001), solution, "", problemError);
		testCreateError(12, title, problem, "S".repeat(2001), "", solutionError);
		testValid(13, title, problem, solution, "");
		testValid(13, title, problem, solution, "E".repeat(2000));
		testCreateError(13, title, problem, solution, "E".repeat(2001), experienceError);

		// this will use the controller to prove rejected edits do not reach the saved record
		for (String badTitle : new String[] {"", "T".repeat(101)}) {
			select(firstId);
			Lesson before = ModelLessons.readLesson(firstId);
			ViewLessons.text_Title.setText(badTitle);
			testError(14, ControllerLessons::updateLesson, titleError);
			Lesson after = ModelLessons.readLesson(firstId);
			performTestCase(14, "Invalid edit preserves all saved fields", sameLesson(before, after));
		}
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
		for (Lesson lesson : ModelLessons.readLessons().getLessons())
			ModelLessons.deleteLesson(lesson.getId());
		ViewLessons.refreshList();
		performTestCase(8, "Empty list displays No lessons", ViewLessons.list_Lessons.getItems().isEmpty()
				&& ((Label) ViewLessons.list_Lessons.getPlaceholder()).getText().equals("No lessons"));
		ControllerLessons.showSelectedLessons();
		performTestCase(9, "Empty subset", ViewLessons.displayedLessons.getLessons().isEmpty());
		fields(title, problem, solution, "");
		ControllerLessons.createLesson();
		int savedId = ViewLessons.list_Lessons.getItems().get(0).getId();
		performTestCase(8, "List with one lesson", ModelLessons.readLessons().getLessons().size() == 1);
		select(savedId);
		ControllerLessons.showSelectedLessons();
		performTestCase(9, "Subset with one lesson", ViewLessons.displayedLessons.getLessons().size() == 1);
		select(savedId);
		ControllerLessons.removeFromSubset();
		performTestCase(9, "Subset removal preserves database record",
				ViewLessons.list_Lessons.getItems().isEmpty() && ModelLessons.readLesson(savedId) != null);
		// the generated variations of the sample test a large list without typing 1,000 forms
		for (int i = 1; i < 1000; i++)
			ModelLessons.createLesson(new Lesson(0, "CRUD diagram lesson " + i, problem, solution, ""));
		ViewLessons.refreshList();
		performTestCase(8, "All 1,000 entries can be read", ModelLessons.readLessons().getLessons().size() == 1000
				&& ViewLessons.list_Lessons.getItems().size() == 1000);
		ViewLessons.list_Lessons.getSelectionModel().selectAll();
		ControllerLessons.showSelectedLessons();
		performTestCase(9, "Subset with 1,000 lessons", ViewLessons.displayedLessons.getLessons().size() == 1000);
		select(savedId);
		ControllerLessons.removeFromSubset();
		performTestCase(9, "Large subset removal preserves database record",
				ViewLessons.list_Lessons.getItems().size() == 999 && ModelLessons.readLesson(savedId) != null);
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
	// this is a second execution that checks that the saved changes survive after restarting the app
	private static void testRestart() throws Exception {
		try (Scanner input = new Scanner(restartFile)) {
			int savedId = input.nextInt();
			int deletedId = input.nextInt();
			Lesson saved = ModelLessons.readLesson(savedId);
			performTestCase(6, "Created and edited lesson survives a separate execution", saved != null
					&& saved.getId() == savedId && saved.getTitle().equals("Saved after restart")
					&& saved.getProblem().equals(problem) && saved.getSolution().equals(solution)
					&& saved.getExperience().isEmpty());
			performTestCase(6, "Deleted lesson stays deleted", ModelLessons.readLesson(deletedId) == null);
			performTestCase(6, "All remaining lessons survive", ModelLessons.readLessons().getLessons().size() == 999);
		}
		if (!restartFile.delete()) System.out.println("Could not remove the restart test marker.");
	}
	private static void testValid(int number, String t, String p, String s, String e) throws Exception {
		int count = ModelLessons.readLessons().getLessons().size();
		fields(t, p, s, e);
		ControllerLessons.createLesson();
		Lesson saved = ViewLessons.list_Lessons.getItems().get(ViewLessons.list_Lessons.getItems().size() - 1);
		performTestCase(number, "Accept lengths " + t.length() + ", " + p.length() + ", " + s.length() + ", " + e.length(),
				ModelLessons.readLessons().getLessons().size() == count + 1
				&& saved.getTitle().equals(t) && saved.getProblem().equals(p)
				&& saved.getSolution().equals(s) && saved.getExperience().equals(e));
	}
	private static void testCreateError(int number, String t, String p, String s, String e, String message) throws Exception {
		int count = ModelLessons.readLessons().getLessons().size();
		fields(t, p, s, e);
		testError(number, ControllerLessons::createLesson, message);
		performTestCase(number, "Invalid create adds no record", ModelLessons.readLessons().getLessons().size() == count);
	}
	private static void testError(int number, Runnable action, String expected) {
		actualError = "";
		// showAndWait processes this queued action and it reads the real dialog before it dismisses it
		Platform.runLater(() -> {
			for (Window window : Window.getWindows().toArray(new Window[0])) {
				if (window.getScene() != null && window.getScene().getRoot() instanceof DialogPane) {
					DialogPane pane = (DialogPane) window.getScene().getRoot();
					actualError = pane.getContentText();
					pane.lookupButton(ButtonType.OK).fireEvent(new javafx.event.ActionEvent());
				}
			}
		});
		action.run();
		performTestCase(number, "Helpful error: " + expected, actualError.equals(expected));
		System.out.println("Actual error: " + actualError);
	}
	private static void select(int id) {
		ViewLessons.list_Lessons.getSelectionModel().clearSelection();
		for (Lesson lesson : ViewLessons.list_Lessons.getItems()) {
			if (lesson.getId() == id) {
				ViewLessons.list_Lessons.getSelectionModel().select(lesson);
				return;
			}
		}
	}
	private static void fields(String t, String p, String s, String e) {
		ViewLessons.text_Title.setText(t);
		ViewLessons.text_Problem.setText(p);
		ViewLessons.text_Solution.setText(s);
		ViewLessons.text_Experience.setText(e);
	}
	private static boolean formMatches(String t, String p, String s, String e) {
		return ViewLessons.text_Title.getText().equals(t) && ViewLessons.text_Problem.getText().equals(p)
				&& ViewLessons.text_Solution.getText().equals(s) && ViewLessons.text_Experience.getText().equals(e);
	}
	private static boolean sameLesson(Lesson a, Lesson b) {
		return a.getId() == b.getId() && a.getTitle().equals(b.getTitle()) && a.getProblem().equals(b.getProblem())
				&& a.getSolution().equals(b.getSolution()) && a.getExperience().equals(b.getExperience());
	}
	// this reports each result and counts the failures and successes when doing the test cases
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
}
