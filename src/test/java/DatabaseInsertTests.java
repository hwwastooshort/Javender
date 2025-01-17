import static org.junit.jupiter.api.Assertions.*;

import Model.Database.JooqDataManager;
import Model.Database.DataManagerException;
import Model.Entities.Appointment;
import Model.Entities.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

public class DatabaseInsertTests {

    private final JooqDataManager dm = new JooqDataManager("jdbc:sqlite:src/test/resources/javenderDatabase.db");

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
    void testAddAppointment() {
        Appointment insertAppointment = new Appointment(
                LocalDateTime.parse("2030-10-01T00:00:00"),
                LocalDateTime.parse("2030-10-01T02:00:00"),
                "BastiGHG gucken",
                "Mit Mike, Lisa, Simon und hw nen BastiGHG Atzenstream anmachen",
                Arrays.asList(
                        new Tag(1,"testingTag", "yellow"),
                        new Tag(6,"Health", "red")
                )
        );

        try {
            int insertedId = dm.addAppointment(insertAppointment);
            Optional<Appointment> insertedAppointment = dm.getAppointmentById(insertedId);

            if (insertedAppointment.isEmpty()) {
                fail("The Appointment wasn't inserted into the database");
            }

            Appointment actualInsertedAppointment = insertedAppointment.get();

            assertEquals(insertedId, actualInsertedAppointment.getAppointmentId());
            assertEquals(insertAppointment.getTitle(), actualInsertedAppointment.getTitle());
            assertEquals(insertAppointment.getStartDate(), actualInsertedAppointment.getStartDate());
            assertEquals(insertAppointment.getEndDate(), actualInsertedAppointment.getEndDate());
            assertEquals(insertAppointment.getDescription(), actualInsertedAppointment.getDescription());
            assertEquals(insertAppointment.getTags(), actualInsertedAppointment.getTags());

            dm.removeAppointment(actualInsertedAppointment);

            Optional<Appointment> fetchedAppointment = dm.getAppointmentById(insertedId);

            if (fetchedAppointment.isPresent()) {
                fail("Entry should not be in Database");
            }

        } catch (DataManagerException e) {
            e.printStackTrace();
            fail("Something went wrong");
        }
    }

    @Test
    void testAddingAndRemovingTags() {
        Tag insertTag = new Tag("Hardcore Bodybuilding", "Regenbogen");
        try {
            int insertId = dm.addTag(insertTag);
            Optional<Tag> insertedTag = dm.getTagById(insertId);

            if (insertedTag.isEmpty()) {
                fail("The Tag wasn't inserted into the Database");
            }

            Tag actualInsertedTag = insertedTag.get();

            assertEquals(insertId, actualInsertedTag.getTagId());
            assertEquals(insertTag.getName(),actualInsertedTag.getName());
            assertEquals(insertTag.getColor(), actualInsertedTag.getColor());

            dm.removeTag(actualInsertedTag);
            Optional<Tag> fetchedTag = dm.getTagById(insertId);

            if (fetchedTag.isPresent()) {
                fail("Entry should not be in Database");
            }


        } catch (DataManagerException e) {
            e.printStackTrace();
            fail("Something went wrong: " + e.getMessage());
        }
    }
}