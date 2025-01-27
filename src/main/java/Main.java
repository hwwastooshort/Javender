import View.CalendarController;
import View.CalendarInterface;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.sqlite.*;
import static org.jooq.impl.SQLDataType.*;
import static org.jooq.SQLDialect.*;

import java.sql.*;


public class Main {
    public static void main(String[] args) {
        clearScreen();
        System.out.println("This is from the main method!");

        CalendarController controller = new CalendarController();
        System.out.println("Welcome to Javender!");

        controller.mainMenu();

    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
