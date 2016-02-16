package updater;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URLConnection;
import java.util.Optional;

/**
 * A class in charge of downloading file.
 */
public class DownloadFile {
    private static final Logger logger = LogManager.getLogger(DownloadFile.class.getName());

    public static final int CONNECTION_TIMEOUT = 15000;
    public static final int READ_CONNECTION_TIMEOUT = 30000;

    private final URI source;
    private final File dest;
    private final Optional<DownloadProgressTracker> downloadProgressTracker;
    private long totalFileSizeInBytes;

    /**
     * downloadProgressTracker is optional. Use Optional.empty() if no tracker is needed.
     *
     * @param source URI of file source, expect to be a valid URL
     * @param dest local destination file
     * @param downloadProgressTracker optional download progress tracker
     */
    public DownloadFile(URI source, File dest, Optional<DownloadProgressTracker> downloadProgressTracker) {
        this.source = source;
        this.dest = dest;
        this.downloadProgressTracker = downloadProgressTracker;
    }

    /**
     * Start download from source to destination as specified in constructor
     *
     * @return true if download is successful, else false
     */
    public boolean startDownload() {
        URLConnection sourceConnection = null;

        try {
            sourceConnection = setupDownloadConnection(source);
        } catch (IOException e) {
            logger.error("URI of source file is not a well-formed URL");
            return false;
        }

        assert sourceConnection != null;

        totalFileSizeInBytes = sourceConnection.getContentLengthLong();

        try (
                InputStream inputStream = setupStreamFromSource(sourceConnection);
                OutputStream outputStream = setupStreamToDest(dest);
        ) {
            if (inputStream == null || outputStream == null) {
                logger.error("Failed to create streams for download");
                return false;
            }

            download(inputStream, outputStream);
        } catch (IOException e) {
            logger.error("Failed to create streams for download", e);
            return false;
        }

        return true;
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

        if (dest.exists() && !dest.delete()) {
            logger.error("Failed to delete old file");
            return null;
        }

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
                new DownloadCountingOutputStream(outputStream, totalFileSizeInBytes);

        if (downloadProgressTracker.isPresent()) {
            countingOutputStream.addListener(downloadProgressTracker.get());
        }

        IOUtils.copy(inputStream, countingOutputStream);
    }
}
