import static org.junit.jupiter.api.Assertions.*;

import Model.Database.DataManager;
import Model.Database.JooqDataManager;
import Model.Database.DataManagerException;
import Model.Entities.Appointment;
import Model.Entities.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DatabaseInsertTests {

    private final DataManager dm;

    {
        try {
            dm = new JooqDataManager("jdbc:sqlite:src/test/resources/javenderDatabase.db");
        } catch (DataManagerException e) {
            throw new RuntimeException("Couldn't connect to database");
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
            // fetching the appointment right after inserting it to see if everything worked correctly
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

            List<Tag> tagListOfAppointment = dm.getTagsByAppointmentId(insertedId);
            assertEquals(2, tagListOfAppointment.size(), "the appointment should have 2 tags associated with it");
            assertEquals(tagListOfAppointment, Arrays.asList(
                    new Tag(1,"testingTag", "yellow"),
                    new Tag(6,"Health", "red")
            ));

            dm.removeAppointment(actualInsertedAppointment);
            // removes inserted appointment to keep the database clean

            Optional<Appointment> fetchedAppointment = dm.getAppointmentById(insertedId);

            if (fetchedAppointment.isPresent()) {
                fail("Entry should not be in Database");
            }

            List<Tag> listOfTagsAfterDeletion = dm.getTagsByAppointmentId(insertedId);
            if (!listOfTagsAfterDeletion.isEmpty()) {
                fail("There should be no Tags associated with the deleted Appointment");
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
            // fetching the Tag right after inserting it to see if everything worked correctly
            Optional<Tag> insertedTag = dm.getTagById(insertId);

            if (insertedTag.isEmpty()) {
                fail("The Tag wasn't inserted into the Database");
            }

            Tag actualInsertedTag = insertedTag.get();

            assertEquals(insertId, actualInsertedTag.getTagId());
            assertEquals(insertTag.getName(),actualInsertedTag.getName());
            assertEquals(insertTag.getColor(), actualInsertedTag.getColor());

            // removes inserted Tag to keep the database clean
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