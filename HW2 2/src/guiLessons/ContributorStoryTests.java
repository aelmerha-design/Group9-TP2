package guiLessons;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import applicationMain.FoundationsMain;
import entityClasses.Lesson;
import entityClasses.LessonList;
import entityClasses.User;

/**
 * <p>Title: ContributorStoryTests Class.</p>
 * <p>Description: Named positive and negative scenarios derived from the supplied
 * Contributor (Phase Two) stories. C01-C12 refer to RequirementsToTests.md.
 * Each method documents its requirement, implementation, and output oracle.
 * Runs inside LessonTestingAutomation on the JavaFX thread and its isolated H2
 * connection. Admin is the privileged-role fixture; Phase Three AI behavior is
 * design scope, not claimed as implemented.</p>
 */
public final class ContributorStoryTests {

	/** Owner fixture; independent of the original regression suite's owner zero. */
	private static final User owner = actor(101, false);
	/** A different contributor used to challenge privacy enforcement. */
	private static final User other = actor(102, false);
	/** Admin represents the other role permitted to edit, delete, and lock. */
	private static final User admin = actor(999, true);
	/** Fixture identities removed after this suite, including failed scenarios. */
	private static final List<Integer> created = new ArrayList<>();
	/** Marker identifies the independent story fixture between JVMs. */
	private static final Path restart = Path.of(System.getProperty("user.home"), "story-restart.txt");

	/** Prevents construction of a static test suite. */
	private ContributorStoryTests() { }

	/** Creates a fixture identity without testing authentication.
	 * @param id fixture owner identity
	 * @param privileged whether this fixture represents an admin
	 * @return requesting user */
	private static User actor(int id, boolean privileged) {
		return new User(privileged ? "Story admin" : "Contributor " + id, "", "", "", "", "", "",
				privileged, !privileged, false, id);
	}

	/** Creates a valid core-content fixture and tracks it for cleanup.
	 * @return saved fixture
	 * @throws Exception if persistence fails */
	private static Lesson fixture() throws Exception {
		Lesson lesson = new Lesson(0, owner.getUserId(), "Story lesson", "A repeated attempt failed",
				"Revise the approach", "Initial attempt: reviewed design; 1 hour; Alex 1 person-hour");
		created.add(ModelLessons.createLesson(lesson, owner));
		return lesson;
	}

	/** Prints an independently named assertion and updates the Foundations-style totals.
	 * @param name scenario/method name
	 * @param passed whether the documented expectation was observed */
	private static void check(String name, boolean passed) {
		System.out.println("\nStory test " + name + ": " + (passed ? "Success" : "Failure"));
		if (passed) LessonTestingAutomation.numPassed++;
		else LessonTestingAutomation.numFailed++;
	}

	/** Checked action used to distinguish expected permission denial from other failures. */
	@FunctionalInterface
	private interface Action {
		/** Executes the challenged operation.
		 * @throws Exception if the operation fails */
		void run() throws Exception;
	}

	/** Accepts only the documented security exception; SQL/programming errors propagate.
	 * @param action challenged actor-aware operation
	 * @return whether access was denied
	 * @throws Exception if a different failure occurs */
	private static boolean denied(Action action) throws Exception {
		try { action.run(); return false; }
		catch (SecurityException expected) { return true; }
	}

	/** Runs every first-process story scenario and restores the previous view identity.
	 * @throws Exception if setup, cleanup, or an unexpected operation fails */
	public static void run() throws Exception {
		User previous = ViewLessons.currentUser;
		try {
			testCreateValid(); testCreateInvalid();
			testOwnListAndRead(); testForeignAccessDenied();
			testOtherRoleEditVisible(); testOtherRoleDeletionVisible();
			testUnlockedEdit(); testLockedEditsAndIndicators();
			testUnlockedDelete(); testLockedDeletionDenied();
			testMetadataHidden(); testMetadataPreserved();
			testExperienceAppend(); testExperienceRejected();
			testExperienceHistoryButton(); testExperienceForeignOrLocked();
			testListOperations(); testListEdges();
			testBatchLocks(); testBatchRollback();
			testBoundaryAccepted(); testBoundaryRejected();
			prepareRestart();
		} finally {
			for (int lessonId : created) FoundationsMain.database.deleteLesson(lessonId);
			created.clear();
			ViewLessons.currentUser = previous;
			ViewLessons.refreshList();
		}
	}

