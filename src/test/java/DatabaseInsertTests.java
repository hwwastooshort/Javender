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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DatabaseInsertTests {

    private DataManager dm;

    @BeforeEach
    void setupDatabase() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:src/test/resources/javenderDataBase.db");
             Statement statement = connection.createStatement()) {

            String setupSql = Files.readString(Paths.get("src/test/resources/DeleteTestAppointments.sql"));
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
    void testAddAppointment() throws DataManagerException {
        Tag tag1 = new Tag("testingTag", "yellow");
        Tag tag2 = new Tag("Uni", "purple");

        int tag1Id = dm.addTag(tag1);
        int tag2Id = dm.addTag(tag2);

        List<Tag> testTags = Arrays.asList(
                new Tag(tag1Id, "testingTag", "yellow"),
                new Tag(tag2Id, "Uni", "purple")
        );

        Appointment insertAppointment = new Appointment(
                LocalDateTime.parse("2030-10-01T00:00:00"),
                LocalDateTime.parse("2030-10-01T02:00:00"),
                "Meeting",
                "Presenting results",
                testTags
        );

        int insertedId = dm.addAppointment(insertAppointment);
        // fetching the appointment right after inserting it to see if everything worked correctly
        Optional<Appointment> optionalInsertedAppointment = dm.getAppointmentById(insertedId);
        assertTrue(optionalInsertedAppointment.isPresent(), "The appointment should exist in the database");

        Appointment actualInsertedAppointment = optionalInsertedAppointment.get();

        assertEquals(insertedId, actualInsertedAppointment.getAppointmentId(), "Appointment IDs should match");
        assertEquals(insertAppointment.getTitle(), actualInsertedAppointment.getTitle(), "Titles should match");
        assertEquals(insertAppointment.getDescription(), actualInsertedAppointment.getDescription(), "Descriptions should match");
        assertEquals(insertAppointment.getStartDate(), actualInsertedAppointment.getStartDate(), "Start dates should match");
        assertEquals(insertAppointment.getEndDate(), actualInsertedAppointment.getEndDate(), "End dates should match");

        List<Tag> tagListOfAppointment = dm.getTagsByAppointmentId(insertedId);
        assertEquals(2, tagListOfAppointment.size(), "The appointment should have 2 tags associated with it");
        assertEquals(testTags, tagListOfAppointment, "The tags should match the expected values");

        dm.removeAppointment(actualInsertedAppointment);

        Optional<Appointment> fetchedAfterRemoval = dm.getAppointmentById(insertedId);
        assertFalse(fetchedAfterRemoval.isPresent(), "The appointment should be removed from the database");

        List<Tag> tagsAfterRemoval = dm.getTagsByAppointmentId(insertedId);
        assertTrue(tagsAfterRemoval.isEmpty(), "Tags associated with the removed appointment should also be removed");
    }

    @Test
    void testAddingAndRemovingTags() throws DataManagerException {
        Tag insertTag = new Tag("Hardcore Bodybuilding", "Regenbogen");

        int insertId = dm.addTag(insertTag);
        // fetching the Tag right after inserting it to see if everything worked correctly
        Optional<Tag> insertedTag = dm.getTagById(insertId);
        assertTrue(insertedTag.isPresent(), "The tag should exist in the database");

        Tag actualTag = insertedTag.get();

        assertEquals(insertId, actualTag.getTagId(), "Tag IDs should match");
        assertEquals(insertTag.getName(), actualTag.getName(), "Tag names should match");
        assertEquals(insertTag.getColor(), actualTag.getColor(), "Tag colors should match");

        dm.removeTag(actualTag);

        Optional<Tag> fetchedTagAfterRemoval = dm.getTagById(insertId);
        assertFalse(fetchedTagAfterRemoval.isPresent(), "The tag should be removed from the database");
    }

    @Test
    void testUpdateTag() throws DataManagerException {
        Tag startTag = new Tag("Start", "blue");
        dm.addTag(startTag);
        assertEquals(startTag, dm.getTagByTitle("Start").orElseThrow(), "The tag should be present in the database");
        dm.updateTag(new Tag("Start", "yellow"));
        assertEquals("yellow", dm.getTagByTitle("Start").orElseThrow().getColor(), "The tag should have been updated");

    }
}