import Controller.CalendarController;


public class Main {
    public static void main(String[] args) {
        System.out.println("This is from the main method!");
        CalendarController controller = new CalendarController();
        System.out.println("Welcome to Javender!");
        controller.mainMenu();
    }

}
