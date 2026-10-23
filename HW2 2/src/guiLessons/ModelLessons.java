package guiLessons;

import java.sql.SQLException;
import database.Database;
import entityClasses.Lesson;
import entityClasses.LessonList;

/*******
 * <p> Title: ModelLessons Class. </p>
 * <p> Description: Validates lesson input and connects lesson operations to the Foundations database. </p>
 * @author Ahmad El-Merhaby
 */

public class ModelLessons {
		private static Database theDatabase = applicationMain.FoundationsMain.database;
		private static LessonList lessons = new LessonList();

		// this returns an empty message when the lesson is valid
		protected static String validateLesson(Lesson lesson) {
			if (lesson.getTitle().length() < 1 || lesson.getTitle().length() > 100)
				return "Enter a title that is between 1-100 characters";
			if (lesson.getProblem().length() < 1 || lesson.getProblem().length() > 2000)
				return "Enter a problem that is between 1-2000 characters";
			if (lesson.getSolution().length() < 1 || lesson.getSolution().length() > 2000)
				return "Enter a solution that is between 1-2000 characters";
			if (lesson.getExperience().length() > 2000)
				return "Experience has max 2000 characters";

			return "";
		}
		// use the database generated ID so that each saved lesson has a unique ID
		protected static int createLesson(Lesson lesson) throws SQLException {
			int id = theDatabase.createLesson(lesson);
			lesson.setId(id);
			lessons.addLesson(lesson);
			return id;
		}
		protected static Lesson readLesson(int id) throws SQLException {
			return theDatabase.readLesson(id);
		}
		// reload from the database so that the list reflects the saved changes
		protected static LessonList readLessons() throws SQLException {
			lessons = theDatabase.readLessons();
			return lessons;
		}
		protected static boolean updateLesson(Lesson lesson) throws SQLException {
			if (!theDatabase.updateLesson(lesson)) return false;
			lessons.updateLesson(lesson);
			return true;
		}
		protected static boolean deleteLesson(int id) throws SQLException {
			if (!theDatabase.deleteLesson(id)) return false;
			lessons.removeLesson(id);
			return true;
		}
	
}
