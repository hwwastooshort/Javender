import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseTests {
    @Test
    public void testDatabaseConnection() {
        String url = "jdbc:sqlite:src/main/resources/javenderDataBase.db";

        try (Connection connection = DriverManager.getConnection(url)) {
            // Überprüfen, ob die Verbindung nicht null ist
            assertNotNull(connection, "Connection to Database should not be null");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AssertionError("Connection couldn't be established", e);
        }
    }

}
