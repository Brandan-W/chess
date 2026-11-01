package dataaccess;

import org.junit.jupiter.api.Test;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @Test
    void testDatabaseConnection() throws Exception {
        DatabaseManager.createDatabase();

        try (Connection connection = DatabaseManager.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            assertNotNull(connection.getCatalog());
            assertFalse(connection.getCatalog().isBlank());
        }
    }
}
