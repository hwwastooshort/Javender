import Model.Database.DataManager;
import Model.Database.JooqDataManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CalendarInterfaceTests {

    private DataManager dm;

    @BeforeAll
    void setupDatabase() {
        dm = new JooqDataManager("src/test/resources/javenderDatabase.db");
    }

    @AfterAll
    void cleanupDatabase() {
        if (dm instanceof JooqDataManager) {
            ((JooqDataManager) dm).close();
        }
    }

    private String getMonth(LocalDate date) {
        StringBuilder monthString = new StringBuilder();

        int offset = getDayOffset(date);
        monthString.append("   ".repeat(Math.max(0, offset)));

        int dayPosition = offset;
        for (int day = 1; day <= date.lengthOfMonth(); day++) {
            monthString.append(String.format("%2d ", day));
            dayPosition = (dayPosition + 1) % 7;
            if (dayPosition == 0) {
                monthString.append("\n");
            }
        }
        monthString.append("   ".repeat(7 - dayPosition));

        return monthString.toString();
    }

    private int getDayOffset(LocalDate date) {
        int offset;
        DayOfWeek firstDayOfMonth = date.minusDays(date.getDayOfMonth() - 1).getDayOfWeek();
        offset = firstDayOfMonth.getValue() - 1;
        return offset;
    }

    @Test
    void testPrintMonth() {

        try {
            String formattedMonthMarch = getMonth(LocalDate.parse("2025-03-01"));

            assertTrue(formattedMonthMarch.contains("                1  2 "));
            assertTrue(formattedMonthMarch.contains(" 3  4  5  6  7  8  9 "));
            assertTrue(formattedMonthMarch.contains("10 11 12 13 14 15 16 "));
            assertTrue(formattedMonthMarch.contains("17 18 19 20 21 22 23 "));
            assertTrue(formattedMonthMarch.contains("24 25 26 27 28 29 30 "));
            assertTrue(formattedMonthMarch.contains("31                   "));

            assertFalse(formattedMonthMarch.contains("25 26 27 28 29 30 31"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
