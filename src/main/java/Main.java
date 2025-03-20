import Controller.CalendarController;
import Model.Database.DataManager;
import Model.Database.DataManagerException;
import Model.Database.JooqDataManager;

import java.io.*;
import java.nio.file.*;

public class Main {

    private static final String SOURCE_PATH = "javenderDataBase.db";
    private static final String DESTINATION_PATH = "data/db.sqlite";

    public static void main(String[] args) throws DataManagerException {

        System.out.println("This is from the main method!");
        DataManager dataManager;
        /*
         putting the sqlite3 database into the .jar file directly is not possible
         so we have to extract it to a temporary file in order to use the database
         and then delete it when the program is closed.
         */

        File file = new File(DESTINATION_PATH);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.mkdirs() && !parentDir.exists()) {
            //TODO: Logger einbinden
        }

        if (!file.exists()){
            copyDatabaseFile();
        }

        dataManager = new JooqDataManager(DESTINATION_PATH);

        CalendarController controller = new CalendarController(dataManager);
        System.out.println("Welcome to Javender!");
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
            //TODO: Logger einbinden
        }
    }
}