	/** C01 positive: create valid core information through the actor-aware model.
	 * Success means generated identity, owner, and stored core fields agree.
	 * @throws Exception if fixture persistence fails */
	public static void testCreateValid() throws Exception {
		// C01-P: Check the saved record rather than relying on a returned ID alone.
		Lesson lesson = fixture();
		Lesson saved = ModelLessons.readLesson(lesson.getId(), owner);
		check("C01-P testCreateValid", saved != null && saved.getId() > 0
				&& saved.getOwnerId() == owner.getUserId() && saved.getProblem().equals(lesson.getProblem()));
	}

	/** C01 negative: invalid core input and forged ownership cannot create content.
	 * Success means invalid input returns -1, spoofing is denied, and count is unchanged.
	 * @throws Exception if fixture persistence fails */
	public static void testCreateInvalid() throws Exception {
		int count = ModelLessons.readLessons(owner).getLessons().size();
		Lesson invalid = new Lesson(0, owner.getUserId(), " ", "Problem", "Solution", "");
		check("C01-N testCreateInvalid", ModelLessons.createLesson(invalid, owner) == -1
				&& denied(() -> ModelLessons.createLesson(new Lesson(0, other.getUserId(), "Fake", "P", "S", ""), owner))
				&& ModelLessons.readLessons(owner).getLessons().size() == count);
	}

	/** C02 positive: owner listing and direct selection expose the owned lesson.
	 * Success means the owner can list/read the fixture and another contributor cannot list it.
	 * @throws Exception if persistence fails */
	public static void testOwnListAndRead() throws Exception {
		Lesson lesson = fixture();
		check("C02-P testOwnListAndRead", ModelLessons.readLessons(owner).getLesson(lesson.getId()) != null
				&& ModelLessons.readLesson(lesson.getId(), owner) != null
				&& ModelLessons.readLessons(other).getLesson(lesson.getId()) == null);
	}

	/** C02 negative: challenge ID-based read/update/delete with a different contributor.
	 * Success means all three operations are denied and saved core content remains intact.
	 * @throws Exception if an unexpected failure occurs */
	public static void testForeignAccessDenied() throws Exception {
		Lesson lesson = fixture();
		check("C02-N testForeignAccessDenied", denied(() -> ModelLessons.readLesson(lesson.getId(), other))
				&& denied(() -> ModelLessons.updateLesson(lesson, other))
				&& denied(() -> ModelLessons.deleteLesson(lesson.getId(), other))
				&& ModelLessons.readLesson(lesson.getId(), owner).getTitle().equals("Story lesson"));
		check("C02-N unsigned actor", denied(() -> ModelLessons.readLessons((User) null))
				&& denied(() -> ModelLessons.readLesson(lesson.getId(), null)));
	}

	/** C03 positive: another privileged role edits contributor-authored core information.
	 * Success means the contributor rereads the edited content under the same identity.
	 * @throws Exception if persistence fails */
	public static void testOtherRoleEditVisible() throws Exception {
		Lesson lesson = fixture();
		lesson.setSolution("Admin corrected contributor solution");
		check("C03-P testOtherRoleEditVisible", ModelLessons.updateLesson(lesson, admin)
				&& ModelLessons.readLesson(lesson.getId(), owner).getSolution().equals(lesson.getSolution()));
	}

	/** C03 second scenario: privileged deletion is reflected in the contributor's list and read.
	 * Success means the deleted record is absent through both paths.
	 * @throws Exception if persistence fails */
	public static void testOtherRoleDeletionVisible() throws Exception {
		Lesson lesson = fixture();
		check("C03-D testOtherRoleDeletionVisible", ModelLessons.deleteLesson(lesson.getId(), admin)
				&& ModelLessons.readLesson(lesson.getId(), owner) == null
				&& ModelLessons.readLessons(owner).getLesson(lesson.getId()) == null);
	}

	/** C04 positive: a contributor can change unlocked content despite another field's lock.
	 * Success means the unlocked solution changes and the title's saved lock survives.
	 * @throws Exception if persistence fails */
	public static void testUnlockedEdit() throws Exception {
		Lesson lesson = fixture();
		ModelLessons.toggleLock(lesson.getId(), ModelLessons.LessonField.TITLE, admin);
		lesson.setSolution("Unlocked solution changed");
		check("C04-P testUnlockedEdit", ModelLessons.updateLesson(lesson, owner)
				&& ModelLessons.readLesson(lesson.getId(), owner).isTitleLocked());
	}

