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
import static org.assertj.core.api.Assertions.*;

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
        Appointment expectedAppointment = new Appointment(
                1,
                LocalDateTime.parse("2025-01-01T09:00:00"),
                LocalDateTime.parse("2025-01-01T10:00:00"),
                "Doctor Appointment",
                "Annual checkup",
                List.of(new Tag(1, "Personal", "red"))
        );

        Appointment actualAppointment = dm.getAppointmentById(appointmentId).orElseThrow();
        assertThat(expectedAppointment).isEqualTo(actualAppointment);
    }

    @Test
    void testGetTagsByIdFromDatabase() throws DataManagerException {
        int appointmentId = 1;
        List<Tag> expectedTags = List.of(new Tag(1, "Personal", "red"));
        List<Tag> actualTags = dm.getTagsByAppointmentId(appointmentId);
        assertEquals(expectedTags, actualTags, "Fetched tags should match the expected tags");
    }

    @Test
    void testGetAppointmentsByStartDate() throws DataManagerException {
        LocalDate testDate = LocalDate.of(2025, 1, 1);
        List<Appointment> fetchedAppointments = dm.getAppointmentsByDate(testDate, JooqDataManager.DateFilter.STARTDATE);

        List<Appointment> expectedAppointments = List.of(new Appointment(
                1,
                LocalDateTime.parse("2025-01-01T09:00:00"),
                LocalDateTime.parse("2025-01-01T10:00:00"),
                "Doctor Appointment",
                "Annual checkup",
                List.of(new Tag(1, "Personal", "red"))
        ),
                new Appointment(
                        2,
                        LocalDateTime.parse("2025-01-01T11:00:00"),
                        LocalDateTime.parse("2025-01-01T12:00:00"),
                        "Team Meeting",
                        "Monthly progress update",
                        List.of(new Tag(2, "Work", "blue"))
                ));

        assertThat(fetchedAppointments).containsExactlyInAnyOrderElementsOf(expectedAppointments);
    }

    @Test
    void testGetAppointmentsByRangeWithTwoMatchingDates() throws DataManagerException {
        LocalDateTime rangeStart = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2025, 1, 2, 16, 0);

        List<Appointment> expectedAppointments = List.of(new Appointment(
                        1,
                        LocalDateTime.parse("2025-01-01T09:00:00"),
                        LocalDateTime.parse("2025-01-01T10:00:00"),
                        "Doctor Appointment",
                        "Annual checkup",
                        List.of(new Tag(1, "Personal", "red"))
                ),
                new Appointment(
                        2,
                        LocalDateTime.parse("2025-01-01T11:00:00"),
                        LocalDateTime.parse("2025-01-01T12:00:00"),
                        "Team Meeting",
                        "Monthly progress update",
                        List.of(new Tag(2, "Work", "blue"))
                ),
                new Appointment(
                        3,
                        LocalDateTime.parse("2025-01-02T14:00:00"),
                        LocalDateTime.parse("2025-01-02T15:00:00"),
                        "Client Presentation",
                        "Present new project proposal",
                        List.of(new Tag(2, "Work", "blue"))
                ));

        List<Appointment> actualAppointments = dm.getAppointmentsByRange(rangeStart, rangeEnd);


        assertEquals(3, actualAppointments.size(), "There should be 3 appointments in this range");
        assertThat(actualAppointments).hasSize(3).isEqualTo(expectedAppointments);
    }

    @Test
    void testGetAppointmentsByRangeWithNoMatchingDates() throws DataManagerException {
        LocalDateTime rangeStart = LocalDateTime.of(2026, 1, 3, 0, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2090, 1, 4, 16, 0);

        List<Appointment> actualAppointments = dm.getAppointmentsByRange(rangeStart, rangeEnd);

        assertThat(actualAppointments).isEmpty();
    }

    @Test
    void testGetTagById() throws DataManagerException {
        int tagId = 1;
        Tag expectedTag = new Tag(1, "Personal", "red");
        assertThat(dm.getTagById(tagId).get()).isEqualTo(expectedTag);
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
        var originalAppointment = dm.getAppointmentById(1).orElseThrow();
        var updatedAppointment = new Appointment(
                originalAppointment.getAppointmentId(),
                originalAppointment.getStartDate(),
                originalAppointment.getEndDate(),
                "Updated Title",
                "Updated Description",
                originalAppointment.getTags()
        );

        dm.updateAppointment(updatedAppointment);
        assertThat(updatedAppointment).isEqualTo(dm.getAppointmentById(1).orElseThrow());
    }

    @Test
    void testUpdateAppointmentThatDoesNotExist() throws DataManagerException {
        var nonExistentAppointment = new Appointment(
                999,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Non-existent",
                "Non-existent",
                List.of()
        );
        assertThrows(DataManagerException.class, () -> dm.updateAppointment(nonExistentAppointment));
    }

    @Test
    void testGetAppointmentByTitle() throws DataManagerException {
        String title = "Doctor Appointment";
        var expectedAppointments = List.of(new Appointment(
                1,
                LocalDateTime.parse("2025-01-01T09:00:00"),
                LocalDateTime.parse("2025-01-01T10:00:00"),
                "Doctor Appointment",
                "Annual checkup",
                List.of(new Tag(1, "Personal", "red"))
        ));

        var actualAppointments = dm.getAppointmentsByTitle(title);
        assertThat(actualAppointments).containsExactlyInAnyOrderElementsOf(expectedAppointments);
    }

    @Test
    void testGetAppointmentsByTitleFail() throws DataManagerException {
        String title = "Non-existent";
        var actualAppointments = dm.getAppointmentsByTitle(title);
        assertThat(actualAppointments).isEmpty();
    }

    @Test
    void testGetTagByTitle() throws DataManagerException {
        String givenTitle = "Personal";
        assertThat(dm.getTagByTitle(givenTitle).get()).isEqualTo(new Tag(1, "Personal", "red"));
    }
}