package org.redbananas.service;

import lombok.AllArgsConstructor;
import org.redbananas.model.Note;
import org.redbananas.model.NoteType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

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

    public byte[] readFile(String filePath) {
        Path path = Paths.get(filePath);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    public String addNote(Note note) {
        var sql = """
                INSERT INTO notes
                (id, type, title, text_content, content_type,
                 file_path, original_file_name, created_at, modified_at, size_bytes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            psmt.setString(10, String.valueOf(note.getSizeBytes()));

            psmt.executeUpdate();
            return note.getId();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Note getNote(String id) {
        var sql = """
                SELECT * FROM notes WHERE id = ?;
                """;

        try {
            var psmt = conn.prepareStatement(sql);
            psmt.setString(1, id);

            var rs = psmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            Note note = new Note();
            note.setId(rs.getString("id"));
            note.setType(NoteType.valueOf(rs.getString("type")));
            note.setTitle(rs.getString("title"));
            note.setTextContent(rs.getString("text_content"));
            note.setContentType(rs.getString("content_type"));
            note.setFilePath(rs.getString("file_path"));
            note.setOriginalFileName(rs.getString("original_file_name"));
            note.setSizeBytes(rs.getInt("size_bytes"));

            note.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
            note.setModifiedAt(LocalDateTime.parse(rs.getString("modified_at")));

            return note;


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
                        modified_at TEXT NOT NULL,
                        size_bytes INTEGER
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

