import static org.junit.jupiter.api.Assertions.*;

import Model.Database.DataManager;
import Model.Database.JooqDataManager;
import Model.Database.DataManagerException;
import Model.Entities.Appointment;
import Model.Entities.Tag;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DatabaseGetTests {

    private DataManager dm;

    @BeforeEach
    void setupDatabase() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:src/test/resources/javenderDataBase.db");
             Statement statement = connection.createStatement()) {

            String setupSql = Files.readString(Paths.get("src/test/resources/AddTestAppointments.sql"));
            statement.executeUpdate(setupSql);
        }

        dm = new JooqDataManager("jdbc:sqlite:src/test/resources/javenderDataBase.db");
        assertNotNull(dm, "Database connection must be established before running tests.");
    }

    @AfterEach
    void cleanupDatabase() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:src/test/resources/javenderDataBase.db");
             Statement statement = connection.createStatement()) {

            String cleanupSql = Files.readString(Paths.get("src/test/resources/DeleteTestAppointments.sql"));
            statement.executeUpdate(cleanupSql);
        }
    }

    @Test
    void testGetAppointmentFromDatabaseById() throws DataManagerException {
        int appointmentId = 1;

        Optional<Appointment> optionalResult = dm.getAppointmentById(appointmentId);

        assertTrue(optionalResult.isPresent(), "Result should not be empty; appointment must exist in the database");

        Appointment result = optionalResult.get();
        assertEquals(appointmentId, result.getAppointmentId(), "AppointmentId should match the expected value");

        LocalDateTime expectedStartDateTime = LocalDateTime.parse("2025-01-01T09:00:00");
        LocalDateTime expectedEndDateTime = LocalDateTime.parse("2025-01-01T10:00:00");
        assertEquals(expectedStartDateTime, result.getStartDate(), "Start Date should match");
        assertEquals(expectedEndDateTime, result.getEndDate(), "End Date should match");

        assertEquals("Doctor Appointment", result.getTitle(), "The title should match the expected value");
        assertEquals("Annual checkup", result.getDescription(), "The description should match the expected value");

        List<Tag> expectedTags = List.of(new Tag(1, "Personal", "red"));
        assertEquals(expectedTags, result.getTags(), "Tags should match the expected tags");
    }

    @Test
    void testGetTagsByIdFromDatabase() throws DataManagerException {
        int appointmentId = 1;

        List<Tag> fetchedTags = dm.getTagsByAppointmentId(appointmentId);

        assertFalse(fetchedTags.isEmpty(), "Tags should be present for the test appointment");

        List<Tag> expectedTags = List.of(new Tag(1, "Personal", "red"));
        assertEquals(expectedTags, fetchedTags, "Fetched tags should match the expected tags");
    }

    @Test
    void testGetAppointmentsByStartDate() throws DataManagerException {
        LocalDate testDate = LocalDate.of(2025, 1, 1);

        List<Appointment> fetchedAppointments = dm.getAppointmentsByDate(testDate, JooqDataManager.DateFilter.STARTDATE);

        assertFalse(fetchedAppointments.isEmpty(), "Appointments for the given date should not be empty");

        List<String> expectedTitles = List.of("Doctor Appointment", "Team Meeting");
        assertTrue(fetchedAppointments.stream().map(Appointment::getTitle).toList().containsAll(expectedTitles),
                "All expected appointments should be present.");
    }

    @Test
    void testGetAppointmentsByRangeWithTwoMatchingDates() throws DataManagerException {
        LocalDateTime rangeStart = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2025, 1, 2, 16, 0);

        List<Appointment> fetchedAppointments = dm.getAppointmentsByRange(rangeStart, rangeEnd);

        assertFalse(fetchedAppointments.isEmpty(), "Appointments for the given range should not be empty");
        assertEquals(3, fetchedAppointments.size(), "There should be 3 appointments in this range");
    }

    @Test
    void testGetTagById() throws DataManagerException {
        int tagId = 1;

        Optional<Tag> optionalTag = dm.getTagById(tagId);

        assertTrue(optionalTag.isPresent(), "Tag should exist in the database");

        Tag actualTag = optionalTag.get();
        assertEquals("Personal", actualTag.getName(), "Tag name should match");
        assertEquals("red", actualTag.getColor(), "Tag color should match");
    }
    @Test
    void testGetTagByIdFail() throws DataManagerException {
        int nonExistentTagId = 999;

        Optional<Tag> actualTag = dm.getTagById(nonExistentTagId);

        assertTrue(actualTag.isEmpty(), "There should be no tag with the given ID: " + nonExistentTagId);
    }

    @Test
    void testGetAllTags() throws DataManagerException {
        List<Tag> allTags = dm.getAllTags();

        List<Tag> expectedTags = List.of(
                new Tag(1, "Personal", "red"),
                new Tag(2, "Work", "blue")
        );
        assertTrue(allTags.containsAll(expectedTags), "All test tags should be present in the fetched tags");
    }

    @Test
    void testUpdateAppointment() throws DataManagerException {
        int appointmentId = 1;

        Appointment fetchedAppointment = dm.getAppointmentById(appointmentId).orElseThrow();
        assertEquals("Doctor Appointment", fetchedAppointment.getTitle(), "The fetched appointment should have the correct title");

        fetchedAppointment.setTitle("Updated Title");
        fetchedAppointment.setDescription("Updated Description");
        dm.updateAppointment(fetchedAppointment);

        Appointment newlyFetchedAppointment = dm.getAppointmentById(appointmentId).orElseThrow();
        assertEquals(fetchedAppointment, newlyFetchedAppointment, "The updated appointment should match the changed appointment");
    }

    @Test
    void testGetAppointmentsByTitle() throws DataManagerException {
        String title = "Doctor Appointment";

        List<Appointment> fetchedAppointments = dm.getAppointmentsByTitle(title);

        assertFalse(fetchedAppointments.isEmpty(), "Appointments for the given title should not be empty");

        Appointment firstAppointment = fetchedAppointments.get(0);
        assertEquals(1, firstAppointment.getAppointmentId(), "The appointment ID should match the expected value");
        assertEquals(title, firstAppointment.getTitle(), "The appointment title should match the expected value");

        List<String> expectedTitles = List.of("Doctor Appointment");
        assertTrue(fetchedAppointments.stream().map(Appointment::getTitle).toList().containsAll(expectedTitles),
                "All expected appointments should be present.");
    }
}