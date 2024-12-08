
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Acer
 */
public class GetConnection {

    public Connection connect() {
        // Ganti dengan nama file database yang benar
        String url = "jdbc:sqlite:db_contacts.db";  
        Connection conn = null;
        try {
            // Mencoba membuka koneksi
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Database connected: " + meta.getDatabaseProductName());
                createTbl(conn); // Membuat tabel
            }
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }

    private void createTbl(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS db_contacts (" +  // Perbaiki nama tabel
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "nama TEXT NOT NULL, " +
                     "noHp TEXT NOT NULL, " +
                     "category TEXT NOT NULL);";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Berhasil membuat tabel");
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }
}