	/** C04 negative: test each individual lock and the whole-lesson lock.
	 * Success means programmatic edits are denied, GUI editability/labels agree, and
	 * title content is unchanged. Contributor lock controls are hidden and denied.
	 * @throws Exception if persistence or evidence fails */
	public static void testLockedEditsAndIndicators() throws Exception {
		for (ModelLessons.LessonField field : ModelLessons.LessonField.values()) {
			Lesson lesson = fixture();
			ModelLessons.toggleLock(lesson.getId(), field, admin);
			Lesson changed = ModelLessons.readLesson(lesson.getId(), owner);
			switch (field) {
				case TITLE -> changed.setTitle("Denied");
				case PROBLEM -> changed.setProblem("Denied");
				case SOLUTION -> changed.setSolution("Denied");
				case EXPERIENCE -> changed.setExperience("Denied");
			}
			ViewLessons.currentUser = owner;
			ViewLessons.showLesson(ModelLessons.readLesson(lesson.getId(), owner));
			boolean[] editable = {ViewLessons.text_Title.isEditable(), ViewLessons.text_Problem.isEditable(),
					ViewLessons.text_Solution.isEditable(), ViewLessons.text_Experience.isEditable()};
			check("C04-N locked " + field, denied(() -> ModelLessons.updateLesson(changed, owner))
					&& !editable[field.ordinal()] && ViewLessons.fieldLabels[field.ordinal()].getText().contains("[locked]"));
		}
		Lesson whole = fixture();
		ModelLessons.setLessonsLocked(List.of(whole.getId()), true, admin);
		whole.setTitle("Denied whole edit");
		check("C04-N whole lesson", denied(() -> ModelLessons.updateLesson(whole, owner))
				&& ModelLessons.readLesson(whole.getId(), owner).getTitle().equals("Story lesson"));
		check("C04-N contributor cannot change locks", denied(() -> ModelLessons.toggleLock(whole.getId(), ModelLessons.LessonField.TITLE, owner))
				&& denied(() -> ModelLessons.setLessonsLocked(List.of(whole.getId()), false, owner)));
		ViewLessons.displayLessons(ViewLessons.theStage, owner);
		ViewLessons.showLesson(ModelLessons.readLesson(whole.getId(), owner));
		check("C04-N admin controls hidden", !ViewLessons.lockButtons[0].isVisible() && !ViewLessons.button_LockSelected.isVisible());
		capture("09-contributor-locked-fields");
	}

	/** C05 positive: an unlocked owned lesson can be deleted.
	 * Success means delete returns true and reread returns null.
	 * @throws Exception if persistence fails */
	public static void testUnlockedDelete() throws Exception {
		Lesson lesson = fixture();
		check("C05-P testUnlockedDelete", ModelLessons.deleteLesson(lesson.getId(), owner)
				&& ModelLessons.readLesson(lesson.getId(), owner) == null);
	}

	/** C05 negative: each field lock, then a whole lock, prevents contributor deletion.
	 * Success means each denial preserves its record; unlocking permits deletion.
	 * @throws Exception if persistence fails */
	public static void testLockedDeletionDenied() throws Exception {
		for (ModelLessons.LessonField field : ModelLessons.LessonField.values()) {
			Lesson lesson = fixture();
			ModelLessons.toggleLock(lesson.getId(), field, admin);
			check("C05-N delete locked " + field, denied(() -> ModelLessons.deleteLesson(lesson.getId(), owner))
					&& ModelLessons.readLesson(lesson.getId(), owner) != null);
			ModelLessons.toggleLock(lesson.getId(), field, admin);
			check("C05-P delete after unlock " + field, ModelLessons.deleteLesson(lesson.getId(), owner));
		}
		Lesson whole = fixture();
		ModelLessons.setLessonsLocked(List.of(whole.getId()), true, admin);
		check("C05-N delete whole locked", denied(() -> ModelLessons.deleteLesson(whole.getId(), owner))
				&& ModelLessons.readLesson(whole.getId(), owner) != null);
	}

	/** C06 positive/privacy: store other-role metadata, then request contributor content.
	 * Success means admin sees persisted metadata and contributor receives no additions.
	 * @throws Exception if persistence fails */
	public static void testMetadataHidden() throws Exception {
		Lesson lesson = fixture();
		lesson.setSearchTerms("PRIVATE CURATOR TERMS"); lesson.setTimeHours(4); lesson.setEffortHours(7); lesson.setStatsVisible(true);
		FoundationsMain.database.updateLessonMetadata(lesson);
		Lesson restricted = ModelLessons.readLesson(lesson.getId(), owner);
		check("C06-P testMetadataHidden", ModelLessons.readLesson(lesson.getId(), admin).getSearchTerms().equals("PRIVATE CURATOR TERMS")
				&& restricted.getSearchTerms().isEmpty() && restricted.getTimeHours() == 0
				&& restricted.getEffortHours() == 0 && !restricted.isStatsVisible()
				&& ModelLessons.readLessons(owner).getLesson(lesson.getId()).getSearchTerms().isEmpty());
	}

