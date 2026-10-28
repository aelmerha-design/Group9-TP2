package entityClasses;
import java.sql.SQLException;
import java.util.ArrayList;
import database.Database;

/*******
 * <p> Title: Lesson Class. </p>
 * 
 * <p> This is the LessonsLearned class required by TP2. </p>
 * 
 * <p> Description: This class represents one persistent Lessons Learned entry.
 * A lesson stores the identification of the lesson, its contributor, along with 
 * the title, problem, solution, and experience information which is optional.</p>
 * 
 * <p> The attributes support the User Stories. The id identifies the lesson, ownerId
 * identifies the contributor who created it, title identifies the lesson to users, 
 * problem describes the situation being addressed, solution records what was done, and 
 * experience stores additional information learned. </p>
 * 
 * <p> This class provides input validation and persistent Create, Read, Update,
 * and Delete operations. </p>
 * 
 * <p> The class also stores attributes for the Phase 3 epics. The lock attributes are for
 * the epic that locks a lesson or its fields. searchTerms is for the AI search terms epic.
 * timeHours and effortHours are for the epic that merges time and effort data. statsVisible
 * is for the epic that shows or hides the statistics. The AI writing epic and the AI
 * refinement epic use the existing text fields so they do not need new attributes. </p>
 * 
 * @author Ahmad El-Merhaby
 */


public class Lesson {
	
	/** The id of the lesson. Needed so each lesson can be found, updated, and deleted. */
	private int id;

	/** The id of the contributor who wrote the lesson. Needed so only the author can change it. */
	private int ownerId;

	/** The title of the lesson. Needed by the contributor user stories to name the lesson. */
	private String title;

	/** The problem that happened. Needed by the contributor user stories. */
	private String problem;

	/** What was done to solve the problem. Needed by the contributor user stories. */
	private String solution;

	/** Extra experience the contributor learned. It is optional. Needed by the contributor user stories. */
	private String experience;

	/** True if the whole lesson is locked. Needed for the epic that locks lessons. */
	private boolean locked;

	/** True if the title is locked. Needed for the epic that locks lesson fields. */
	private boolean titleLocked;

	/** True if the problem is locked. Needed for the epic that locks lesson fields. */
	private boolean problemLocked;

	/** True if the solution is locked. Needed for the epic that locks lesson fields. */
	private boolean solutionLocked;

	/** True if the experience is locked. Needed for the epic that locks lesson fields. */
	private boolean experienceLocked;

	/** Search terms the curator approves so users can find this lesson. Needed for the AI search terms epic. */
	private String searchTerms = "";

	/** Time spent on this lesson in hours. Needed for the epic that merges time and effort data. */
	private double timeHours = 0;

	/** Effort spent on this lesson in person hours. Needed for the epic that merges time and effort data. */
	private double effortHours = 0;

	/** Whether the contributor can see the statistics. Needed for the epic that shows or hides statistics. */
	private boolean statsVisible = false;
	
	
	
    /**
	 * Creates a Lesson object using the lesson identification, contributor, title
	 * problem, solution and experience information(optional). Text values are trimmed 
	 * before validation so that whitespace only required input is rejected and stored 
	 * lengths match the validation lengths
	 * 
	 * @param id the persistent identification of the lesson
	 * @param ownerId the identification of the contributor who created the lesson
	 * @param title the lesson title
	 * @param problem the problem or situation
	 * @param solution the recorded solution or lesson learned
	 * @param experience optional experience information
	 */
	public Lesson(int id, int ownerId, String title, String problem, String solution, String experience) {
		this.id = id;
		this.ownerId = ownerId;
		this.title = title == null ? "" : title.trim();
		this.problem = problem == null ? "" : problem.trim();
		this.solution = solution == null ? "" : solution.trim();
		this.experience = experience == null ? "" : experience.trim();
	}
	
