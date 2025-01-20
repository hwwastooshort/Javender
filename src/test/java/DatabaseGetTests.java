import static org.junit.jupiter.api.Assertions.*;

import Model.Database.DataManager;
import Model.Database.JooqDataManager;
import Model.Database.DataManagerException;
import Model.Entities.Appointment;
import Model.Entities.Tag;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseGetTests {

    private final DataManager dm;

    {
        try {
            dm = new JooqDataManager("jdbc:sqlite:src/test/resources/javenderDatabase.db");
        } catch (DataManagerException e) {
            throw new RuntimeException("Couldn't connect to Database");
        }
    }

    @Test
    void testGetAppointmentFromDatabaseById() {
        int appointmentId = 1;

        try {
            Optional<Appointment> optionalResult = dm.getAppointmentById(appointmentId);

            assertTrue(optionalResult.isPresent(),
                    "Result should not be empty, there is an element with that appointmentId in the database");

            Appointment result = optionalResult.get();

            assertEquals(appointmentId, result.getAppointmentId(), "AppointmentId's should be equal");

            String expectedStartDateTimeString = "2025-01-01T10:00:00";
            String expectedEndDateTimeString = "2025-01-01T11:00:00";
            LocalDateTime expectedStartDateTime = LocalDateTime.parse(expectedStartDateTimeString);
            LocalDateTime expectedEndDateTime = LocalDateTime.parse(expectedEndDateTimeString);

            assertEquals(expectedStartDateTime, result.getStartDate(), "Start Dates should be equal");
            assertEquals(expectedEndDateTime, result.getEndDate(), "End Dates should be equal");
            assertEquals("Doctor Appointment", result.getTitle(), "Titles should be equal");
            assertEquals("Annual checkup", result.getDescription(), "Descriptions should be equal");

            List<Tag> expectedList = new ArrayList<>();
            Tag firstTagInExpectedList = new Tag(4, "Work", "blue");
            expectedList.add(firstTagInExpectedList);

            assertEquals(expectedList, result.getTags(), "Appointment 1 should only have 1 Tag");

        } catch (DataManagerException e) {
            fail("An exception occurred while fetching the appointment: " + e.getMessage());
        }
    }

    @Test
    void testGetTagsByIdFromDatabase() {
        int appointmentId = 3;

        try {
            List<Tag> fetchedTags = dm.getTagsByAppointmentId(appointmentId);

            assertFalse(fetchedTags.isEmpty(), "Tags should be present for appointmentId 3");


            List<Tag> expectedTags = new ArrayList<>();
            expectedTags.add(new Tag(1, "testingTag", "yellow"));
            expectedTags.add(new Tag(4, "Work", "blue"));

            assertEquals(expectedTags, fetchedTags, "Tags for appointmentId 3 should match the expected list");

        } catch (DataManagerException e) {
            fail("An exception occurred while fetching tags: " + e.getMessage());
        }
    }

    @Test
    void testGetAppointmentsByStartDate() {
        LocalDate testDate = LocalDate.of(2025, 1, 1);

        try {
            List<Appointment> fetchedAppointments = dm.getAppointmentsByDate(testDate, JooqDataManager.DateFilter.STARTDATE);

            assertFalse(fetchedAppointments.isEmpty(), "Appointments for the given date should not be empty");

            Appointment firstAppointment = fetchedAppointments.get(0);
            assertEquals(1, firstAppointment.getAppointmentId(), "First appointment ID should be 1");
            assertEquals("Doctor Appointment", firstAppointment.getTitle(), "First appointment title should be 'Doctor Appointment'");

        } catch (DataManagerException e) {
            fail("An exception occurred while fetching appointments by start date: " + e.getMessage());
        }
    }

    @Test
    void testGetAppointmentsByRangeWithTwoMatchingDates() throws DataManagerException {
        LocalDateTime rangeStart = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2025, 1, 2, 16, 0);

        List<Appointment> fetchedAppointments = dm.getAppointmentsByRange(rangeStart, rangeEnd);

        assertFalse(fetchedAppointments.isEmpty(), "Appointment fot the given range should not be empty");
        assertEquals(2, fetchedAppointments.size(), "There should be 2 appointments in this range");

    }

    @Test
    void testGetTagById() {
        int tagId = 1;
        try {
            Optional<Tag> optionalTag = dm.getTagById(tagId);

            assertTrue(optionalTag.isPresent(), "Tag with tagId = 1 should exist");

            Tag expectedTag = new Tag(1, "testingTag", "yellow");
            Tag actualTag = optionalTag.get();

            assertEquals(expectedTag.getTagId(), actualTag.getTagId(), "Tag IDs should match");
            assertEquals(expectedTag.getColor(), actualTag.getColor(), "Tag colors should match");
            assertEquals(expectedTag.getName(), actualTag.getName(), "Tag names should match");

        } catch (DataManagerException e) {
            fail("An exception occurred while fetching the tag: " + e.getMessage());
        }
    }

    @Test
    void testGetTagByIdFail() {
        int wrongTagId = 200;
        try {
            Optional<Tag> actualTag = dm.getTagById(wrongTagId);
            assertTrue(actualTag.isEmpty(), "There should be no Tag in the Database that matches the TagId 1200");

        } catch (DataManagerException e) {
            fail("An Error occurred while fetching the tag: " + e.getMessage());
        }
    }

    @Test
    void testGetAllTags() {
        int[] knownIds = {1, 2, 3, 4, 5, 6};
        List<Tag> individuallyFetchedTags = new ArrayList<>();
        try {
            for (int id : knownIds) {
                individuallyFetchedTags.add(dm.getTagById(id).get());
            }
            assertEquals(individuallyFetchedTags, dm.getAllTags(), "Fetching every Tag individually results in a different List than fetching all at once");
        } catch (DataManagerException e) {
            //Lisa wird das hier noch ändern heute
        }
    }
}