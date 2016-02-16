package updater;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressBar;

/**
 *
 */
public class DownloadProgressTracker implements ChangeListener<Number> {
    private final String name;
    private final DoubleProperty progressProperty;

    public DownloadProgressTracker(String downloadName) {
        this.name = downloadName;
        this.progressProperty = new SimpleDoubleProperty(-1);
    }

    public String getDownloadName() {
        return this.name;
    }

    public void setProgress(double progress) {
        assert progress == -1.0 || progress >= 0.0 && progress <= 1.0;

        this.progressProperty.set(progress);
    }

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
