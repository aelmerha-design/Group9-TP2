package guiAdminHome;


import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import passwordPopUpWindow.Model;
import database.Database;

/*******
 * <p> Title: GUIAdminHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Admin Home Page.  This class provides the controller actions
 * basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains a number of buttons that have not yet been implemented.  WHen those buttons
 * are pressed, an alert pops up to tell the user that the function associated with the button has
 * not been implemented. Also, be aware that What has been implemented may not work the way the
 * final product requires and there maybe defects in this code.
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

public class ControllerAdminHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerAdminHome() {
	}
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**********
	 * <p> 
	 * 
	 * Title: performInvitation () Method. </p>
	 * 
	 * <p> Description: Protected method to send an email inviting a potential user to establish
	 * an account and a specific role. </p>
	 */
	protected static void performInvitation () {
		// Verify that the email address is valid - If not alert the user and return
		String emailAddress = ViewAdminHome.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(emailAddress)) {
			return;
		}
		
		// Check to ensure that we are not sending a second message with a new invitation code to
		// the same email address.  
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewAdminHome.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Inform the user that the invitation has been sent and display the invitation code
		String theSelectedRole = (String) ViewAdminHome.combobox_SelectRole.getValue();
		if (theSelectedRole == null || theSelectedRole.isEmpty()) {
			ViewAdminHome.alertEmailError.setContentText(
					"Select a role for the invitation.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole);
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress;
		System.out.println(msg);
		ViewAdminHome.alertEmailSent.setContentText(msg);
		ViewAdminHome.alertEmailSent.showAndWait();
		
		// Update the Admin Home pages status
		ViewAdminHome.text_InvitationEmailAddress.setText("");
		ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: manageInvitations () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void manageInvitations () {
		System.out.println("\n*** WARNING ***: Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
		ViewAdminHome.alertNotImplemented.setHeaderText("Manage Invitations Issue");
		ViewAdminHome.alertNotImplemented.setContentText("Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: setOnetimePassword () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void setOnetimePassword () {
		
		ChoiceDialog<String> userDialog = new ChoiceDialog<>(
				"<Select a User>", theDatabase.getUserList());
		userDialog.setHeaderText("Select a user");
		Optional<String> selectedUser = userDialog.showAndWait();
		
		if (selectedUser.isEmpty()) return;
		if (selectedUser.get().equals("<Select a User>")) {
			ViewAdminHome.alertNotImplemented.setHeaderText("Select a user.");
			ViewAdminHome.alertNotImplemented.setContentText("Select an existing user.");
			ViewAdminHome.alertNotImplemented.showAndWait();
			return;
		}
		
		TextInputDialog passwordDialog = new TextInputDialog();
		passwordDialog.setTitle("Set One Time Password");
		passwordDialog.setHeaderText("Enter a One Time Password for " + selectedUser.get());
		
		Optional<String> password = passwordDialog.showAndWait();
		
		if(password.isEmpty()) {
			return;
		}
		
		String errorMessage = Model.evaluatePassword(password.get());
		
		if(!errorMessage.equals("")) {
			ViewAdminHome.alertNotImplemented.setTitle("Invalid Password");
			ViewAdminHome.alertNotImplemented.setHeaderText(
			"The password does not meet the requirements.");
			
			ViewAdminHome.alertNotImplemented.setContentText(errorMessage);
			ViewAdminHome.alertNotImplemented.showAndWait();
			return;
			
			
		}
		
		theDatabase.setOneTimePasscode(
		   selectedUser.get(), password.get());
		ViewAdminHome.alertNotImplemented.setTitle("One Time Password");
		ViewAdminHome.alertNotImplemented.setHeaderText(
				"The one time password has been set.");
	    ViewAdminHome.alertNotImplemented.setContentText(
	    		"The one time password for " + selectedUser.get() + " has been  set.");
	    ViewAdminHome.alertNotImplemented.showAndWait();
	    
				
				
	}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		

	
	
	/**********
	 * <p> 
	 * 
	 * Title: deleteUser () Method. </p>
	 * 
	 * <p> Description: Select a user and delete account after admin confirms and the admin cant delete own account. </p>
	 */
	protected static void deleteUser() {
		ChoiceDialog<String> userDialog = new ChoiceDialog<>(
				"<Select a User>", theDatabase.getUserList());
		userDialog.setHeaderText("Select user to delete");
		Optional<String> selectedUser = userDialog.showAndWait();

		if (selectedUser.isEmpty()) return;
		if (selectedUser.get().equals("<Select a User>")) {
			ViewAdminHome.alertNotImplemented.setHeaderText("Select a user.");
			ViewAdminHome.alertNotImplemented.setContentText("Select an existing user.");
			ViewAdminHome.alertNotImplemented.showAndWait();
			return;
		}

		String username = selectedUser.get();
		if (username.equals(ViewAdminHome.theUser.getUserName())) {
			ViewAdminHome.alertNotImplemented.setTitle("Delete User");
			ViewAdminHome.alertNotImplemented.setHeaderText(
					"Your own account cant be deleted");
			ViewAdminHome.alertNotImplemented.setContentText(
					"Select another user.");
			ViewAdminHome.alertNotImplemented.showAndWait();
			return;
		}

		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
				"Delete account of " + username + "?",
				ButtonType.YES, ButtonType.NO);
		confirmation.setTitle("Delete User");
		confirmation.setHeaderText("Are you sure?");
		Optional<ButtonType> answer = confirmation.showAndWait();

		if (answer.isPresent() && answer.get() == ButtonType.YES) {
			theDatabase.deleteUser(username);
			ViewAdminHome.label_NumberOfUsers.setText(
					"Number of users: " + theDatabase.getNumberOfUsers());
		}
	}	
	/**********
	 * <p> 
	 * 
	 * Title: listUsers () Method. </p>
	 * 
	 * <p> Description: Display the username, name, email address, and roles of each of the users. </p>
	 */
	protected static void listUsers() {
		Alert userList = new Alert(Alert.AlertType.INFORMATION);
		userList.setTitle("User List");
		userList.setHeaderText("Registered users");
		userList.setContentText(theDatabase.getUserDetailsList());
		userList.getDialogPane().setMinWidth(550);
		userList.showAndWait();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: addRemoveRoles () Method. </p>
	 * 
	 * <p> Description: Protected method that allows an admin to add and remove roles for any of
	 * the users currently in the system.  This is done by invoking the AddRemoveRoles Page. There
	 * is no need to specify the home page for the return as this can only be initiated by and
	 * Admin.</p>
	 */
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Protected method that is intended to check an email address before it is
	 * used to reduce errors.  The code currently only checks to see that the email address is not
	 * empty.  In the future, a syntactic check must be performed and maybe there is a way to check
	 * if a properly email address is active.</p>
	 * 
	 * @param emailAddress	This String holds what is expected to be an email address
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		int firstAt = emailAddress.indexOf('@');
		if (emailAddress.isEmpty() || emailAddress.length() > 64 ||
				firstAt <= 0 || firstAt != emailAddress.lastIndexOf('@') ||
				firstAt == emailAddress.length() - 1 ||
				!emailAddress.substring(firstAt + 1).contains(".")) {
			ViewAdminHome.alertEmailError.setContentText(
					"Enter a valid email address with a maximum of 64 characters.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		return false;
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performLogout () Method. </p>
	 * 
	 * <p> Description: Protected method that logs this user out of the system and returns to the
	 * login page for future use.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performQuit () Method. </p>
	 * 
	 * <p> Description: Protected method that gracefully terminates the execution of the program.
	 * </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
