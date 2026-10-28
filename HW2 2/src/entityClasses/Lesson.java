package entityClasses;
import java.sql.SQLException;
import java.util.ArrayList;
import database.Database;


/*******
 * <p> Title: Lesson Class. </p>
 * 
 * <p> Description: This class represents one persistent Lessons Learned entry.
 * A lesson stores the identification of the lesson, its contributor, along with 
 * the title, problem, solution, and experience information which is optional.</p>
 * 
 * <p> The attributes support the User Stories. The id identifies the lesson, ownerId
 * identifies the contributor who created it, title identifies the lesson to users, 
 * problem describes the situation being addressed, solution records what was done, and 
 * experience stores additional information learned.</p>
 * 
 * <p> This class provides input validation and persistent Create, Read, Update,
 * and Delete operations. This design can be extended with additional attributes needed 
 * in later project phases, including refinement, searching, locking, visibility, and
 * effort statistics.
 *  
 * experience information.</p>
 * 
 * @author Ahmad El-Merhaby
 */


public class Lesson {
	
	private int id;
	private int ownerId;
	private String title;
	private String problem;
	private String solution;
	private String experience;
	/** Persistent locked state used by contributor lock restrictions. */
	private boolean locked;
	/** Persistent titleLocked state used by contributor lock restrictions. */
	private boolean titleLocked;
	/** Persistent problemLocked state used by contributor lock restrictions. */
	private boolean problemLocked;
	/** Persistent solutionLocked state used by contributor lock restrictions. */
	private boolean solutionLocked;
	/** Persistent experienceLocked state used by contributor lock restrictions. */
	private boolean experienceLocked;
	/** Curator-approved search terms reserved for Phase Three search coaching. */
	private String searchTerms = "";
	/** Recorded duration in hours for future effort aggregation. */
	private double timeHours;
	/** Recorded effort in person-hours for future effort aggregation. */
	private double effortHours;
	/** Visibility policy for future statistics, separate from contributor core content. */
	private boolean statsVisible;

	
	
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
	/**@param title the new lesson title */
	public void setTitle(String title) {
		this.title = title == null ? "" : title.trim();
		
	}
	/**@param problem the new problem information*/
	public void setProblem(String problem) {
		this.problem = problem == null ? "" : problem.trim();
		
	}
	/** @return the lesson title */
	public String getTitle() { return title; }

	/** @return the lesson problem */
	public String getProblem() { return problem; }
	
	/** @return the lesson solution */
	public String getSolution() { return solution; }
	
	/**@param solution the solution information*/
	public void setSolution(String solution) {
		this.solution = solution == null ? "" : solution.trim();
	}
	
	/**@param experience the experience information*/
	public void setExperience(String experience) {
		this.experience = experience == null ? "" : experience.trim();
		
	}
	/** @return the lesson experience information */
	public String getExperience() { return experience; }
	
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
	
	/**
	 * Validates the optional experience information. Experience may be empty
	 * but it cannot exceet the 2000 character limit
	 * 
	 * @param experience the experience information to validate
	 * @return true if the experience information is valid, otherwise false
	 */
	public static boolean isValidExperience(String experience) {
		return experience == null || experience.trim().length() <= 2000;
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
	 * 
	 * returns the lesson identification and title
	 * 
	 * @return the lesson identification and title
	 */
	@Override
	public String toString() {
		return id + ": " + title;
	}

	/** @return the stored locked state */
	public boolean isLocked() { return locked; }
	/** @param value the locked state to persist */
	public void setLocked(boolean value) { locked = value; }

	/** @return the stored titleLocked state */
	public boolean isTitleLocked() { return titleLocked; }
	/** @param value the titleLocked state to persist */
	public void setTitleLocked(boolean value) { titleLocked = value; }

	/** @return the stored problemLocked state */
	public boolean isProblemLocked() { return problemLocked; }
	/** @param value the problemLocked state to persist */
	public void setProblemLocked(boolean value) { problemLocked = value; }

	/** @return the stored solutionLocked state */
	public boolean isSolutionLocked() { return solutionLocked; }
	/** @param value the solutionLocked state to persist */
	public void setSolutionLocked(boolean value) { solutionLocked = value; }

	/** @return the stored experienceLocked state */
	public boolean isExperienceLocked() { return experienceLocked; }
	/** @param value the experienceLocked state to persist */
	public void setExperienceLocked(boolean value) { experienceLocked = value; }
	/** @return whether the whole lesson or this field is locked */
	public boolean isTitleEffectivelyLocked() { return locked || titleLocked; }
	/** @return whether the whole lesson or this field is locked */
	public boolean isProblemEffectivelyLocked() { return locked || problemLocked; }
	/** @return whether the whole lesson or this field is locked */
	public boolean isSolutionEffectivelyLocked() { return locked || solutionLocked; }
	/** @return whether the whole lesson or this field is locked */
	public boolean isExperienceEffectivelyLocked() { return locked || experienceLocked; }
	/** @return whether any lesson or field lock is active */
	public boolean hasAnyLock() { return locked || titleLocked || problemLocked || solutionLocked || experienceLocked; }
	/** @return curator-managed search terms */
	public String getSearchTerms() { return searchTerms; }
	/** @param value curator-managed search terms */
	public void setSearchTerms(String value) { searchTerms = value == null ? "" : value.trim(); }
	/** @return recorded duration in hours */
	public double getTimeHours() { return timeHours; }
	/** @param value recorded duration in hours */
	public void setTimeHours(double value) { timeHours = value; }
	/** @return recorded effort in person-hours */
	public double getEffortHours() { return effortHours; }
	/** @param value recorded effort in person-hours */
	public void setEffortHours(double value) { effortHours = value; }
	/** @return whether the statistics visibility policy is enabled */
	public boolean isStatsVisible() { return statsVisible; }
	/** @param value whether statistics may be displayed by a future authorized view */
	public void setStatsVisible(boolean value) { statsVisible = value; }
	/**
	 * Appends one validated attempt without replacing earlier experiences.
	 * Authorization belongs to the actor-aware model, as with other entity operations.
	 * @param db trusted persistence connection
	 * @param text what was done, how, time, and individual team effort
	 * @return whether an existing lesson received a valid experience
	 * @throws SQLException if persistence fails
	 */
	public boolean addExperience(Database db, String text) throws SQLException {
		if (text == null || text.trim().isEmpty() || !isValidExperience(text)) return false;
		return db.addExperience(id, text.trim());
	}
	/**
	 * Reads attempts in insertion order through the trusted entity API.
	 * @param db trusted persistence connection
	 * @return saved attempts, possibly empty
	 * @throws SQLException if persistence fails
	 */
	public ArrayList<String> getExperiences(Database db) throws SQLException { return db.readExperiences(id); }
}
