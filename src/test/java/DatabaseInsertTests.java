import static org.junit.jupiter.api.Assertions.*;

import Model.Database.DataManager;
import Model.Database.DataManagerException;
import Model.Entities.Appointment;
import Model.Entities.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DatabaseInsertTests {

    private final DataManager dm = new DataManager("jdbc:sqlite:src/test/resources/javenderDatabase.db");

    @AfterEach
    void cleanUp() {
        try {
            dm.removeAppointmentById(100);
        } catch (DataManagerException ignored) {
            // ignore Exception
        }

        try {
            dm.removeTagByTagId(100);
        } catch (DataManagerException ignored) {
            // Ignore Exception
        }
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

        try {
            boolean result = dm.addAppointment(appointment);

            assertTrue(result, "Insertion of this Appointment should be possible");

            Optional<Appointment> checkAppointment = dm.getAppointmentById(100);

            assertTrue(checkAppointment.isPresent(), "Appointment should now be in the database");

            Appointment appointmentFromDatabase = checkAppointment.get();
            assertEquals(appointment.getAppointmentId(), appointmentFromDatabase.getAppointmentId(),
                    "Appointment ID should match");
            assertEquals(appointment.getStartDate(), appointmentFromDatabase.getStartDate(),
                    "Start date should match");
            assertEquals(appointment.getEndDate(), appointmentFromDatabase.getEndDate(),
                    "End date should match");
            assertEquals(appointment.getTitle(), appointmentFromDatabase.getTitle(),
                    "Title should match");
            assertEquals(appointment.getDescription(), appointmentFromDatabase.getDescription(),
                    "Description should match");
            assertEquals(appointment.getTags(), appointmentFromDatabase.getTags(),
                    "Tags should match");

        } catch (DataManagerException e) {
            fail("Exception should not be thrown during successful appointment insertion: " + e.getMessage());
        }
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

        try {
            boolean result = dm.addAppointment(appointment);
            assertFalse(result, "Insertion should fail, Id is already in use");
        } catch (DataManagerException e) {
            assertTrue(e.getMessage().contains("UNIQUE constraint failed"),
                    "Expected UNIQUE constraint violation, but got: " + e.getMessage());
        }
    }

    @Test
    void testSuccessfulTagInsertion() {
        Tag tag = new Tag(100, "Simon Says a lot", "käseweiß");

        try {
            boolean result = dm.addTag(tag);
            assertTrue(result, "Insertion should not fail, tagId is free");

            Optional<Tag> checkTag = dm.getTagById(100);
            assertTrue(checkTag.isPresent(), "Tag should now be in the database");

            Tag tagFromDatabase = checkTag.get();
            assertEquals(tag.getTagId(), tagFromDatabase.getTagId(), "Tag ID should match");
            assertEquals(tag.getName(), tagFromDatabase.getName(), "Tag name should match");
            assertEquals(tag.getColor(), tagFromDatabase.getColor(), "Tag color should match");

        } catch (DataManagerException e) {
            fail("Exception should not be thrown during successful tag insertion: " + e.getMessage());
        }
    }
}