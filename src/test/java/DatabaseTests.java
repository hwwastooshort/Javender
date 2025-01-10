import static org.junit.jupiter.api.Assertions.*;

import Domain.Database.DataManager;
import Domain.Entities.Appointment;
import Domain.Entities.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DatabaseTests {
    @Test
    public void testDatabaseConnection() {
        String url = "jdbc:sqlite:src/main/resources/javenderDataBase.db";
        try (Connection connection = DriverManager.getConnection(url)) {
            assertNotNull(connection, "Connection to Database should not be null");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AssertionError("Connection couldn't be established", e);
        }
    }

    @Test
    void testGetAppointmentFromDatabaseById() {
        DataManager dm = new DataManager();
        int appointmentId = 1;
        Appointment result = dm.getAppointmentById(1);
        assertNotNull(result, "Result should not be null, there is an Element with that appointmentId in the database");
        assertEquals(appointmentId, result.getAppointmentId(), "AppointmentId's should be equal");
        String expectedStartDateTimeString = "2025-01-01T10:00:00";
        String expectedEndDateTimeString = "2025-01-01T11:00:00";
        LocalDateTime expectedStartDateTime = LocalDateTime.parse(expectedStartDateTimeString);
        LocalDateTime expectedEndDateTime = LocalDateTime.parse(expectedEndDateTimeString);
        assertEquals(expectedStartDateTime, result.getStartDate(), "Start Dates should be equal");
        assertEquals(expectedEndDateTime, result.getEndDate(), "End Dates should be equals");
        assertEquals("Doctor Appointment", result.getTitle());
        assertEquals("Annual checkup", result.getDescription());
    }
    @Test
    void testGetTagsByIdFromDatabase(){
        DataManager dm = new DataManager();
        int appointmentId = 3;
        List<Tag> tags = dm.getTagByAppointmentId(appointmentId);
        System.out.println(tags);
    }
}
