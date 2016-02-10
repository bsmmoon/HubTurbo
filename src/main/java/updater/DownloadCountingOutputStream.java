package updater;

import org.apache.commons.io.output.CountingOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

/**
 * This class is the subject in observer pattern where the observer is a progress tracker.
 */
public class DownloadCountingOutputStream extends CountingOutputStream {
    private final ProgressTracker progressTracker;
    private final long totalFileSizeInBytes;

    public DownloadCountingOutputStream(OutputStream out, ProgressTracker progressTracker, long totalFileSizeInBytes) {
        super(out);

        this.progressTracker = progressTracker;

        this.totalFileSizeInBytes = totalFileSizeInBytes;
    }

    /**
     * {@inheritDoc}
     *
     * Notify progress tracker of current download progress.
     */
    @Override
    protected void afterWrite(int n) throws IOException {
        super.afterWrite(n);

        progressTracker.setProgressValue(1.0 * this.getByteCount() / this.totalFileSizeInBytes);
    }
}