	/** C06 negative: contributor tries to replace other-role metadata along with a core edit.
	 * Success means the core edit succeeds but stored private additions remain unchanged.
	 * @throws Exception if persistence fails */
	public static void testMetadataPreserved() throws Exception {
		Lesson lesson = fixture();
		lesson.setSearchTerms("PRIVATE PRESERVE"); lesson.setTimeHours(2);
		FoundationsMain.database.updateLessonMetadata(lesson);
		Lesson edited = ModelLessons.readLesson(lesson.getId(), owner);
		edited.setSearchTerms("FORGED"); edited.setTimeHours(99); edited.setTitle("Allowed core edit");
		check("C06-N testMetadataPreserved", ModelLessons.updateLesson(edited, owner)
				&& ModelLessons.readLesson(lesson.getId(), admin).getSearchTerms().equals("PRIVATE PRESERVE")
				&& ModelLessons.readLesson(lesson.getId(), admin).getTimeHours() == 2);
	}

	/** C07 positive: append two attempts describing what/how/time/individual team effort.
	 * Success means both entries remain in exact insertion order.
	 * @throws Exception if persistence fails */
	public static void testExperienceAppend() throws Exception {
		Lesson lesson = fixture();
		String first = "What: retry diagrams; how: pair review; time: 2 hours; Alex: 1, Sam: 2 person-hours";
		String second = "What: refine diagrams; how: prototype; time: 1 hour; Alex: 1 person-hour";
		check("C07-P testExperienceAppend", ModelLessons.addExperience(lesson.getId(), first, owner)
				&& ModelLessons.addExperience(lesson.getId(), second, owner)
				&& ModelLessons.readExperiences(lesson.getId(), owner).equals(List.of(first, second)));
	}

	/** C07 negative: blank/overlong/null attempts are rejected without modifying history.
	 * Success means every invalid append returns false and history is unchanged.
	 * @throws Exception if persistence fails */
	public static void testExperienceRejected() throws Exception {
		Lesson lesson = fixture();
		ModelLessons.addExperience(lesson.getId(), "Existing attempt", owner);
		check("C07-N testExperienceRejected", !ModelLessons.addExperience(lesson.getId(), " ", owner)
				&& !ModelLessons.addExperience(lesson.getId(), null, owner)
				&& !ModelLessons.addExperience(lesson.getId(), "E".repeat(2001), owner)
				&& ModelLessons.readExperiences(lesson.getId(), owner).equals(List.of("Existing attempt")));
	}

	/** C07 GUI positive: invoke the actual Add experience and Experience history buttons.
	 * Success means the button appends stored text and the actual dialog displays it.
	 * @throws Exception if persistence or evidence fails */
	public static void testExperienceHistoryButton() throws Exception {
		Lesson lesson = fixture();
		ViewLessons.currentUser = owner;
		ViewLessons.refreshList();
		ViewLessons.list_Lessons.getSelectionModel().select(ViewLessons.displayedLessons.getLesson(lesson.getId()));
		ViewLessons.text_Experience.setText("What: retest; how: review; time: 1 hour; Sam: 1 person-hour");
		for (javafx.scene.Node node : ViewLessons.theStage.getScene().getRoot().lookupAll(".button")) {
			if (node instanceof javafx.scene.control.Button button && button.getText().equals("Add experience")) button.fire();
		}
		check("C07-P append button", ModelLessons.readExperiences(lesson.getId(), owner).size() == 1);
		javafx.application.Platform.runLater(() -> {
			for (javafx.stage.Window window : javafx.stage.Window.getWindows().toArray(new javafx.stage.Window[0])) {
				if (window.getScene() != null && window.getScene().getRoot() instanceof javafx.scene.control.DialogPane pane) {
					check("C07-P history dialog", pane.getContentText().contains("Sam: 1 person-hour"));
					try { captureNode(pane, "10-experience-history"); } catch (Exception failure) { throw new RuntimeException(failure); }
					((javafx.scene.control.Button) pane.lookupButton(javafx.scene.control.ButtonType.OK)).fire();
				}
			}
		});
		for (javafx.scene.Node node : ViewLessons.theStage.getScene().getRoot().lookupAll(".button")) {
			if (node instanceof javafx.scene.control.Button button && button.getText().equals("Experience history")) button.fire();
		}
	}

