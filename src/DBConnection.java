import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL =
        System.getenv().getOrDefault("PIMS_DB_URL",
            "jdbc:mariadb://localhost:3306/pims");
    private static final String USER =
        System.getenv().getOrDefault("PIMS_DB_USER", "pims_app");
    private static final String PASSWORD =
        System.getenv().getOrDefault("PIMS_DB_PASSWORD", "");

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MariaDB driver not on classpath: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
