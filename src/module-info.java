/*******
 * <p> Title: TP2 Application Module. </p>
 *
 * <p> Description: Declares the JavaFX and JDBC dependencies used by the account
 * pages and Contributor lesson/experience CRUD, and opens the launch package to
 * JavaFX for application initialization. </p>
 */
module FoundationsF26 {
	requires javafx.controls;
	requires java.sql;
	requires javafx.fxml;
	
	opens applicationMain to javafx.graphics, javafx.fxml;
}