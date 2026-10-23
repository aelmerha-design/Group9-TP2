package guiLessons;

import java.sql.SQLException;
import entityClasses.Lesson;
import entityClasses.LessonList;

/*******
 * <p> Title: ControllerLessons Class. </p>
 * <p> Description: this handles lesson page actions and displays helpful errors for invalid input or selection. </p>
 * @author Ahmad El-Merhaby
 */

public class ControllerLessons {

	// this uses a separate object so invalid edits do not change saved data.
	private static Lesson lessonFromFields(int id) {
		return new Lesson(id, ViewLessons.text_Title.getText(),
				ViewLessons.text_Problem.getText(), ViewLessons.text_Solution.getText(),
				ViewLessons.text_Experience.getText());
	}
	protected static void createLesson() {
		Lesson lesson = lessonFromFields(0);
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
	// check that the records exists and that the input is valid before saving changes
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

			Lesson lesson = lessonFromFields(selected.getId());
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
	// this builds a subset without changing the database records
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
	// this removes only the displayed list entry
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
	protected static void performBack() {
		ViewLessons.theStage.setScene(ViewLessons.theHomeScene);
		ViewLessons.theStage.setTitle(ViewLessons.theHomeTitle);
	}
	
}
