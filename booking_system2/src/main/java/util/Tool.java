package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DB connection helper.
 * 使用方式：
 *   Tool conn = new Tool();
 *   Connection c = conn.getDb();
 */
public class Tool {
    // TODO: 依你的 MySQL 設定修改
    private static final String URL =
            "jdbc:mysql://localhost:3306/booking_system2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Taipei&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found", e);
        }
    }

    public Connection getDb() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
