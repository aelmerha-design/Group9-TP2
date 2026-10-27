package entityClasses;
import java.sql.SQLException;
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

}
