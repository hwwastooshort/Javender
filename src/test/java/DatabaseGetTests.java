import static org.junit.jupiter.api.Assertions.*;

import Model.Database.DataManager;
import Model.Database.JooqDataManager;
import Model.Database.DataManagerException;
import Model.Entities.Appointment;
import Model.Entities.Tag;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseGetTests {

    private DataManager dm;
    private final List<Integer> testTagIds = new ArrayList<>();
    private final List<Integer> testAppointmentIds = new ArrayList<>();

    @BeforeEach
    void setupDatabase() throws DataManagerException {
        dm = new JooqDataManager("jdbc:sqlite:src/test/resources/javenderDatabase.db");

        assertNotNull(dm, "Database connection must be established before running tests.");

        Tag personalTag = new Tag("Personal", "red");
        Tag workTag = new Tag("Work", "blue");
        int personalTagId = dm.addTag(personalTag);
        int workTagId = dm.addTag(workTag);
        testTagIds.add(personalTagId);
        testTagIds.add(workTagId);

        List<Tag> personalTags = List.of(new Tag(personalTagId, "Personal", "red"));
        List<Tag> workTags = List.of(new Tag(workTagId, "Work", "blue"));

        Appointment annualCheckup = new Appointment(
                LocalDateTime.parse("2025-01-01T09:00:00"),
                LocalDateTime.parse("2025-01-01T10:00:00"),
                "Doctor Appointment",
                "Annual checkup",
                personalTags
        );
        testAppointmentIds.add(dm.addAppointment(annualCheckup));

        Appointment monthlyProgressUpdate = new Appointment(
                LocalDateTime.parse("2025-01-01T11:00:00"),
                LocalDateTime.parse("2025-01-01T12:00:00"),
                "Team Meeting",
                "Monthly progress update",
                workTags
        );
        testAppointmentIds.add(dm.addAppointment(monthlyProgressUpdate));

        Appointment newProjectProposal = new Appointment(
                LocalDateTime.parse("2025-01-02T14:00:00"),
                LocalDateTime.parse("2025-01-02T15:00:00"),
                "Client Presentation",
                "Present new project proposal",
                workTags
        );
        testAppointmentIds.add(dm.addAppointment(newProjectProposal));
    }

    @AfterEach
    void cleanupDatabase() throws DataManagerException {
        for (int tagId : testTagIds) {
            dm.removeTagByTagId(tagId);
        }
        for (int appointmentId : testAppointmentIds) {
            dm.removeAppointmentById(appointmentId);
        }
        testTagIds.clear();
        testAppointmentIds.clear();
    }


    @Test
    void testGetAppointmentFromDatabaseById() throws DataManagerException {
        int appointmentId = testAppointmentIds.get(0);

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

        List<Tag> expectedTags = List.of(new Tag(testTagIds.get(0), "Personal", "red"));
        assertEquals(expectedTags, result.getTags(), "Tags should match the expected tags");
    }

    @Test
    void testGetTagsByIdFromDatabase() throws DataManagerException {
        int appointmentId = testAppointmentIds.get(0);

        List<Tag> fetchedTags = dm.getTagsByAppointmentId(appointmentId);

        assertFalse(fetchedTags.isEmpty(), "Tags should be present for the test appointment");

        List<Tag> expectedTags = List.of(new Tag(testTagIds.get(0), "Personal", "red"));
        assertEquals(expectedTags, fetchedTags, "Fetched tags should match the expected tags");
    }

    @Test
    void testGetAppointmentsByStartDate() throws DataManagerException {
        LocalDate testDate = LocalDate.of(2025, 1, 1);

        List<Appointment> fetchedAppointments = dm.getAppointmentsByDate(testDate, JooqDataManager.DateFilter.STARTDATE);

        assertFalse(fetchedAppointments.isEmpty(), "Appointments for the given date should not be empty");

        Appointment firstAppointment = fetchedAppointments.get(0);
        assertEquals(testAppointmentIds.get(0), firstAppointment.getAppointmentId(), "The appointment ID should match the expected value");

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
        int tagId = testTagIds.get(0);

        Optional<Tag> optionalTag = dm.getTagById(tagId);

        assertTrue(optionalTag.isPresent(), "Tag should exist in the database");

        Tag actualTag = optionalTag.get();
        assertEquals("Personal", actualTag.getName(), "Tag name should match");
        assertEquals("red", actualTag.getColor(), "Tag color should match");
    }

    @Test
    void testGetTagByIdFail() throws DataManagerException {
        int nonExistentTagId = testTagIds.stream().max(Integer::compare).orElse(1000) + 1;

        Optional<Tag> actualTag = dm.getTagById(nonExistentTagId);

        assertTrue(actualTag.isEmpty(), "There should be no tag with the given ID: " + nonExistentTagId);
    }

    @Test
    void testGetAllTags() throws DataManagerException {
        List<Tag> allTags = dm.getAllTags();

        List<Tag> expectedTags = List.of(
                new Tag(testTagIds.get(0), "Personal", "red"),
                new Tag(testTagIds.get(1), "Work", "blue")
        );
        assertTrue(allTags.containsAll(expectedTags), "All test tags should be present in the fetched tags");
    }

    @Test
    void testUpdateAppointment() throws DataManagerException {
        Appointment fetchedAppointment = dm.getAppointmentById(testAppointmentIds.get(0)).orElseThrow();
        assertEquals(fetchedAppointment.getTitle(), "Doctor Appointment","The fetched appointment should have the correct title");

        fetchedAppointment.setTitle("Updated Title");
        fetchedAppointment.setDescription("Updated Description");
        dm.updateAppointment(fetchedAppointment);

        Appointment newlyfetchedAppointment = dm.getAppointmentById(testAppointmentIds.get(0)).orElseThrow();
        assertEquals(fetchedAppointment, newlyfetchedAppointment, "The updated appointment should match the changed appointment");

    }
}