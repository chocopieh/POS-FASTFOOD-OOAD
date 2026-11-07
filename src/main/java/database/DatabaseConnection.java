package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Chuỗi kết nối đến SQL Server Express và database FastFoodDB của bạn
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=FastFoodDB;encrypt=true;trustServerCertificate=true;";
    
    private static final String USER = "sa"; // Tài khoản SQL của bạn (mặc định thường là sa)
    private static final String PASSWORD = "123456"; // Điền mật khẩu SSMS của bạn vào đây

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println(">>> Kết nối cơ sở dữ liệu thành công!");
            return conn;
        } catch (SQLException e) {
            System.out.println(">>> Lỗi kết nối CSDL: " + e.getMessage());
            return null;
        }
    }
}