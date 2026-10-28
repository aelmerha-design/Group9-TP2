package guiLessons;

import java.sql.SQLException;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import entityClasses.Lesson;
import entityClasses.LessonList;
import entityClasses.User;

/**
 * <p> Title: ViewLessons Class. </p>
 * 
 * <p> Description: Displays the Lessons Learned page using a lazily created, reusable view.
 * Presents lesson fields and a list supporting multiple selections, delegates
 * user actions to {@link ControllerLessons}, and loads saved lessons through
 * {@link ModelLessons}.
 *
 * Call UI methods on the JavaFX Application Thread. Opening the page saves
 * the current scene and title so the controller can restore them on Back. </p>
 *
 * @author Ahmad El-Merhaby
 */

public class ViewLessons {

	
	/** Page width taken from the application window settings. */
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	/** Page height taken from the application window settings. */
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;
	/** View instance created on the first call to {@link #displayLessons(Stage, User)}. */
	private static ViewLessons theView;
	/** Scene reused each time the lesson page is opened. */
	private static Scene theLessonsScene;
	/** Window currently displaying the lesson page. */
	protected static Stage theStage;
	/** Scene to restore when Back is selected. */
	protected static Scene theHomeScene;
	/** Window title to restore when Back is selected. */
	protected static String theHomeTitle;
	/** Currently displayed lessons, either the full loaded list or a selected subset. */
	protected static LessonList displayedLessons = new LessonList();
	/** Label identifying the user who opened the page. */
	protected static Label label_UserDetails = new Label();
	/** Input for the required lesson title. */
	protected static TextField text_Title = new TextField();
	/** Input for the required problem description. */
	protected static TextArea text_Problem = new TextArea();
	/** Input for the required solution description. */
	protected static TextArea text_Solution = new TextArea();
	/** Input for optional experience details. */
	protected static TextArea text_Experience = new TextArea();
	/** Lesson list whose selection is used by controller actions. */
	protected static ListView<Lesson> list_Lessons = new ListView<Lesson>();
	/** Reusable dialog for validation, selection, and database errors. */
	private static Alert alertError = new Alert(AlertType.ERROR);
	/** The current user accessing the scene. */
	protected static User currentUser;
	/** Visible lock indicators explain why a contributor cannot edit each field. */
	protected static Label[] fieldLabels = {new Label("Title"), new Label("Problem"),
			new Label("Solution"), new Label("Experience: what, how, time, team effort (optional)")};
	/** Privileged per-field controls, hidden from contributors. */
	protected static Button[] lockButtons = {new Button("Lock"), new Button("Lock"), new Button("Lock"), new Button("Lock")};
	/** Privileged whole-lesson controls, hidden from contributors. */
	protected static Button button_LockSelected = new Button("Lock selected");
	/** Unlocking the whole lesson leaves individual field locks unchanged. */
	protected static Button button_UnlockSelected = new Button("Unlock selected");

	/**
	 * Opens the lesson page, creating its widgets on the first call. Saves the
	 * stage's current scene and title, displays the user's name, and attempts to
	 * reload all saved lessons before showing the page.
	 *
	 * @param ps the non-null application stage whose current scene is the return page
	 * @param user the non-null user whose username is displayed
	 */
	public static void displayLessons(Stage ps, User user) {
		theStage = ps;
		theHomeScene = ps.getScene();
		theHomeTitle = ps.getTitle();

		if (theView == null) theView = new ViewLessons();

		label_UserDetails.setText("User: " + user.getUserName());
		currentUser = user;
		for (Button button : lockButtons) { button.setVisible(user.getAdminRole()); button.setManaged(user.getAdminRole()); }
		button_LockSelected.setVisible(user.getAdminRole());
		button_UnlockSelected.setVisible(user.getAdminRole());
		refreshList();

		theStage.setTitle("CSE 360: Lessons Learned");
		theStage.setScene(theLessonsScene);
		theStage.show();
	}

