package updater;

import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.TestController;
import ui.UI;
import util.DialogMessage;
import util.events.UpdateManagerProgressWindowEventHandler;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

    private final FileDownloader fileDownloader;

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    public UpdateManager() {
        fileDownloader = new FileDownloader();
    }

    /**
     * Driver method to trigger UpdateManager to run. Update will be run on another thread.
     *
     * - Run is not automatic upon instancing the class in case there would like to be conditions on when to run update,
     *   e.g. only if user is logged in
     */
    public void run() {
        if (!TestController.isTestMode()) {
            pool.execute(() -> runUpdate());
        }
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

        downloadUpdateData();

        // TODO check if there is a new update since last update, and download update if necessary

        downloadUpdateForApplication();

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
        boolean result = false;

        File updateDir = new File(UPDATE_DIRECTORY);

        if (!updateDir.exists()) {
            result = updateDir.mkdirs();
            if (!result) {
                logger.error("Failed to create update directories");
            }
        }

        return result;
    }

    private void downloadUpdateData() {
        try {
            fileDownloader.setProgressTrackerWindowDescription("Checking for update", "Checking for update...");
            fileDownloader.setEnableProgressWindow(false);
            fileDownloader.downloadFileFromUrlToFile(
                    new URI(UPDATE_SERVER_DATA_NAME),
                    new File(UPDATE_DIRECTORY + File.separator + UPDATE_LOCAL_DATA_NAME));
        } catch (URISyntaxException e) {
            logger.error("Failed to download update data", e);
        }
    }

    private void downloadUpdateForApplication() {
        try {
            fileDownloader.setProgressTrackerWindowDescription("Downloading update",
                    "Downloading update for HubTurbo...");
            fileDownloader.setEnableProgressWindow(true);
            fileDownloader.showDownloadProgress();
            // TODO replace download source to use updater data
            fileDownloader.downloadFileFromUrlToFile(
                    new URI("https://github.com/HubTurbo/HubTurbo/releases/download/V3.18.0/resource-v3.18.0.jar"),
                    new File(UPDATE_DIRECTORY + File.separator + UPDATE_APP_NAME));
            fileDownloader.hideDownloadProgress();
            DialogMessage.showInformationDialog("Download completed", "Downloaded new HubTurbo.");
        } catch (URISyntaxException e) {
            logger.error("Failed to download new application", e);
        }
    }

    /**
     * Create menu for UpdateManager
     *
     * @return Menu for MenuControl to display
     */
    public Menu getMenu() {
        Menu update = new Menu("Update");

        MenuItem checkProgress = new MenuItem("Check progress...");
        checkProgress.setDisable(true);

        checkProgress.setOnAction(e -> {
            Platform.runLater(() -> fileDownloader.showDownloadProgress());
        });

        UI.events.registerEvent((UpdateManagerProgressWindowEventHandler) e -> {
            checkProgress.setDisable(!e.isProgressWindowEnabled);
        });

        update.getItems().addAll(checkProgress);

        return update;
    }
}
