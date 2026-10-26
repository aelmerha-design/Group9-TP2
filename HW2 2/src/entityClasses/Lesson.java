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
	public String getTitle() { return title; }
	public String getProblem() { return problem; }
	public String getSolution() { return solution; }
	public String getExperience() { return experience; }
	
	// this displays the identification and title in the list
	@Override
	public String toString() {
		return id + ": " + title;
	}

}