	/** Creates the reusable scene, lays out its controls, and connects controller actions. */
	private ViewLessons() {
		Pane theRootPane = new Pane();
		theLessonsScene = new Scene(theRootPane, width, height);
		Label label_PageTitle = new Label("Lessons Learned");
		setupLabelUI(label_PageTitle, 28, width, 0, 5);
		label_PageTitle.setAlignment(Pos.CENTER);
		setupLabelUI(label_UserDetails, 20, 600, 20, 55);
		Line line_Separator = new Line(20, 95, width - 20, 95);
		Label label_List = new Label("Lessons (select one or more)");
		setupLabelUI(label_List, 16, 300, 20, 105);
		list_Lessons.setLayoutX(20);
		list_Lessons.setLayoutY(135);
		list_Lessons.setPrefSize(300, 325);
		list_Lessons.setPlaceholder(new Label("No Lessons"));
		list_Lessons.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		list_Lessons.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldLesson, newLesson) -> {
					if (newLesson != null) ControllerLessons.readLesson();
				});

		Label label_Title = fieldLabels[0];
		setupLabelUI(label_Title, 16, 420, 350, 105);
		text_Title.setLayoutX(350);
		text_Title.setLayoutY(130);
		text_Title.setPrefWidth(430);
		Label label_Problem = fieldLabels[1];
		setupLabelUI(label_Problem, 16, 420, 350, 170);
		setupTextAreaUI(text_Problem, 350, 195);
		Label label_Solution = fieldLabels[2];
		setupLabelUI(label_Solution, 16, 420, 350, 280);
		setupTextAreaUI(text_Solution, 350, 305);
		Label label_Experience = fieldLabels[3];
		setupLabelUI(label_Experience, 14, 430, 350, 390);
		setupTextAreaUI(text_Experience, 350, 415);
		Button button_Create = new Button("Create");
		setupButtonUI(button_Create, 95, 350, 520);
		for (int index = 0; index < lockButtons.length; index++) {
			Button button = lockButtons[index];
			button.setLayoutX(680);
			button.setLayoutY(new double[] {102, 167, 277, 387}[index]);
			button.setPrefWidth(100);
			ModelLessons.LessonField field = ModelLessons.LessonField.values()[index];
			button.setOnAction(event -> ControllerLessons.toggleLock(field));
			theRootPane.getChildren().add(button);
		}
		setupButtonUI(button_LockSelected, 145, 20, 560);
		setupButtonUI(button_UnlockSelected, 145, 175, 560);
		button_LockSelected.setOnAction(event -> ControllerLessons.setSelectedLessonsLocked(true));
		button_UnlockSelected.setOnAction(event -> ControllerLessons.setSelectedLessonsLocked(false));
		Button button_AddExperience = new Button("Add experience");
		setupButtonUI(button_AddExperience, 205, 350, 560);
		button_AddExperience.setOnAction(event -> ControllerLessons.addExperience());
		Button button_History = new Button("Experience history");
		setupButtonUI(button_History, 205, 570, 560);
		button_History.setOnAction(event -> ControllerLessons.showExperiences());
		theRootPane.getChildren().addAll(button_LockSelected, button_UnlockSelected, button_AddExperience, button_History);
		button_Create.setOnAction((event) -> { ControllerLessons.createLesson(); });
		Button button_Read = new Button("Read");
		setupButtonUI(button_Read, 95, 460, 520);
		button_Read.setOnAction((event) -> { ControllerLessons.readLesson(); });
		Button button_Update = new Button("Update");
		setupButtonUI(button_Update, 95, 570, 520);
		button_Update.setOnAction((event) -> { ControllerLessons.updateLesson(); });
		Button button_Delete = new Button("Delete");
		setupButtonUI(button_Delete, 95, 680, 520);
		button_Delete.setOnAction((event) -> { ControllerLessons.deleteLesson(); });
		Button button_Subset = new Button("Show selected");
		setupButtonUI(button_Subset, 145, 20, 470);
		button_Subset.setOnAction((event) -> { ControllerLessons.showSelectedLessons(); });
		Button button_All = new Button("Show all");
		setupButtonUI(button_All, 145, 175, 470);
		button_All.setOnAction((event) -> { refreshList(); });
		Button button_Remove = new Button("Remove from subset");
		setupButtonUI(button_Remove, 300, 20, 515);
		button_Remove.setOnAction((event) -> { ControllerLessons.removeFromSubset(); });
		Button button_Back = new Button("Back");
		setupButtonUI(button_Back, 100, 680, 55);
		button_Back.setOnAction((event) -> { ControllerLessons.performBack(); });
		theRootPane.getChildren().addAll(label_PageTitle, label_UserDetails, line_Separator,
				label_List, list_Lessons, label_Title, text_Title, label_Problem, text_Problem,
				label_Solution, text_Solution, label_Experience, text_Experience,
				button_Create, button_Read, button_Update, button_Delete, button_Subset,
				button_All, button_Remove, button_Back);
	}
	/**
	 * Replaces the displayed list with all saved lessons and clears the selection
	 * and form. Displays an error if loading fails, leaving the existing display
	 * unchanged.
	 */
	protected static void refreshList() {
		try {
			displayedLessons = ModelLessons.readLessons(currentUser);
			list_Lessons.getSelectionModel().clearSelection();
			list_Lessons.getItems().setAll(displayedLessons.getLessons());
			showLesson(null);
		} catch (SecurityException e) {
			showError(e.getMessage());
		} catch (SQLException e) {
			showError("Could not load lessons. Please try again.");
		}
	}
	/**
	 * Displays an error dialog and waits for it to be dismissed.
	 *
	 * @param message the error explanation to display
	 */
	protected static void showError(String message) {
		alertError.setTitle("Lesson Error");
		alertError.setHeaderText(null);
		alertError.setContentText(message);
		alertError.showAndWait();
	}
	/**
	 * Copies a lesson's values into the form, or clears all fields for {@code null}.
	 *
	 * @param lesson the lesson to display, or {@code null} to clear the form
	 */
	protected static void showLesson(Lesson lesson) {
		text_Title.setText(lesson == null ? "" : lesson.getTitle());
		text_Problem.setText(lesson == null ? "" : lesson.getProblem());
		text_Solution.setText(lesson == null ? "" : lesson.getSolution());
		text_Experience.setText(lesson == null ? "" : lesson.getExperience());
		boolean[] locks = {lesson != null && lesson.isTitleEffectivelyLocked(), lesson != null && lesson.isProblemEffectivelyLocked(),
				lesson != null && lesson.isSolutionEffectivelyLocked(), lesson != null && lesson.isExperienceEffectivelyLocked()};
		String[] captions = {"Title", "Problem", "Solution", "Experience: what, how, time, team effort (optional)"};
		for (int index = 0; index < locks.length; index++) {
			fieldLabels[index].setText(captions[index] + (locks[index] ? " [locked]" : ""));
			lockButtons[index].setText(locks[index] ? "Unlock" : "Lock");
			lockButtons[index].setDisable(lesson == null);
		}
		boolean admin = currentUser != null && currentUser.getAdminRole();
		text_Title.setEditable(admin || !locks[0]);
		text_Problem.setEditable(admin || !locks[1]);
		text_Solution.setEditable(admin || !locks[2]);
		text_Experience.setEditable(admin || !locks[3]);
	}
	/**
	 * Sets a label's Arial font, minimum width, and position within the pane.
	 *
	 * @param label the label to configure
	 * @param size the font size
	 * @param w the minimum width in pixels
	 * @param x the horizontal layout position in pixels
	 * @param y the vertical layout position in pixels
	 */
	private static void setupLabelUI(Label label, double size, double w, double x, double y) {
		label.setFont(Font.font("Arial", size));
		label.setMinWidth(w);
		label.setLayoutX(x);
		label.setLayoutY(y);
	}
	/**
	 * Sets a button's font, minimum width, centered alignment, and pane position.
	 *
	 * @param button the button to configure
	 * @param w the minimum width in pixels
	 * @param x the horizontal layout position in pixels
	 * @param y the vertical layout position in pixels
	 */
	private static void setupButtonUI(Button button, double w, double x, double y) {
		button.setFont(Font.font("Dialog", 16));
		button.setMinWidth(w);
		button.setAlignment(Pos.CENTER);
		button.setLayoutX(x);
		button.setLayoutY(y);
	}
	/**
	 * Positions a wrapping text area with a preferred size of 430 by 75 pixels.
	 *
	 * @param text the text area to configure
	 * @param x the horizontal layout position in pixels
	 * @param y the vertical layout position in pixels
	 */
	private static void setupTextAreaUI(TextArea text, double x, double y) {
		text.setLayoutX(x);
		text.setLayoutY(y);
		text.setPrefSize(430, 75);
		text.setWrapText(true);
	}

	/** Displays contributor-authored attempts in order.
	 * @param history authorized history entries */
	protected static void showExperienceHistory(java.util.List<String> history) {
		Alert dialog = new Alert(AlertType.INFORMATION);
		dialog.setTitle("Experience history");
		dialog.setHeaderText(null);
		dialog.setContentText(history.isEmpty() ? "No experiences" : String.join("\n\n", history));
		dialog.showAndWait();
	}
}
