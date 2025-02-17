import Model.Database.DataManager;
import Model.Database.DataManagerException;
import Model.Database.JooqDataManager;
import Model.Entities.Appointment;
import View.CalendarInterface;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CalendarInterfaceTests {

    private DataManager dm;

    @BeforeAll
    void setupDatabase() throws DataManagerException {
        dm = new JooqDataManager("src/test/resources/javenderDatabase.db");
    }

    @AfterAll
    void cleanupDatabase() {
        if (dm instanceof JooqDataManager) {
            ((JooqDataManager) dm).close();
        }
    }

    @Test
    void testPrintMonth() {
        CalendarInterface cI = new CalendarInterface();
        LocalDateTime startRange = LocalDateTime.parse("2025-03-01T00:00");
        LocalDateTime endRange = LocalDateTime.parse("2025-03-31T23:59");

        try {
            List<Appointment> appointmentList = dm.getAppointmentsByRange(startRange, endRange);
            String formattedMonthMarch = cI.getCalendar(LocalDate.parse("2025-03-01"), new ArrayList<>(), 1);

            assertTrue(formattedMonthMarch.contains("     MARCH 2025      "));
            assertTrue(formattedMonthMarch.contains("MO TU WE TH FR SA SU "));
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
