package guiUserLogin;
import java.util.Optional;
import javafx.scene.control.TextInputDialog;
import passwordPopUpWindow.Model;
import database.Database;
import entityClasses.User;
import javafx.stage.Stage;

/*******
 * <p> Title: ControllerUserLogin Class. </p>
 * 
 * <p> Description: The Java/FX-based User Login Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This controller determines if the log in is valid.  If so set up the link to the database, 
 * determines how many roles this user is authorized to play, and the calls one the of the array of
 * role home pages if there is only one role.  If there are more than one role, it setup up and
 * calls the multiple roles dispatch page for the user to determine which role the user wants to
 * play.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *  
 */

public class ControllerUserLogin {
	
	/*-********************************************************************************************

	The User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/

	/**
	 * Default constructor is not used.
	 */
	public ControllerUserLogin() {
	}

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	private static Stage theStage;	
	
	/**********
	 * <p> Method: public doLogin() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Login button. This
	 * method checks the username and password to see if they are valid.  If so, it then logs that
	 * user in my determining which role to use.
	 * 
	 * The method reaches batch to the view page and to fetch the information needed rather than
	 * passing that information as parameters.
	 * 
	 */	
	protected static void doLogin(Stage ts) {
		theStage = ts;
		String username = ViewUserLogin.text_Username.getText();
		String password = ViewUserLogin.text_Password.getText();
		
		// Check the input lengths before looking up the account
		if (username.isEmpty() || password.isEmpty() ||
				username.length() > 16 || password.length() > 64) {
			ViewUserLogin.alertUsernamePasswordError.setContentText(
					"Enter a username of at most 16 characters and a password "
					+ "of at most 64 characters.");
			ViewUserLogin.alertUsernamePasswordError.showAndWait();
			return;
		}
    	boolean loginResult = false;
    	
		// Fetch the user and verify the username
     	if (theDatabase.getUserAccountDetails(username) == false) {
     		// Don't provide too much information.  Don't say the username is invalid or the
     		// password is invalid.  Just say the pair is invalid.
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Incorrect username/password. Try again!");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
		// System.out.println("*** Username is valid");
		
		// Check to see that the login password matches the account password
    	String actualPassword = theDatabase.getCurrentPassword();
    	String oneTimePassword = theDatabase.getOneTimePasscode(username);
    	
		boolean usingOneTimePassword = oneTimePassword != null &&
				!oneTimePassword.isEmpty() && password.equals(oneTimePassword);
		if (usingOneTimePassword) {
			theDatabase.delOneTimePasscode(username);
			while (true) {
				TextInputDialog newPasswordDialog = new TextInputDialog();
				newPasswordDialog.setTitle("New Password");
				newPasswordDialog.setHeaderText("Enter your new password");
				Optional<String> newPassword = newPasswordDialog.showAndWait();
				if (newPassword.isEmpty()) return;

				String errorMessage = Model.evaluatePassword(newPassword.get());
				if (!errorMessage.isEmpty() || newPassword.get().equals(password)) {
					ViewUserLogin.alertUsernamePasswordError.setContentText(
						errorMessage.isEmpty() ? "Choose a different password." : errorMessage);
					ViewUserLogin.alertUsernamePasswordError.showAndWait();
					continue;
				}

				TextInputDialog confirmDialog = new TextInputDialog();
				confirmDialog.setTitle("Confirm Password");
				confirmDialog.setHeaderText("Enter the new password again");
				Optional<String> confirmation = confirmDialog.showAndWait();
				if (confirmation.isEmpty()) return;
				if (!newPassword.get().equals(confirmation.get())) {
					ViewUserLogin.alertUsernamePasswordError.setContentText(
							"The passwords do not match.");
					ViewUserLogin.alertUsernamePasswordError.showAndWait();
					continue;
				}

				theDatabase.updatePassword(username, newPassword.get());
				ViewUserLogin.alertUsernamePasswordError.setContentText(
						"Password updated. Please log in again using your new password.");
				ViewUserLogin.alertUsernamePasswordError.showAndWait();
				ViewUserLogin.displayUserLogin(theStage);
				return;
			}
		}
		if (!password.equals(actualPassword)) {
			ViewUserLogin.alertUsernamePasswordError.setContentText(
					"Incorrect username/password. Try again!");
			ViewUserLogin.alertUsernamePasswordError.showAndWait();
			return;
		}

		// System.out.println("*** Password is valid for this user");
		
		// Establish this user's details
    	User user = new User(username, password, theDatabase.getCurrentFirstName(), 
    			theDatabase.getCurrentMiddleName(), theDatabase.getCurrentLastName(), 
    			theDatabase.getCurrentPreferredFirstName(), theDatabase.getCurrentEmailAddress(), 
    			theDatabase.getCurrentAdminRole(), 
    			theDatabase.getCurrentNewRole1(), theDatabase.getCurrentNewRole2(), theDatabase.getCurrentUserId());
    	
    	// See which home page dispatch to use
		int numberOfRoles = theDatabase.getNumberOfRoles(user);		
		// System.out.println("*** The number of roles: "+ numberOfRoles);
		if (numberOfRoles == 1) {
			// Single Account Home Page - The user has no choice here
			
			// Admin role
			if (user.getAdminRole()) {
				loginResult = theDatabase.loginAdmin(user);
				if (loginResult) {
					guiAdminHome.ViewAdminHome.displayAdminHome(theStage, user);
				}
			} else if (user.getNewRole1()) {
				loginResult = theDatabase.loginRole1(user);
				if (loginResult) {
					guiRole1.ViewRole1Home.displayRole1Home(theStage, user);
				}
			} else if (user.getNewRole2()) {
				loginResult = theDatabase.loginRole2(user);
				if (loginResult) {
					guiRole2.ViewRole2Home.displayRole2Home(theStage, user);
				}
				// Other roles
			} else {
				System.out.println("***** UserLogin goToUserHome request has an invalid role");
			}
		} else if (numberOfRoles > 1) {
			// Multiple Account Home Page - The user chooses which role to play
			// System.out.println("*** Going to displayMultipleRoleDispatch");
			guiMultipleRoleDispatch.ViewMultipleRoleDispatch.
				displayMultipleRoleDispatch(theStage, user);
		}
	}
	
		
	/**********
	 * <p> Method: setup() </p>
	 * 
	 * <p> Description: This method is called to reset the page and then populate it with new
	 * content for the new user.</p>
	 * 
	 */
	protected static void doSetupAccount(Stage theStage, String invitationCode) {
		guiNewAccount.ViewNewAccount.displayNewAccount(theStage, invitationCode);
	}

	
	/**********
	 * <p> Method: public performQuit() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Quit button.  Doing
	 * this terminates the execution of the application.  All important data must be stored in the
	 * database, so there is no cleanup required.  (This is important so we can minimize the impact
	 * of crashed.)
	 * 
	 */	
	protected static void performQuit() {
		System.out.println("Perform Quit");
		System.exit(0);
	}	

}
