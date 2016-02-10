package updater;

import javafx.application.Platform;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.UI;
import util.DialogMessage;
import util.events.Event;
import util.events.UpdateManagerProgressWindowEvent;

import java.io.*;
import java.net.URI;
import java.net.URLConnection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A class in charge of downloading file.
 * It has a progress tracker which can be used to show progress window.
 */
public class FileDownloader {
    private static final Logger logger = LogManager.getLogger(FileDownloader.class.getName());

    public static final int CONNECTION_TIMEOUT = 15000;
    public static final int READ_CONNECTION_TIMEOUT = 30000;

    private long totalFileSizeInBytes;

    private final ProgressTracker progressTracker;

    public FileDownloader() {
        progressTracker = new ProgressTracker();
    }

    /**
     * Set if current download can have its progress shown
     *
     * @param enableProgressWindow
     */
    public void setEnableProgressWindow(boolean enableProgressWindow) {
        UI.events.triggerEvent(new UpdateManagerProgressWindowEvent(enableProgressWindow));
    }

    /**
     * Set current download's progress window title and message
     *
     * @param windowTitle
     * @param windowMessage
     */
    public void setProgressTrackerWindowDescription(String windowTitle, String windowMessage) {
        this.progressTracker.setProgressWindowDetails(windowTitle, windowMessage);
    }

    /**
     * Download file from a URL to a local destination file
     *
     * @param source URI of file source, expect to be a valid URL
     * @param dest local destination file
     */
    public void downloadFileFromUrlToFile(URI source, File dest) {
        progressTracker.setProgressValue(-1);

        URLConnection sourceConnection = null;

        try {
            sourceConnection = setupDownloadConnection(source);
        } catch (IOException e) {
            logger.error("URI of source file is not a well-formed URL");
            return;
        }

        assert sourceConnection != null;

        totalFileSizeInBytes = sourceConnection.getContentLengthLong();

        try (
                InputStream inputStream = setupStreamFromSource(sourceConnection);
                OutputStream outputStream = setupStreamToDest(dest);
        ) {
            if (inputStream == null || outputStream == null) {
                logger.info("Failed to create streams for download");
                return;
            }

            download(inputStream, outputStream);
        } catch (IOException e) {
            logger.error("Failed to create streams for download", e);
        }

    }

    private URLConnection setupDownloadConnection(URI source) throws IOException {
        URLConnection connection = source.toURL().openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_CONNECTION_TIMEOUT);

        return connection;
    }

    private InputStream setupStreamFromSource(URLConnection source) {
        InputStream inputStream = null;

        try {
            inputStream = source.getInputStream();
        } catch (IOException e) {
            logger.warn("Failed to create buffered input stream");
        }

        return inputStream;
    }

    private OutputStream setupStreamToDest(File dest) {
        OutputStream outputStream = null;

        try {
            if (dest.createNewFile()) {
                outputStream = new FileOutputStream(dest);
            }
        } catch (IOException e) {
            logger.error("Failed to create local file", e);
        }

        return outputStream;
    }

    private void download(InputStream inputStream, OutputStream outputStream) throws IOException {
        DownloadCountingOutputStream countingOutputStream =
                new DownloadCountingOutputStream(outputStream, progressTracker, totalFileSizeInBytes);

        IOUtils.copy(inputStream, countingOutputStream);
    }

    /**
     * Show progress window of download.
     * Expect setProgressTrackerWindowDescription to have been called before download.
     */
    public void showDownloadProgress() {
        this.progressTracker.showProgressWindow();
    }

    /**
     * Hide progress window of download.
     */
    public void hideDownloadProgress() {
        this.progressTracker.hideProgressWindow();
    }
}
