import View.CalendarInterface;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class CalenderInterfaceTests {

    @Test
    void testPrintMonth(){
        CalendarInterface cI = new CalendarInterface();
        System.out.println(cI.printMonth(LocalDate.now()));
    }
}
