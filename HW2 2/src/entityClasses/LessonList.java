package entityClasses;

import java.util.ArrayList;

/*******
 * <p> Title: LessonList Class. </p>
 * <p> Description: Stores all lessons or a selected subset without a fixed size limit. </p>
 * @author Ahmad El-Merhaby
 */

public class LessonList {
		// ArrayList grows as it should because the lists and the subsets have no limits
		private ArrayList<Lesson> lessons = new ArrayList<Lesson>();

		// this adds the lesson once
		public void addLesson(Lesson lesson) {
			if (getLesson(lesson.getId()) == null) lessons.add(lesson);
		}
		public Lesson getLesson(int id) {
			for (Lesson lesson : lessons) {
				if (lesson.getId() == id) return lesson;
			}
			return null;
		}
		// this updates the entry with the same identification
		public boolean updateLesson(Lesson lesson) {
			for (int i = 0; i < lessons.size(); i++) {
				if (lessons.get(i).getId() == lesson.getId()) {
					lessons.set(i, lesson);
					return true;
				}
			}
			return false;
		}
		// this removes only the list entry and leaves the database record unchanged
		public boolean removeLesson(int id) {
			for (int i = 0; i < lessons.size(); i++) {
				if (lessons.get(i).getId() == id) {
					lessons.remove(i);
					return true;
				}
			}
			return false;
		}
		// this returns a copy so changing the returned list does not change this list
		public ArrayList<Lesson> getLessons() {
			return new ArrayList<Lesson>(lessons);
		}
	}
