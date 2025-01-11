import static org.junit.jupiter.api.Assertions.*;

import Domain.Database.DataManager;
import Domain.Entities.Appointment;
import Domain.Entities.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        Optional<Appointment> optionalResult = dm.getAppointmentById(appointmentId);

        assertTrue(optionalResult.isPresent(), "Result should not be empty, there is an element with that appointmentId in the database");

        Appointment result = optionalResult.get(); // Zugriff auf den Wert, da wir sicher sind, dass er vorhanden ist

        assertEquals(appointmentId, result.getAppointmentId(), "AppointmentId's should be equal");

        String expectedStartDateTimeString = "2025-01-01T10:00:00";
        String expectedEndDateTimeString = "2025-01-01T11:00:00";
        LocalDateTime expectedStartDateTime = LocalDateTime.parse(expectedStartDateTimeString);
        LocalDateTime expectedEndDateTime = LocalDateTime.parse(expectedEndDateTimeString);

        assertEquals(expectedStartDateTime, result.getStartDate(), "Start Dates should be equal");
        assertEquals(expectedEndDateTime, result.getEndDate(), "End Dates should be equal");
        assertEquals("Doctor Appointment", result.getTitle());
        assertEquals("Annual checkup", result.getDescription());

        List<Tag> expectedList = new ArrayList<>();
        Tag firstTagInExpectedList = new Tag(4, "Work", "blue");
        expectedList.add(firstTagInExpectedList);
        assertEquals(expectedList, result.getTags(), "Appointment 1 should only have 1 Tag");
    }
    @Test
    void testGetTagsByIdFromDatabase() {
        DataManager dm = new DataManager();
        int appointmentId = 3;

        Optional<List<Tag>> optionalTags = dm.getTagByAppointmentId(appointmentId);
        assertTrue(optionalTags.isPresent(), "Tags should be present for appointmentId 3");
        List<Tag> actualTags = optionalTags.get();

        List<Tag> expectedTags = new ArrayList<>();
        expectedTags.add(new Tag(1, "testingTag", "yellow"));
        expectedTags.add(new Tag(4, "Work", "blue"));

        assertEquals(expectedTags, actualTags, "There should be 2 Tags matching to appointmentId 3");
    }
}
