package updater;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is the subject in observer pattern where the observer is a progress tracker.
 */
public class DownloadCountingOutputStream extends CountingOutputStream {
    private final long totalFileSizeInBytes;
    private final DoubleProperty percentageDownloaded; // 0.0 to 1.0

    public DownloadCountingOutputStream(OutputStream out, long totalFileSizeInBytes) {
        super(out);

        this.totalFileSizeInBytes = totalFileSizeInBytes;
        percentageDownloaded = new SimpleDoubleProperty(0);
    }

    public void addListener(DownloadProgressTracker downloadProgressTracker) {
        percentageDownloaded.addListener(downloadProgressTracker);
    }

    /**
     * {@inheritDoc}
     *
     * Notify progress tracker of current download progress.
     */
    @Override
    protected void afterWrite(int n) throws IOException {
        super.afterWrite(n);

        percentageDownloaded.set(1.0 * this.getByteCount() / this.totalFileSizeInBytes);
    }
}
