import Model.Database.DataManager;
import Model.Database.DataManagerException;
import Model.Database.JooqDataManager;
import Controller.CalendarController;
import Model.Entities.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.jooq.impl.DSL.out;
import static org.jooq.impl.DSL.when;
import static org.junit.jupiter.api.Assertions.*;

public class CalendarControllerTests {

    private DataManager dm;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    @BeforeEach
    void setupDatabase() throws Exception {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/test/resources/javenderDataBase.db");
            Statement statement = connection.createStatement()) {

            String setupSql = Files.readString(Paths.get("src/test/resources/AddTestAppointments.sql"));
            statement.executeUpdate(setupSql);
        }

        dm = new JooqDataManager("jdbc:sqlite:src/test/resources/javenderDataBase.db");
        assertNotNull(dm, "Database connection must be established before running tests.");

        System.setOut(new PrintStream(outputStream)); // Umleiten von System.out
    }

    @AfterEach
    void cleanupDatabase() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:src/test/resources/javenderDataBase.db");
             Statement statement = connection.createStatement()) {

            String cleanupSql = Files.readString(Paths.get("src/test/resources/DeleteTestAppointments.sql"));
            statement.executeUpdate(cleanupSql);

            System.setIn(originalIn); // Wiederherstellen von System.in
            System.setOut(originalOut); // Wiederherstellen von System.out
        }

    }

    @Test
    void testValidateDate(){
        CalendarController cc = new CalendarController();
        String validDate = "2030-10-01";
        String invalidDate = "01-10-2030";
        assertFalse(cc.validateDate(validDate));
        assertTrue(cc.validateDate(invalidDate));
    }

    @Test
    void testValidateTime(){
        CalendarController cc = new CalendarController();
        String validTime = "12:00";
        String invalidTime = "12-00";
        assertFalse(cc.validateTime(validTime));
        assertTrue(cc.validateTime(invalidTime));
    }

    @Test
    void testValidateDateTimeOrder(){
        CalendarController cc = new CalendarController();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = LocalDateTime.now().minusDays(1);
        assertTrue(cc.validateDateTimeOrder(now, tomorrow));
        assertFalse(cc.validateDateTimeOrder(tomorrow, now));
    }

    @Test
    void testValidateDateEdgeCases() {
        CalendarController cc = new CalendarController();

        String emptyDate = "";
        String nullDate = null;

        assertTrue(cc.validateDate(emptyDate));
        assertThrows(NullPointerException.class, () -> cc.validateDate(nullDate));
    }

    @Test
    void testAddNewTag() throws DataManagerException {

        String simulatedInput = "Pumpen gehen\nred\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        CalendarController cc = new CalendarController();
        cc.addTag();

        String output = outputStream.toString();
        assertTrue(output.contains("Enter the details of the new Tag"));
        assertTrue(output.contains("Tag title:"));
        assertTrue(output.contains("Tag color:"));

        Tag addedTag = dm.getTagByName("Pumpen gehen").orElse(null);
        assertNotNull(addedTag);
        assertEquals("red", addedTag.getColor());
    }

    @Test
    void testAddAndOverwriteExistingTag() throws DataManagerException {

        String simulatedInput = "Personal\n1\nblue\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        CalendarController cc = new CalendarController();
        Tag existingTag = dm.getTagByName("Personal").orElse(null);
        assertNotNull(existingTag);
        assertEquals("Personal", existingTag.getName());
        assertEquals("red", existingTag.getColor());
        int existingTagId = existingTag.getTagId();

        // Adding + Overwriting the Personal tag
        cc.addTag();

        String output = outputStream.toString();
        assertTrue(output.contains("Enter the details of the new Tag"));
        assertTrue(output.contains("Tag title:"));
        assertTrue(output.contains("The tag with the name \"Personal\" already exists."));
        assertTrue(output.contains("Tag color:"));
        assertTrue(output.contains("You have successfully overwritten the tag. Updated tag: \"Personal\""));

        Tag addedTag = dm.getTagByName("Personal").orElse(null);
        assertNotNull(addedTag);
        assertEquals("blue", addedTag.getColor());
        assertEquals(addedTag.getTagId(), existingTagId);
    }

    @Test
    void testAddAndCancelOverwriteTag() throws DataManagerException {

        String simulatedInput = "Personal\n2\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        CalendarController cc = new CalendarController();

        Tag existingTag = dm.getTagByName("Personal").orElse(null);
        assertNotNull(existingTag);
        assertEquals("Personal", existingTag.getName());
        assertEquals("red", existingTag.getColor());
        int existingTagId = existingTag.getTagId();

        // Adding + Cancel Overwriting the tag
        cc.addTag();

        String output = outputStream.toString();
        assertTrue(output.contains("Enter the details of the new Tag"));
        assertTrue(output.contains("Tag title:"));
        assertTrue(output.contains("Canceled! You have not overwritten the tag."));

        Tag finalTag = dm.getTagByName("Personal").orElse(null);
        assertNotNull(finalTag);
        assertEquals("red", finalTag.getColor());
    }



}

