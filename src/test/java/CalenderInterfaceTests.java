import View.CalendarInterface;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import java.time.LocalDate;

public class CalenderInterfaceTests {

    @Test
    void testPrintMonth(){
        CalendarInterface cI = new CalendarInterface();
        String formattedMonthMarch = cI.getMonth(LocalDate.parse("2025-03-01"));

        assertTrue(formattedMonthMarch.contains("     MARCH\t2025"));
        assertTrue(formattedMonthMarch.contains("MO TU WE TH FR SA SU"));
        assertTrue(formattedMonthMarch.contains("                1  2"));
        assertTrue(formattedMonthMarch.contains(" 3  4  5  6  7  8  9"));
        assertTrue(formattedMonthMarch.contains("10 11 12 13 14 15 16"));
        assertTrue(formattedMonthMarch.contains("17 18 19 20 21 22 23"));
        assertTrue(formattedMonthMarch.contains("24 25 26 27 28 29 30"));
        assertTrue(formattedMonthMarch.contains("31 "));

        assertFalse(formattedMonthMarch.contains("25 26 27 28 29 30 31"));
    }
}
