package updater;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressBar;

/**
 * Listener for download progress
 */
public class DownloadProgressTracker implements ChangeListener<Number> {
    private final String name;
    private final DoubleProperty progressProperty;

    /**
     *
     * @param downloadName name of item being downloaded for identification purpose
     */
    public DownloadProgressTracker(String downloadName) {
        this.name = downloadName;
        this.progressProperty = new SimpleDoubleProperty(-1);
    }

    /**
     * Get name of item being tracked
     *
     * @return name of item being downloaded
     */
    public String getDownloadName() {
        return this.name;
    }

    /**
     * Use -1 to indicate loading. Otherwise, value is expected to be between 0 to 1 inclusive
     *
     * @param progress value of progress. Expected to be -1 (loading) or between 0 to 1 inclusive
     */
    private void setProgress(double progress) {
        assert progress == -1.0 || progress >= 0.0 && progress <= 1.0;

        this.progressProperty.set(progress);
    }

    /**
     * add progress bar (UI element) to listen to download progress
     *
     * @param progressBar progress bar that will listen to progress
     */
    public void addProgressBarToListen(ProgressBar progressBar) {
        progressProperty.addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
                        Platform.runLater(() -> progressBar.setProgress(newValue.doubleValue())));
    }

    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        setProgress(newValue.doubleValue());
    }
}
