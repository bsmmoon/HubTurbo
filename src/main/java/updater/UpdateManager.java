package updater;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.DialogMessage;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UpdateManager is the class that will handle updating of HubTurbo application
 */
public class UpdateManager {
    private static final Logger logger = LogManager.getLogger(UpdateManager.class.getName());

    private static final String UPDATE_DIRECTORY = "updates";
    private static final String UPDATE_SERVER_DATA_NAME =
            "https://raw.githubusercontent.com/HubTurbo/AutoUpdater/master/HubTurbo.xml";
    private static final String UPDATE_LOCAL_DATA_NAME = "HubTurbo.json";
    private static final String UPDATE_APP_NAME = "HubTurbo.jar";

    private final UpdateProgressWindow updateProgressWindow;

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    public UpdateManager(UpdateProgressWindow updateProgressWindow) {
        this.updateProgressWindow = updateProgressWindow;
    }

    /**
     * Driver method to trigger UpdateManager to run. Update will be run on another thread.
     *
     * - Run is not automatic upon instancing the class in case there would like to be conditions on when to run update,
     *   e.g. only if user is logged in
     */
    public void run() {
        pool.execute(() -> runUpdate());
    }

    /**
     * Method that will run update sequence.
     * 1. Initialize system for update, e.g. make directory
     * 2. Check if there is any new update
     *   2.a. If there is no new update but the previously downloaded update is not applied yet,
     *        prompt user for updating and restarting application
     * 3. Download update according to user preference
     * 4. Once update is done, prompt user for updating and restarting application
     */
    private void runUpdate() {
        if (!initUpdate()) {
            logger.error("Failed to initialize update");
            return;
        }

        if (!downloadUpdateData()) {
            logger.error("Failed to download update data");
            return;
        }

        // TODO check if there is a new update since last update, and download update if necessary

        if (!downloadUpdateForApplication()) {
            logger.error("Failed to download updated application");
            return;
        }

        updateProgressWindow.hideWindow();
        Platform.runLater(() ->
                DialogMessage.showInformationDialog("Download completed", "Downloaded new HubTurbo."));

        // TODO once update is done, run new process that will:
        // - kill current HubTurbo
        // - replace JAR
        // - start the new JAR

    }

    /**
     * Initializing system for updates
     * - Create directory(ies) for updates
     */
    private boolean initUpdate() {
        File updateDir = new File(UPDATE_DIRECTORY);

        if (!updateDir.exists() && !updateDir.mkdirs()) {
            logger.error("Failed to create update directories");
            return false;
        }

        // TODO check if internet connection is present

        return true;
    }

    private boolean downloadUpdateData() {
        try {
            DownloadFile downloadFile = new DownloadFile(
                    new URI(UPDATE_SERVER_DATA_NAME),
                    new File(UPDATE_DIRECTORY + File.separator + UPDATE_LOCAL_DATA_NAME),
                    Optional.empty());
            return downloadFile.startDownload();
        } catch (URISyntaxException e) {
            logger.error("Failed to download update data", e);
            return false;
        }
    }

    private boolean downloadUpdateForApplication() {
        boolean result = false;
        DownloadProgressTracker progressTracker =
                updateProgressWindow.getNewDownloadProgressTracker("HubTurbo Application");

        try {
            // TODO replace download source to use updater data
            DownloadFile downloadFile = new DownloadFile(
                    new URI("https://github.com/HubTurbo/HubTurbo/releases/download/V3.18.0/resource-v3.18.0.jar"),
                    new File(UPDATE_DIRECTORY + File.separator + UPDATE_APP_NAME),
                    Optional.of(progressTracker));
            result = downloadFile.startDownload();
        } catch (URISyntaxException e) {
            logger.error("Failed to download new application", e);
            result = false;
        }

        updateProgressWindow.removeDownloadProgressTracker(progressTracker);

        return result;
    }
}
