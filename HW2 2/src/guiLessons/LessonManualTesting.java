package guiLessons;

import java.nio.file.Files;
import java.nio.file.Path;
import applicationMain.FoundationsMain;
import entityClasses.User;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * <p>Title: LessonManualTesting Class.</p>
 * <p>Description: Opens an isolated contributor page for the manual procedures in
 * ContributorTests_Javadocs/ManualTests.md. F12 saves the visible lesson scene.
 * Reuse the same test home on successive launches to check persistence.</p>
 * <p>MT-01 checks create/read; MT-02 checks update and optional experience;
 * MT-03 checks deletion and the empty list; MT-04 checks selected subsets;
 * MT-05 checks input boundaries and helpful errors; MT-06 checks persistence
 * after restart; MT-07 checks scrolling and large subsets; MT-08 checks Back.
 * These procedures exercise Task 3's entities through Task 4's real MVC page.
 * The tester records observed results in ManualTests.md; opening the launcher
 * alone is not a completed manual test.</p>
 */
public final class LessonManualTesting {

	/** Prevents instantiation of the launcher. */
	private LessonManualTesting() { }

	/**
	 * Launches the manual fixture with a return page and owner ID zero.
	 * @param args unused; storage and evidence paths use contributor.test properties
	 * @throws Exception if the isolated storage directory cannot be created
	 */
	public static void main(String[] args) throws Exception {
		Path testHome = Path.of(System.getProperty("contributor.test.home", "ContributorTests_Javadocs/.work/manual-database"));
		Files.createDirectories(testHome);
		// Keep manual test accounts and lessons separate from normal application data.
		System.setProperty("user.home", testHome.toAbsolutePath().toString());
		Platform.startup(() -> {
			try {
				FoundationsMain.database.connectToDatabase();
				Stage stage = new Stage();
				stage.setScene(new Scene(new StackPane(new Label("Manual test return page")), 800, 600));
				stage.setTitle("Manual test home");
				// MT-01 through MT-07 use the same real contributor controls as the application.
				ViewLessons.displayLessons(stage, new User("Manual contributor", "", "", "", "", "", "", false, true, false, 0));
				stage.getScene().setOnKeyPressed(event -> {
					// F12 records actual visible state; it does not infer a pass or fail.
					if (event.getCode() == KeyCode.F12) {
						try {
							Path evidence = Path.of(System.getProperty("contributor.test.evidence", "ContributorTests_Javadocs/screenshots"));
							LessonTestEvidence.capture(stage.getScene().getRoot(), evidence.resolve("manual-" + System.currentTimeMillis() + ".png"));
						} catch (Exception failure) {
							failure.printStackTrace();
						}
					}
				});
				stage.setOnHidden(event -> {
					// MT-06 needs an orderly database close before the next launch reads the file.
					FoundationsMain.database.closeConnection();
					Platform.exit();
				});
			} catch (Exception failure) {
				failure.printStackTrace();
				Platform.exit();
				System.exit(1);
			}
		});
	}
}
