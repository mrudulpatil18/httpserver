package org.redbananas.service;

import lombok.AllArgsConstructor;
import org.redbananas.model.Note;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

@AllArgsConstructor
public class DbService {

    // TODO: refactor functions
    // TODO: integrate SQLite

    Connection conn;
    String dbPath = Paths.get("my.db").toAbsolutePath().toString();
    final String url = "jdbc:sqlite:" + dbPath;

    public DbService() {
        this.conn = setupSqliteDB(url);
    }

    private void saveFile(byte[] ar) {
        //do action
    }

    private String addFile(byte[] arr, String fileType) {
        saveFile(arr);
        return UUID.randomUUID().toString();
    }

    public String addNote(Note note) {
        var sql = "INSERT INTO notes VALUES (?, ?, ?, ?)";
        try {
            var psmt = conn.prepareStatement(sql);
            psmt.setString(1, note.getId());
            psmt.setString(2, note.getTitle());
            psmt.setString(3, note.getContent());
            psmt.setString(4, note.getType().name());
            psmt.executeUpdate();
            return note.getId();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private Connection setupSqliteDB(String url) {
        try {
            Connection connection = DriverManager.getConnection(url);

            // sql to create new table
            var sql = "CREATE TABLE IF NOT EXISTS notes (id TEXT UNIQUE PRIMARY KEY, title TEXT NOT NULL, content TEXT NOT NULL, type TEXT NOT NULL);";

            var stmt = connection.createStatement();
            stmt.execute(sql);

            return connection;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

}