	/** C07 negative/privacy: another contributor cannot append or read history; locks block append.
	 * Success means all denied operations leave an empty history.
	 * @throws Exception if persistence fails */
	public static void testExperienceForeignOrLocked() throws Exception {
		Lesson lesson = fixture();
		ModelLessons.toggleLock(lesson.getId(), ModelLessons.LessonField.EXPERIENCE, admin);
		check("C07-N testExperienceForeignOrLocked", denied(() -> ModelLessons.addExperience(lesson.getId(), "Other attempt", other))
				&& denied(() -> ModelLessons.readExperiences(lesson.getId(), other))
				&& denied(() -> ModelLessons.addExperience(lesson.getId(), "Locked attempt", owner))
				&& ModelLessons.readExperiences(lesson.getId(), owner).isEmpty());
	}

	/** C09 positive: list add/read/update/remove and a selected keyword subset.
	 * Success means intended entries change and the source list survives subset removal. */
	public static void testListOperations() {
		LessonList list = new LessonList();
		list.addLesson(new Lesson(1, 101, "Alpha", "P", "S", ""));
		list.addLesson(new Lesson(2, 101, "Beta", "P", "S", ""));
		LessonList subset = list.searchByKeyword("alpha");
		check("C09-P testListOperations", list.updateLesson(new Lesson(2, 101, "Edited", "P", "S", ""))
				&& subset.removeLesson(1) && list.getLesson(1) != null && list.getLesson(2).getTitle().equals("Edited"));
	}

	/** C09 negative: duplicate and missing identities cannot corrupt a list.
	 * Success means duplicate insertion is ignored and missing removal returns false. */
	public static void testListEdges() {
		LessonList list = new LessonList();
		list.addLesson(new Lesson(1, 101, "Original", "P", "S", ""));
		list.addLesson(new Lesson(1, 101, "Duplicate", "P", "S", ""));
		check("C09-N testListEdges", list.getLessons().size() == 1 && !list.removeLesson(999)
				&& list.getLesson(1).getTitle().equals("Original") && list.searchByKeyword("absent").getLessons().isEmpty());
	}

	/** C10 positive: batch-lock and unlock two lessons while retaining individual locks.
	 * Success means both whole locks change and the independent title lock survives.
	 * @throws Exception if persistence fails */
	public static void testBatchLocks() throws Exception {
		Lesson first = fixture(); Lesson second = fixture();
		ModelLessons.toggleLock(first.getId(), ModelLessons.LessonField.TITLE, admin);
		ModelLessons.setLessonsLocked(List.of(first.getId(), second.getId()), true, admin);
		check("C10-P batch lock", ModelLessons.readLesson(first.getId(), admin).isLocked() && ModelLessons.readLesson(second.getId(), admin).isLocked());
		ModelLessons.setLessonsLocked(List.of(first.getId(), second.getId()), false, admin);
		check("C10-P batch unlock", !ModelLessons.readLesson(first.getId(), admin).isLocked()
				&& !ModelLessons.readLesson(second.getId(), admin).isLocked() && ModelLessons.readLesson(first.getId(), admin).isTitleLocked());
	}

	/** C10 negative: a missing ID in a batch must roll back the valid ID's change.
	 * Success means SQL rejection leaves the valid record unlocked and permits later writes.
	 * @throws Exception if an unexpected persistence error occurs */
	public static void testBatchRollback() throws Exception {
		Lesson lesson = fixture();
		boolean rejected = false;
		try { ModelLessons.setLessonsLocked(List.of(lesson.getId(), Integer.MAX_VALUE), true, admin); }
		catch (java.sql.SQLException expected) { rejected = true; }
		check("C10-N testBatchRollback", rejected && !ModelLessons.readLesson(lesson.getId(), admin).isLocked()
				&& ModelLessons.updateLesson(lesson, owner));
	}

	/** C11 positive: accept exact required-field maxima and optional empty experience.
	 * Success means the application's validator accepts both minimum and maximum input. */
	public static void testBoundaryAccepted() {
		check("C11-P testBoundaryAccepted", ModelLessons.validateLesson(new Lesson(0, 101, "T", "P", "S", "")).isEmpty()
				&& ModelLessons.validateLesson(new Lesson(0, 101, "T".repeat(100), "P".repeat(2000), "S".repeat(2000), "E".repeat(2000))).isEmpty());
	}

