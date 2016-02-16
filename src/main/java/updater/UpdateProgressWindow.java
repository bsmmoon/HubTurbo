package updater;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * UpdateProgressWindow is the class that will display update download progresses
 */
public class UpdateProgressWindow {
    private static final String WINDOW_TITLE = "Update Progress";
    private Stage window;
    private final List<DownloadProgressTracker> downloads;

    public UpdateProgressWindow() {
        this.downloads = new ArrayList<>();
    }

    /**
     * shows update download progress window
     */
    public void showWindow() {
        if (window == null) {
            Platform.runLater(this::createProgressWindow);
        } else {
            Platform.runLater(window::show);
        }
    }

    /**
     * hides update download progress window
     */
    public void hideWindow() {
        if (window != null) {
            Platform.runLater(window::hide);
        }
    }

    /**
     * If there is no download tracker, shows that there is no update being downloaded.
     * Else, show download progress bar(s).
     */
    private void createProgressWindow() {
        Stage stage = new Stage();
        stage.setTitle(WINDOW_TITLE);

        VBox downloadsContainer = new VBox();

        if (downloads.isEmpty()) {
            Label noDownloadLabel = new Label();
            noDownloadLabel.setText("There is no update being downloaded.");
            noDownloadLabel.setPadding(new Insets(50));

            downloadsContainer.getChildren().add(noDownloadLabel);
        } else {
            for (DownloadProgressTracker progressTracker : downloads) {
                Label downloadLabel = new Label();
                downloadLabel.setText("Downloading " + progressTracker.getDownloadName() + "...");

                ProgressBar progressBar = new ProgressBar(-1.0);
                progressBar.setPrefWidth(400);
                progressTracker.addProgressBarToListen(progressBar);

                VBox downloadProgressItem = new VBox();
                downloadProgressItem.setSpacing(20);
                downloadProgressItem.setPadding(new Insets(20));
                downloadProgressItem.setAlignment(Pos.CENTER_LEFT);
                downloadProgressItem.getChildren().addAll(downloadLabel, progressBar);

                downloadsContainer.getChildren().add(downloadProgressItem);
            }
        }

        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);

        scene.setRoot(downloadsContainer);

        stage.show();

        window = stage;
    }

    /**
     * Get a download progress tracker that will be shown in the update progress window
     *
     * @param downloadName name of item being downloaded for labeling in window
     * @return progress tracker that can listen
     */
    public DownloadProgressTracker getNewDownloadProgressTracker(String downloadName) {
        DownloadProgressTracker progressTracker = new DownloadProgressTracker(downloadName);

        downloads.add(progressTracker);

        window = null; // to reset the layout

        return progressTracker;
    }

    /**
     * Remove a download progress tracker from update progress window
     *
     * This should be called after download is done
     *
     * @param progressTracker progress tracker to be removed from window
     */
    public void removeDownloadProgressTracker(DownloadProgressTracker progressTracker) {
        downloads.remove(progressTracker);

        window = null; // to reset the layout
    }
}
