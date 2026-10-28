package entityClasses;

import java.util.ArrayList;
import java.sql.SQLException;
import database.Database;

/*******
 * <p> Title: LessonList Class. </p>
 * <p> This is the LessonsLearnedList class required by TP2. </p>
 * <p> Description: Stores a collection of Lesson objects without a fixed size limit.
 * The collection may represent all lessons, the lessons belonging to
 * one contributor, or a selected set.</p>
 * 
 * <p> This class supports the contributor user stories by allowing lessons
 * to be added, found updated, removed, retrieved from the database
 * and searched</p>
 * 
 * <p> Persistent list operations use the Database class to retrieve saved lessons
 * The design can support later project phases that require searching, viewing collections
 * and producing subsets of lessons</p>
 * 
 * 
 * 
 * @author Ahmad El-Merhaby
 * 
 */


public class LessonList {
		/** The lessons in this list. An ArrayList is used because the list and its subsets have no size limit. */
		private ArrayList<Lesson> lessons = new ArrayList<Lesson>();
		
		
		/** 
		 * Adds a lesson to the list if a lesson with the same Id
		 * is not already there
		 * 
		 * 
		 * @param lesson the lesson to be added
		 */
		public void addLesson(Lesson lesson) {
			if (getLesson(lesson.getId()) == null) lessons.add(lesson);
		}
		
		
		/**
		 * Finds a lesson in the list using its id
		 * 
		 * @param id the lesson id
		 * 
		 * @return the matching lesson, null if it is not found
		 *
		 * 
		 */
		public Lesson getLesson(int id) {
			for (Lesson lesson : lessons) {
				if (lesson.getId() == id) return lesson;
			}
			return null;
		}
		/**
		 * Replaces the lesson with the same id using the
		 * updated lesson.
		 * 
		 * @param lesson the updated lesson
		 * @return true if the lesson was found and updated, otherwise
		 * return false
		 */
		public boolean updateLesson(Lesson lesson) {
			for (int i = 0; i < lessons.size(); i++) {
				if (lessons.get(i).getId() == lesson.getId()) {
					lessons.set(i, lesson);
					return true;
				}
			}
			return false;
		}
		/**
		 * Removes the lesson with the specified id from
		 * this list. 
		 * This does not delete the lesson from the database
		 * 
		 * @param id the id of the lesson to remove
		 * @return true if the lesson was removed, otherwise
		 * return false
		 */
		public boolean removeLesson(int id) {
			for (int i = 0; i < lessons.size(); i++) {
				if (lessons.get(i).getId() == id) {
					lessons.remove(i);
					return true;
				}
			}
			return false;
		}
		
		
		/**
		 * Returns a copy of the lessons so changes to the Arraylist
		 * dont directly change this lesson list
		 * 
		 * @return a copy of the lessons in this list
		 */
		public ArrayList<Lesson> getLessons() {
			return new ArrayList<Lesson>(lessons);
		}
		
		/**
		 * Retrieves the lessons belonging to one contributor
		 * @param db the database used to retrieve the lessons
		 * @param ownerId the identification of the contributor
		 * @return the contributors saved lessons
		 * @throws SQLException if the database operation fails
		 * 
		 */
		public static LessonList readLessons(Database db, int ownerId) throws SQLException {
			return db.readLessons(ownerId);
		}
		/**
		 * Retrieves all lessons from the database.
		 * 
		 * @param db the database used to retrieve the lessons
		 * @return all saved lessons
		 * @throws SQLException if the database operation fails
		 */
		public static LessonList readAllLessons(Database db) throws SQLException {
			return db.readAllLessons();
		}
		
		/**
		 * Creates a new subset containing lessons where the title, problem
		 * ,solution, or experience information contains the specified keyword.
		 * The original list remains unchanged
		 * 
		 * @param keyword the text to search for
		 * @return a new LessonList containing matching lessons
		 */
		public LessonList searchByKeyword(String keyword) {
			LessonList subset = new LessonList();
			String key = keyword == null ? "" : keyword.trim().toLowerCase();
			for (Lesson lesson : lessons) {
				String text = (lesson.getTitle() + " " + lesson.getProblem() + " " + lesson.getSolution() + " "
						+ lesson.getExperience()).toLowerCase();
				if (text.contains(key)) subset.addLesson(lesson);
			}
			return subset;
		}
	}