	/** C11 negative: reject blank required content and values above each maximum.
	 * Success means all five invalid fixtures produce a validation error. */
	public static void testBoundaryRejected() {
		for (Lesson invalid : new Lesson[] {new Lesson(0,101," ","P","S",""),
				new Lesson(0,101,"T".repeat(101),"P","S",""), new Lesson(0,101,"T","P".repeat(2001),"S",""),
				new Lesson(0,101,"T","P","S".repeat(2001),""), new Lesson(0,101,"T","P","S","E".repeat(2001))})
			check("C11-N testBoundaryRejected", !ModelLessons.validateLesson(invalid).isEmpty());
	}

	/** Prepares C08/C12's independent owner-77 record without affecting owner-zero counts.
	 * @throws Exception if saving the fixture or marker fails */
	private static void prepareRestart() throws Exception {
		Lesson lesson = new Lesson(0, 77, "Story restart", "P", "S", "");
		lesson.create(FoundationsMain.database);
		lesson.setLocked(true); lesson.setProblemLocked(true); lesson.setSearchTerms("PRIVATE RESTART");
		lesson.setTimeHours(3); lesson.setEffortHours(4); lesson.setStatsVisible(true);
		FoundationsMain.database.updateLessonLocks(lesson);
		FoundationsMain.database.updateLessonMetadata(lesson);
		lesson.addExperience(FoundationsMain.database, "Attempt one; time 1h; Alex 1 person-hour");
		lesson.addExperience(FoundationsMain.database, "Attempt two; time 2h; Sam 2 person-hours");
		Files.writeString(restart, Integer.toString(lesson.getId()));
	}

	/** C08/C12 positive: a new JVM must restore lock state, metadata, and attempt order.
	 * Success means these persisted values survive closing the original connection.
	 * @throws Exception if reading the fixture fails */
	public static void testRestartRestoresState() throws Exception {
		int lessonId = Integer.parseInt(Files.readString(restart));
		Lesson lesson = ModelLessons.readLesson(lessonId, admin);
		check("C08-P testRestartRestoresState", lesson != null && lesson.isLocked() && lesson.isProblemLocked()
				&& lesson.getSearchTerms().equals("PRIVATE RESTART") && lesson.getTimeHours() == 3
				&& ModelLessons.readExperiences(lessonId, admin).equals(List.of(
						"Attempt one; time 1h; Alex 1 person-hour", "Attempt two; time 2h; Sam 2 person-hours")));
		check("C12-P future attributes persisted", lesson.getSearchTerms().equals("PRIVATE RESTART")
				&& lesson.getTimeHours() == 3 && lesson.getEffortHours() == 4 && lesson.isStatsVisible());
	}

	/** C08/C12 negative: restart must not bypass locks or reveal restricted attributes.
	 * Success means edit/delete are denied and the contributor projection hides metadata.
	 * @throws Exception if fixture access or cleanup fails */
	public static void testRestartKeepsRestrictions() throws Exception {
		int lessonId = Integer.parseInt(Files.readString(restart));
		User contributor = actor(77, false);
		Lesson visible = ModelLessons.readLesson(lessonId, contributor);
		visible.setTitle("Forbidden after restart");
		check("C08-N testRestartKeepsRestrictions", denied(() -> ModelLessons.updateLesson(visible, contributor))
				&& denied(() -> ModelLessons.deleteLesson(lessonId, contributor)));
		check("C12-N future attributes restricted", visible.getSearchTerms().isEmpty()
				&& visible.getTimeHours() == 0 && visible.getEffortHours() == 0 && !visible.isStatsVisible());
		FoundationsMain.database.deleteLesson(lessonId);
		Files.delete(restart);
	}

	/** Saves optional evidence of the actual scene.
	 * @param name descriptive filename stem
	 * @throws Exception if evidence writing fails */
	private static void capture(String name) throws Exception { captureNode(ViewLessons.theStage.getScene().getRoot(), name); }

	/** Saves optional evidence of a real rendered node.
	 * @param node scene or dialog
	 * @param name descriptive filename stem
	 * @throws Exception if writing fails */
	private static void captureNode(javafx.scene.Node node, String name) throws Exception {
		String directory = System.getProperty("contributor.test.evidence");
		if (directory != null) LessonTestEvidence.capture(node, Path.of(directory, name + ".png"));
	}
}
