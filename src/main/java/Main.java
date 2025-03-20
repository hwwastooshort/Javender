import Controller.CalendarController;
import Model.Database.DataManager;
import Model.Database.DataManagerException;
import Model.Database.JooqDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

public class Main {

    private static final String SOURCE_PATH = "javenderDataBase.db";
    private static final String DESTINATION_PATH = "data/db.sqlite";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws DataManagerException {
        /*
         putting the database directly into the .jar file is not possible.
         Therefore, the database is copied to a fresh folder "data", which stores
         the data of the user.
         */
        DataManager dataManager;
        File file = new File(DESTINATION_PATH);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.mkdirs() && !parentDir.exists()) {
            logger.error("Could not create parent directory for database file.");
        }

        if (!file.exists()) {
            copyDatabaseFile();
        }

        dataManager = new JooqDataManager(DESTINATION_PATH);
        CalendarController controller = new CalendarController(dataManager);
        controller.mainMenu();
    }

    private static void copyDatabaseFile() {
        ClassLoader classLoader = Main.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(SOURCE_PATH)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Datei nicht gefunden!");
            }
            Files.copy(inputStream, Paths.get(DESTINATION_PATH));
        } catch (Exception e) {
            logger.error("Could not copy database file.", e);
        }
    }
}