package guiLessons;

import java.sql.SQLException;
import entityClasses.Lesson;
import entityClasses.LessonList;

/**
 * Handles lesson page actions by coordinating {@link ViewLessons} and
 * {@link ModelLessons}. Validates input before saving and asks the view to
 * display validation, selection, and database errors.
 *
 * <p>Call these actions on the JavaFX Application Thread after the lesson view
 * has been initialized.</p>
 *
 * @author Ahmad El-Merhaby
 */

public class ControllerLessons {

	/**
	 * Copies the form values into a separate lesson so rejected edits do not
	 * modify a lesson already held in the list. The lesson constructor trims text.
	 *
	 * @param id the selected lesson's ID, or {@code 0} for a new lesson
	 * @return a lesson containing the current form values
	 */
	private static Lesson lessonFromFields(int id, int ownerId) {
		return new Lesson(id, ownerId, ViewLessons.text_Title.getText(),
				ViewLessons.text_Problem.getText(), ViewLessons.text_Solution.getText(),
				ViewLessons.text_Experience.getText());
	}

	/**
	 * Validates and saves a new lesson from the form, then reloads all lessons.
	 * Displays an error if validation or saving fails.
	 */
	protected static void createLesson() {
		Lesson lesson = lessonFromFields(0, ViewLessons.currentUser.getUserId());
		String errorMessage = ModelLessons.validateLesson(lesson);
		if (!errorMessage.isEmpty()) {
			ViewLessons.showError(errorMessage);
			return;
		}
		try {
			ModelLessons.createLesson(lesson);
			ViewLessons.refreshList();
		} catch (SQLException e) {
			ViewLessons.showError("Could not save the lesson. Please try again.");
		}
	}

	/**
	 * Loads the selected lesson from the database into the form. Displays an error
	 * if no lesson is selected, the record no longer exists, or reading fails.
	 */
	protected static void readLesson() {
		Lesson selected = ViewLessons.list_Lessons.getSelectionModel().getSelectedItem();
		if (selected == null) {
			ViewLessons.showError("Select a lesson that exists");
			return;
		}
		try {
			Lesson lesson = ModelLessons.readLesson(selected.getId());

			if (lesson == null) ViewLessons.showError("Select a lesson that exists");
			else ViewLessons.showLesson(lesson);
		} catch (SQLException e) {
			ViewLessons.showError("Could not read the lesson. Please try again.");
		}
	}

	/**
	 * Checks that the selected record exists and validates a separate copy of the
	 * form values before updating it. Reloads all lessons after a successful
	 * update; otherwise displays a selection, validation, or database error.
	 */
	protected static void updateLesson() {
		Lesson selected = ViewLessons.list_Lessons.getSelectionModel().getSelectedItem();
		if (selected == null) {
			ViewLessons.showError("Select a lesson that exists");
			return;
		}

		try {
			if (ModelLessons.readLesson(selected.getId()) == null) {
				ViewLessons.showError("Select a lesson that exists");
				return;
			}

			Lesson lesson = lessonFromFields(selected.getId(), selected.getOwnerId());
			String errorMessage = ModelLessons.validateLesson(lesson);

			if (!errorMessage.isEmpty()) {
				ViewLessons.showError(errorMessage);
				return;
			}

			if (ModelLessons.updateLesson(lesson)) ViewLessons.refreshList();
			else ViewLessons.showError("Select a lesson that exists");
		} catch (SQLException e) {
			ViewLessons.showError("Could not update the lesson. Please try again.");
		}
	}

	/**
	 * Deletes the selected lesson from the database and reloads all lessons.
	 * Displays an error if no lesson is selected, the record no longer exists,
	 * or deletion fails.
	 */
	protected static void deleteLesson() {
		Lesson selected = ViewLessons.list_Lessons.getSelectionModel().getSelectedItem();
		if (selected == null) {
			ViewLessons.showError("Select a lesson that exists");
			return;
		}
		try {
			if (ModelLessons.deleteLesson(selected.getId())) ViewLessons.refreshList();
			else ViewLessons.showError("Select a lesson that exists");
		} catch (SQLException e) {
			ViewLessons.showError("Could not delete the lesson. Please try again.");
		}
	}

	/**
	 * Replaces the displayed list with a subset of the selected lessons, then
	 * clears the selection and form. An empty selection produces an empty subset.
	 * Database records are unchanged.
	 */
	protected static void showSelectedLessons() {
		LessonList subset = new LessonList();
		for (Lesson lesson : ViewLessons.list_Lessons.getSelectionModel().getSelectedItems()) {
			subset.addLesson(lesson);
		}
		ViewLessons.displayedLessons = subset;
		ViewLessons.list_Lessons.getSelectionModel().clearSelection();
		ViewLessons.list_Lessons.getItems().setAll(subset.getLessons());
		ViewLessons.showLesson(null);
	}
	
	/**
	 * Removes the selected lesson from the displayed list, then clears the
	 * selection and form. Displays an error if no lesson is selected.
	 * The database record is unchanged; reloading all lessons restores the entry.
	 */
	protected static void removeFromSubset() {
		Lesson selected = ViewLessons.list_Lessons.getSelectionModel().getSelectedItem();
		if (selected == null) {
			ViewLessons.showError("Select a lesson that exists");
			return;
		}
		ViewLessons.displayedLessons.removeLesson(selected.getId());
		ViewLessons.list_Lessons.getSelectionModel().clearSelection();
		ViewLessons.list_Lessons.getItems().setAll(ViewLessons.displayedLessons.getLessons());
		ViewLessons.showLesson(null);
	}
	/** Restores the scene and window title saved when the lesson page opened. */
	protected static void performBack() {
		ViewLessons.theStage.setScene(ViewLessons.theHomeScene);
		ViewLessons.theStage.setTitle(ViewLessons.theHomeTitle);
	}
	
}