	/** @return the lesson identification */
	public int getId() { return id; }
  /**@param id the lesson identification*/
	public void setId(int id) { this.id = id; }
  /** @return the contributor identification */
	public int getOwnerId() { return ownerId; }
  /**@param id the contributor identification*/
	public void setOwnerId(int id) { this.ownerId = id;}
	/** @return the lesson title */
	public String getTitle() { return title; }
  /**@param title the new lesson title */
	public void setTitle(String title) {
		this.title = title == null ? "" : title.trim();
	}
  /**
	 * Validates that the title contains non blank text and does not exceed
	 * the 100 character limit
	 * 
	 * @param title the title to validate
	 * @return true if the title is valid, otherwise false
	 */
	public static boolean isValidTitle(String title) {
		return title != null && !title.trim().isEmpty() && title.trim().length() <= 100;
	}
  /** @return the stored title-specific lock */
	public boolean isTitleLocked() { return titleLocked; }
  /** @return true if locked or whole-locked; false if unlocked */
	public boolean isTitleEffectivelyLocked() { return locked || titleLocked; }
  /** @param titleLocked the bool determining whether to lock or not */
	public void setTitleLocked(boolean titleLocked) {
		this.titleLocked = titleLocked;
	}

	/** @return the lesson problem */
	public String getProblem() { return problem; }
  /**@param problem the new problem information*/
	public void setProblem(String problem) {
		this.problem = problem == null ? "" : problem.trim();
	}
  /** 
	 * Validates that the problem contains non blank text and does not exceed the
	 * 2000 character limit
	 * 
	 * @param problem the problem to validate
	 * @return true if the problem is valid, otherwise false
	 */
	public static boolean isValidProblem(String problem) {
		return problem != null && !problem.trim().isEmpty() && problem.trim().length() <= 2000;
	}
  /** @return the stored problem-specific lock */
	public boolean isProblemLocked() { return problemLocked; }
  /** @return true if locked or whole-locked; false if unlocked */
	public boolean isProblemEffectivelyLocked() { return locked || problemLocked; }
  /** @param problemLocked the bool determining whether to lock or not */
	public void setProblemLocked(boolean problemLocked) {
		this.problemLocked = problemLocked;
	}

	/** @return the lesson solution */
	public String getSolution() { return solution; }
  /**@param solution the solution information*/
	public void setSolution(String solution) {
		this.solution = solution == null ? "" : solution.trim();
	}
  /**
	 * Validates that the solution contains non blank text and does not exceed the 2000
	 * character limit
	 * 
	 * @param solution the solution to validate
	 * @return true if the solution is valid, otherwise  returns false
	 */
	public static boolean isValidSolution(String solution) {
		return solution != null && !solution.trim().isEmpty() && solution.trim().length() <= 2000;
	}
  /** @return the stored solution-specific lock */
	public boolean isSolutionLocked() { return solutionLocked; }
  /** @return true if locked or whole-locked; false if unlocked */
	public boolean isSolutionEffectivelyLocked() { return locked || solutionLocked; }
  /** @param solutionLocked the bool determining whether to lock or not */
	public void setSolutionLocked(boolean solutionLocked) {
		this.solutionLocked = solutionLocked;
	}

	/** @return the lesson experience information */
	public String getExperience() { return experience; }
	/**@param experience the experience information*/
	public void setExperience(String experience) {
		this.experience = experience == null ? "" : experience.trim();
	}
  /**
	 * Validates the optional experience information. Experience may be empty
	 * but it cannot exceed the 2000 character limit
	 * 
	 * @param experience the experience information to validate
	 * @return true if the experience information is valid, otherwise false
	 */
	public static boolean isValidExperience(String experience) {
		return experience == null || experience.trim().length() <= 2000;
	}
  /** @return the stored experience-specific lock */
	public boolean isExperienceLocked() { return experienceLocked; }
  /** @return true if locked or whole-locked; false if unlocked */
	public boolean isExperienceEffectivelyLocked() { return locked || experienceLocked; }
  /** @param experienceLocked the bool determining whether to lock or not */
	public void setExperienceLocked(boolean experienceLocked) {
		this.experienceLocked = experienceLocked;
	}

	/** @return true if the whole lesson is locked */
	public boolean isLocked() { return locked; }

