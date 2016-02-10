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

/**
 * A class that track progress of download. It acts as a observer to a download output stream.
 */
public class ProgressTracker {
    private ProgressBar progressBar;
    private String progressWindowTitle;
    private String progressWindowMessage;

    private Stage progressWindow;

    /**
     * Set progress window details
     *
     * @param progressWindowTitle title of progress window
     * @param progressWindowMessage
     */
    public void setProgressWindowDetails(String progressWindowTitle, String progressWindowMessage) {
        this.progressWindowTitle = progressWindowTitle;
        this.progressWindowMessage = progressWindowMessage;
    }

    private void createProgressWindow() {
        Stage stage = new Stage();
        assert progressWindowTitle != null;
        stage.setTitle(progressWindowTitle);

        Label downloadLabel = new Label();
        assert progressWindowMessage != null;
        downloadLabel.setText(progressWindowMessage);

        progressBar = new ProgressBar(-1.0);
        progressBar.setPrefWidth(400);

        VBox downloadProgressWindowLayout = new VBox();
        downloadProgressWindowLayout.setSpacing(20);
        downloadProgressWindowLayout.setPadding(new Insets(20));
        downloadProgressWindowLayout.setAlignment(Pos.CENTER_LEFT);
        downloadProgressWindowLayout.getChildren().addAll(downloadLabel, progressBar);

        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);

        scene.setRoot(downloadProgressWindowLayout);

        stage.show();

        progressWindow = stage;
    }

    /**
     * Set value of progress
     *
     * @param currentValue progress value between 0.0 to 1.0 or -1.0 for loading
     */
    public void setProgressValue(double currentValue) {
        assert currentValue == -1.0 || currentValue >= 0.0 && currentValue <= 1.0;

        if (progressWindow != null) {
            Platform.runLater(() -> progressBar.setProgress(currentValue));
        }
    }

    /**
     * Hides the progress window if window is present.
     */
    public void hideProgressWindow() {
        if (progressWindow != null) {
            Platform.runLater(() -> progressWindow.hide());
        }
    }

    /**
     * Show progress window; if window is not present, create it
     */
    public void showProgressWindow() {
        if (progressWindow == null) {
            Platform.runLater(() -> createProgressWindow());
        } else {
            Platform.runLater(() -> progressWindow.show());
        }
    }
}
