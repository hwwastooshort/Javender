import View.CalendarController;
import Model.Entities.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarControllerTests {

    @Test
    void testValidateDate(){
        CalendarController cc = new CalendarController();
        String validDate = "2030-10-01";
        String invalidDate = "01-10-2030";
        assertFalse(cc.validateDate(validDate));
        assertTrue(cc.validateDate(invalidDate));
    }

    @Test
    void testValidateTime(){
        CalendarController cc = new CalendarController();
        String validTime = "12:00";
        String invalidTime = "12-00";
        assertFalse(cc.validateTime(validTime));
        assertTrue(cc.validateTime(invalidTime));
    }

    @Test
    void testValidateDateTimeOrder(){
        CalendarController cc = new CalendarController();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = LocalDateTime.now().minusDays(1);
        assertTrue(cc.validateDateTimeOrder(now, tomorrow));
        assertFalse(cc.validateDateTimeOrder(tomorrow, now));
    }

    @Test
    void testValidateDateEdgeCases() {
        CalendarController cc = new CalendarController();

        String emptyDate = "";
        String nullDate = null;

        assertTrue(cc.validateDate(emptyDate));
        assertThrows(NullPointerException.class, () -> cc.validateDate(nullDate));
    }
}

