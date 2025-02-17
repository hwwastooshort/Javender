import Controller.CalendarController;
import Model.Database.DataManager;
import Model.Database.DataManagerException;
import Model.Database.JooqDataManager;


public class Main {
    public static void main(String[] args) {
        System.out.println("This is from the main method!");
        DataManager dataManager = null;

        try {
            dataManager = new JooqDataManager("src/test/resources/javenderDatabase.db");
        } catch (DataManagerException e) {
            throw new RuntimeException(e);
        }
        CalendarController controller = new CalendarController(dataManager);
        System.out.println("Welcome to Javender!");
        controller.mainMenu();
    }

}
