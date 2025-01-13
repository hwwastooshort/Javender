import static org.junit.jupiter.api.Assertions.*;

import Domain.Database.DataManager;
import Domain.Entities.Appointment;
import Domain.Entities.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DatabaseInsertTests {

    private final DataManager dm = new DataManager();

    @AfterEach
    void cleanUp() {
        dm.removeAppointmentById(100);
        dm.removeTagByTagId(100);
    }

    @Test
    void testSuccessfulAppointmentInsertion() {
        Appointment appointment = new Appointment(
                100,
                LocalDateTime.parse("2026-01-01T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                LocalDateTime.parse("2027-01-01T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "Handy spielen!",
                "Brawl Stars mit Simon",
                new ArrayList<>(Arrays.asList(new Tag(1, "testingTag", "yellow")))
        );

        boolean result = dm.addAppointment(appointment);

        assertTrue(result, "Insertion of this Appointment should be possible");

        Optional<Appointment> checkAppointment = dm.getAppointmentById(100);

        assertTrue(checkAppointment.isPresent(), "Appointment should now be in the database");

        Appointment appointmentFromDatabase = checkAppointment.get();
        assertEquals(appointment.getAppointmentId(), appointmentFromDatabase.getAppointmentId());
        assertEquals(appointment.getStartDate(), appointmentFromDatabase.getStartDate());
        assertEquals(appointment.getEndDate(), appointmentFromDatabase.getEndDate());
        assertEquals(appointment.getTitle(), appointmentFromDatabase.getTitle());
        assertEquals(appointment.getDescription(), appointmentFromDatabase.getDescription());
        assertEquals(appointment.getTags(), appointmentFromDatabase.getTags());
    }

    @Test
    void testUnsuccessfulAppointmentInsertion_IdAlreadyInDataBase() {
        Appointment appointment = new Appointment(
                1,
                LocalDateTime.parse("2025-01-01T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                LocalDateTime.parse("2025-01-08T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "Taking the most important step",
                "the next one",
                new ArrayList<>(List.of(new Tag(1, "testingTag", "yellow")))
        );

        boolean result = dm.addAppointment(appointment);
        assertFalse(result, "Insertion should fail, Id is already in use");
    }

    @Test
    void testSuccessfulTagInsertion() {
        Tag tag = new Tag(100, "Simon Says a lot", "käseweiß");
        boolean result = dm.addTag(tag);
        assertTrue(result, "Insertion should not fail, tagId is free");

    }
}