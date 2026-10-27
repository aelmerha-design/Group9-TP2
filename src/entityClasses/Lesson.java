package entityClasses;

/*******
 * <p> Title: Lesson Class. </p>
 * <p> Description: This stores a lesson's identification, title, problem, solution, and optional experience. </p>
 * @author Ahmad El-Merhaby
 */

// Trim before the validation so that the spacesonly input is rejected and checked lengths match the stored length
public class Lesson {
	
	private int id;
	private int ownerId;
	private String title;
	private String problem;
	private String solution;
	private String experience;

	private boolean locked;
	private boolean titleLocked;
	private boolean problemLocked;
	private boolean solutionLocked;
	private boolean experienceLocked;
	
	
	// this trims the text before validation and storage
	public Lesson(int id, int ownerId, String title, String problem, String solution, String experience) {
		this.id = id;
		this.ownerId = ownerId;
		this.title = title == null ? "" : title.trim();
		this.problem = problem == null ? "" : problem.trim();
		this.solution = solution == null ? "" : solution.trim();
		this.experience = experience == null ? "" : experience.trim();
	}
	
	// ID
	public int getId() { return id; }

	public void setId(int id) { this.id = id; }

	// Owner ID
	public int getOwnerId() { return ownerId; }

	public void setOwnerId(int id) { this.ownerId = id;}
	
	// Title
	public String getTitle() { return title; }

	public void setTitle(String title) {
		this.title = title == null ? "" : title.trim();
	}

	public static boolean isValidTitle(String title) {
		return title != null && !title.trim().isEmpty() && title.trim().length() <= 100;
	}

	public boolean isTitleLocked() { return titleLocked; }

	public boolean isTitleEffectivelyLocked() { return locked || titleLocked; }

	public void setTitleLocked(boolean titleLocked) {
		this.titleLocked = titleLocked;
	}

	// Problem
	public String getProblem() { return problem; }

	public void setProblem(String problem) {
		this.problem = problem == null ? "" : problem.trim();
	}

	public static boolean isValidProblem(String problem) {
		return problem != null && !problem.trim().isEmpty() && problem.trim().length() <= 2000;
	}

	public boolean isProblemLocked() { return problemLocked; }

	public boolean isProblemEffectivelyLocked() { return locked || problemLocked; }

	public void setProblemLocked(boolean problemLocked) {
		this.problemLocked = problemLocked;
	}

	// Solution
	public String getSolution() { return solution; }

	public void setSolution(String solution) {
		this.solution = solution == null ? "" : solution.trim();
	}

	public static boolean isValidSolution(String solution) {
		return solution != null && !solution.trim().isEmpty() && solution.trim().length() <= 2000;
	}

	public boolean isSolutionLocked() { return solutionLocked; }

	public boolean isSolutionEffectivelyLocked() { return locked || solutionLocked; }

	public void setSolutionLocked(boolean solutionLocked) {
		this.solutionLocked = solutionLocked;
	}

	// Experience
	public String getExperience() { return experience; }
	
	public void setExperience(String experience) {
		this.experience = experience == null ? "" : experience.trim();
		
	}

	public static boolean isValidExperience(String experience) {
		return experience == null || experience.trim().length() <= 2000;
	}

	public boolean isExperienceLocked() { return experienceLocked; }

	public boolean isExperienceEffectivelyLocked() { return locked || experienceLocked; }

	public void setExperienceLocked(boolean experienceLocked) {
		this.experienceLocked = experienceLocked;
	}

	// Check if the entire lesson is locked
	// NOTE: The idea of an entire lesson being locked is mentioned breifly in the User Stories.
	// This essentially serves as an easy way to lock all fields.
	public boolean isLocked() { return locked; }

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	// Check if any part of the lesson is locked
	// This helps with checking if a contributor can delete a lesson.
	public boolean hasAnyLock() {
		return locked || titleLocked || problemLocked || solutionLocked || experienceLocked;
	}
	
	// this displays the identification and title in the list
	@Override
	public String toString() {
		return id + ": " + title;
	}

}
