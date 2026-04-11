package org.redbananas.service;

import lombok.AllArgsConstructor;
import org.redbananas.model.Note;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@AllArgsConstructor
public class DbService {

    Connection conn;
    String dbPath = Paths.get("data/my.db").toAbsolutePath().toString();
    final String url = "jdbc:sqlite:" + dbPath;

    public DbService() {
        this.conn = setupSqliteDB(url);
    }

    public String saveFile(byte[] arr, String filePath) {
        Path path = Paths.get(filePath);

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, arr);
            return path.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save fil: " + filePath, e);
        }


    }

    public String addNote(Note note) {
        var sql = """
                INSERT INTO notes
                (id, type, title, text_content, content_type,
                 file_path, original_file_name, created_at, modified_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try {
            var psmt = conn.prepareStatement(sql);

            psmt.setString(1, note.getId());
            psmt.setString(2, note.getType().name());
            psmt.setString(3, note.getTitle());
            psmt.setString(4, note.getTextContent());
            psmt.setString(5, note.getContentType());

            psmt.setString(6, note.getFilePath());
            psmt.setString(7, note.getOriginalFileName());

            psmt.setString(8, note.getCreatedAt().toString());
            psmt.setString(9, note.getModifiedAt().toString());

            psmt.executeUpdate();
            return note.getId();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection setupSqliteDB(String url) {
        try {
            Connection connection = DriverManager.getConnection(url);

            // sql to create new table
            var sql = """
                    CREATE TABLE IF NOT EXISTS notes (
                        id TEXT UNIQUE PRIMARY KEY,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        text_content TEXT,
                        content_type TEXT NOT NULL,
                        file_path TEXT,
                        original_file_name TEXT,
                        created_at TEXT NOT NULL,
                        modified_at TEXT NOT NULL
                    );
                    """;
            var stmt = connection.createStatement();
            stmt.execute(sql);

            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

