import Controller.CalendarController;
import Model.Database.DataManager;
import Model.Database.DataManagerException;
import Model.Database.JooqDataManager;

import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("This is from the main method!");
        DataManager dataManager;
        /*
         putting the sqlite3 database into the .jar file directly is not possible
         so we have to extract it to a temporary file in order to use the database
         and then delete it when the program is closed.
         */

        try {
            String databaseFileName = "javenderDataBase.db";
            File tempDbFile = new File(System.getProperty("java.io.tmpdir"), databaseFileName);

            if (!tempDbFile.exists()) {
                try (InputStream in = Main.class.getClassLoader().getResourceAsStream(databaseFileName);
                     OutputStream out = new FileOutputStream(tempDbFile)) {

                    if (in == null) {
                        throw new FileNotFoundException("Database could not be found in the JAR file");
                    }

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error while trying to extract the database", e);
                }
            }
            dataManager = new JooqDataManager(tempDbFile.getAbsolutePath());

        } catch (DataManagerException e) {
            throw new RuntimeException("Error while trying to initialize DataManager", e);
        }

        CalendarController controller = new CalendarController(dataManager);
        System.out.println("Welcome to Javender!");
        controller.mainMenu();
    }
}