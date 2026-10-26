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
	
	
	// this trims the text before validation and storage
	public Lesson(int id, int ownerId, String title, String problem, String solution, String experience) {
		this.id = id;
		this.ownerId = ownerId;
		this.title = title == null ? "" : title.trim();
		this.problem = problem == null ? "" : problem.trim();
		this.solution = solution == null ? "" : solution.trim();
		this.experience = experience == null ? "" : experience.trim();
	}
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public int getOwnerId() { return ownerId; }
	public void setOwnerId(int id) { this.ownerId = id;}
	public void setTitle(String title) {
		this.title = title == null ? "" : title.trim();
		
	}
	
	public void setProblem(String problem) {
		this.problem = problem == null ? "" : problem.trim();
		
	}
	public String getTitle() { return title; }
	public String getProblem() { return problem; }
	public String getSolution() { return solution; }
	public void setSolution(String solution) {
		this.solution = solution == null ? "" : solution.trim();
	}
	public void setExperience(String experience) {
		this.experience = experience == null ? "" : experience.trim();
		
	}
	public String getExperience() { return experience; }
	
	public static boolean isValidTitle(String title) {
		return title != null && !title.trim().isEmpty() && title.trim().length() <= 100;
	}
	
	public static boolean isValidProblem(String problem) {
		return problem != null && !problem.trim().isEmpty() && problem.trim().length() <= 2000;
		
	}
	
	public static boolean isValidSolution(String solution) {
		return solution != null && !solution.trim().isEmpty() && solution.trim().length() <= 2000;
	}
	
	public static boolean isValidExperience(String experience) {
		return experience == null || experience.trim().length() <= 2000;
	}
	
	// this displays the identification and title in the list
	@Override
	public String toString() {
		return id + ": " + title;
	}

}