  /** @param locked true to lock the whole lesson; false to unlock */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/** @return true if the whole lesson or any individual field is locked */
	public boolean hasAnyLock() {
		return locked || titleLocked || problemLocked || solutionLocked || experienceLocked;
	}
	
	
	

	/** @return the search terms */
	public String getSearchTerms() {
		return searchTerms;
	}

	/** @param searchTerms the new search terms */
	public void setSearchTerms(String searchTerms) {
		this.searchTerms = searchTerms == null ? "" : searchTerms.trim();
	}

	/** @return the time in hours */
	public double getTimeHours() {
		return timeHours;
	}

	/** @param timeHours the new time in hours */
	public void setTimeHours(double timeHours) {
		this.timeHours = timeHours;
	}

	/** @return the effort in person hours */
	public double getEffortHours() {
		return effortHours;
	}

	/** @param effortHours the new effort in person hours */
	public void setEffortHours(double effortHours) {
		this.effortHours = effortHours;
	}

	/** @return true if the contributor can see the statistics */
	public boolean isStatsVisible() {
		return statsVisible;
	}

	/** @param statsVisible true to show the statistics */
	public void setStatsVisible(boolean statsVisible) {
		this.statsVisible = statsVisible;
	}
	
	
  
	/**
	 * Checks all lesson fields using the validation methods above so invalid 
	 * lesson data is not written to the persistent database
	 * 
	 * @return true if all lesson fields are valid, otherwise returns false.
	 */
	public boolean isValid() {
		return isValidTitle(title) && isValidProblem(problem) && isValidSolution(solution) && isValidExperience(experience);
	}

	/**
	 * Saves this lesson to the database after validating its fields
	 * The database generates and assigns the lesson identification
	 * when the lesson is created successfully
	 * 
	 * @param db the database used to save the lesson
	 * @return the generated lesson identification, or -1 if the lesson is invalid
	 * @throws SQLException if the database operation fails
	 */
	public int create(Database db) throws SQLException {
		if (!isValid()) return -1;
		return db.createLesson(this);
	}
    /**
     * Reads one lesson from the database using its identification
     * 
     * @param db the database used to retrieve the lesson
     * @param id the identification of the lesson
     * @return the stored lesson, or null if the lesson does not exist
     * @throws SQLException if the database operation fails
     */
	public static Lesson read(Database db, int id) throws SQLException {
		return db.readLesson(id);
	}

	/**
	 * Validates this lesson and save its changed information to the
	 * database without changing its identification
	 * 
	 * @param db the database used to update the lesson
	 * @return true if the lesson was valid and updated, otherwise false
	 * @throws SQLException if the database operation fails
	 */
	public boolean update(Database db) throws SQLException {
		if (!isValid()) return false;
		return db.updateLesson(this);
	}

	/**
	 * Deletes this lesson from the database using its identification
	 * 
	 * @param db the database used to delete the lesson
	 * @return true if the lesson was deleted, otherwise false
	 * @throws SQLException if the database operation fails
	 * 
	 */
	public boolean delete(Database db) throws SQLException {
		return db.deleteLesson(id);
	}
	
	
	
	/**
	 * Adds a new experience to this lesson and keeps the old ones.
	 *
	 * @param db the database
	 * @param newExperience the experience to add
	 * @return true if the experience was saved
	 * @throws SQLException if the database has a problem
	 */
	public boolean addExperience(Database db, String newExperience) throws SQLException {
		if (newExperience == null || newExperience.trim().isEmpty() || !isValidExperience(newExperience)) return false;
		return db.addExperience(id, newExperience.trim());
	}

	/**
	 * Gets all the experiences for this lesson in the order they were added.
	 *
	 * @param db the database
	 * @return the list of experiences
	 * @throws SQLException if the database has a problem
	 */
	public ArrayList<String> getExperiences(Database db) throws SQLException {
		return db.readExperiences(id);
	}
	
	
  
	/**
	 * 
	 * returns the lesson identification and title
	 * 
	 * @return the lesson identification and title
	 */
	@Override
	public String toString() {
		return id + ": " + title;
	}

}
