import static org.junit.jupiter.api.Assertions.*;

import Domain.Database.DataManager;
import Domain.Entities.Appointment;
import Domain.Entities.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
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

    @Test
    void testGetAppointmentsByStartDate() {
        DataManager dm = new DataManager();
        LocalDate testDate = LocalDate.of(2025, 1, 1);

        Optional<List<Appointment>> optionalAppointments = dm.getAppointmentsByDate(testDate, DataManager.DateFilter.STARTDATE);

        assertTrue(optionalAppointments.isPresent(), "Appointments for the given date should not be empty");
        List<Appointment> appointments = optionalAppointments.get();
        assertFalse(appointments.isEmpty(), "Appointment list should not be empty");

        Appointment firstAppointment = appointments.get(0);
        assertEquals(1, firstAppointment.getAppointmentId());
        assertEquals("Doctor Appointment", firstAppointment.getTitle());
    }

    @Test
    void testGetAppointmentsByEndDate() {
        DataManager dm = new DataManager();
        LocalDate testDate = LocalDate.of(2025, 1, 2);

        Optional<List<Appointment>> optionalAppointments = dm.getAppointmentsByDate(testDate, DataManager.DateFilter.ENDDATE);

        assertTrue(optionalAppointments.isPresent(), "Appointments for the given date should not be empty");
        List<Appointment> appointments = optionalAppointments.get();
        assertFalse(appointments.isEmpty(), "Appointment list should not be empty");

        Appointment firstAppointment = appointments.get(0);
        assertEquals(2, firstAppointment.getAppointmentId());
        assertEquals("Team Meeting", firstAppointment.getTitle());
    }

    @Test
    void testGetAppointmentsByRangeWithTwoMatchingDates() {
        DataManager dm = new DataManager();
        LocalDateTime rangeStart = LocalDateTime.of(2025, 1, 1, 00, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2025, 1, 2, 16, 0);

        Optional<List<Appointment>> optionalAppointments = dm.getAppointmentsByRange(rangeStart, rangeEnd);

        assertTrue(optionalAppointments.isPresent(), "Appointments for the given range should not be empty");
        List<Appointment> appointments = optionalAppointments.get();
        assertFalse(appointments.isEmpty(), "Appointment list should not be empty");
        assertEquals(2, appointments.size(), "There are 2 Elements in this range");
    }

    @Test
    void testGetApppointmentsByRangeWithNoMatches() {
        DataManager dm = new DataManager();
        LocalDateTime rangeStart = LocalDateTime.of(2030, 1, 1, 0, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2040, 1, 1, 0, 0);

        Optional<List<Appointment>> optionalAppointments = dm.getAppointmentsByRange(rangeStart, rangeEnd);
        assertFalse(optionalAppointments.isPresent(), "There should be no Appointments in that Time Range");
    }
    @Test
    void testGetTagById(){
        DataManager dm = new DataManager();
        int tagId = 1;

        Optional<Tag> optionalTag = dm.getTagById(tagId);
        assertTrue(optionalTag.isPresent(), "Tag with tagId = 1 should exist");

        Tag expectedTag = new Tag(1, "testingTag", "yellow");
        Tag actualTag = optionalTag.get();

        assertEquals(expectedTag.getTagId(), actualTag.getTagId());
        assertEquals(expectedTag.getColor(), actualTag.getColor());
        assertEquals(expectedTag.getName(), actualTag.getName());
    }
    @Test
    void testGetAppointmentsByTagId(){
        DataManager dm = new DataManager();
        int tagId = 1;

        Optional<List<Appointment>> optionalAppointments = dm.getAppointmentsByTagId(tagId);
        assertTrue(optionalAppointments.isPresent(), "Appointments should be present for tagId = 1");

        List<Appointment> actualAppointments = optionalAppointments.get();
        Appointment actualFirstAppointment = actualAppointments.get(0);
        Appointment actualSecondAppointment = actualAppointments.get(1);

        List<Tag> actualFirstAppointmentTags = new ArrayList<>();
        actualFirstAppointmentTags.add(new Tag(1, "testingTag", "yellow"));

        List<Tag> actualSecondAppointmentTags = new ArrayList<>();
        actualSecondAppointmentTags.add(new Tag(1, "testingTag", "yellow"));
        actualSecondAppointmentTags.add(new Tag(4, "Work", "blue"));

        Appointment expectedFirstAppointment = new Appointment
                (2, LocalDateTime.parse("2025-01-02T14:00:00"),
                        LocalDateTime.parse("2025-01-02T15:30:00"), "Team Meeting",
                        "Monthly progress update", actualFirstAppointmentTags);

        Appointment expectedSecondAppointment = new Appointment
                (3, LocalDateTime.parse("2025-01-03T09:00:00"),
                        LocalDateTime.parse("2025-01-03T10:30:00"), "Client Presentation",
                        "Present new project proposal", actualSecondAppointmentTags);

        assertEquals(expectedFirstAppointment.getAppointmentId(), actualFirstAppointment.getAppointmentId());
        assertEquals(expectedFirstAppointment.getStartDate(), actualFirstAppointment.getStartDate());
        assertEquals(expectedFirstAppointment.getEndDate(), actualFirstAppointment.getEndDate());
        assertEquals(expectedFirstAppointment.getTitle(), actualFirstAppointment.getTitle());
        assertEquals(expectedFirstAppointment.getDescription(), actualFirstAppointment.getDescription());
        assertEquals(expectedFirstAppointment.getTags(), actualFirstAppointment.getTags());

        assertEquals(expectedSecondAppointment.getAppointmentId(), actualSecondAppointment.getAppointmentId());
        assertEquals(expectedSecondAppointment.getStartDate(), actualSecondAppointment.getStartDate());
        assertEquals(expectedSecondAppointment.getEndDate(), actualSecondAppointment.getEndDate());
        assertEquals(expectedSecondAppointment.getTitle(), actualSecondAppointment.getTitle());
        assertEquals(expectedSecondAppointment.getDescription(), actualSecondAppointment.getDescription());
        assertEquals(expectedSecondAppointment.getTags(), actualSecondAppointment.getTags());

    }
}
