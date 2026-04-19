package dev.mrudul.service;

import lombok.AllArgsConstructor;
import dev.mrudul.model.Note;
import dev.mrudul.model.NoteType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class DbService {

    Connection conn;
    private static final String DATA_DIR = "/var/lib/raw-notes";
    private static final String DB_PATH = DATA_DIR + "/my.db";
    final String url = "jdbc:sqlite:" + DB_PATH;

    public DbService() {
        try {
            Files.createDirectories(Paths.get("/var/lib/raw-notes/files"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public byte[] readFile(String filePath) throws RuntimeException {
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
            psmt.setInt(10, note.getSizeBytes());

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
            return getNoteFromResultSet(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public List<Note> getAllNotes() {
        var sql = """
                SELECT * FROM notes ORDER BY modified_at DESC;
        """;

        try{
            var psmt = conn.prepareStatement(sql);
            var rs = psmt.executeQuery();

            List<Note> notes = new ArrayList<>();

            while(rs.next()){
                notes.add(getNoteFromResultSet(rs));
               }

            return notes;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void updateNote(Note note) {

        var sql = """
        UPDATE notes
        SET
            title = ?,
            text_content = ?,
            content_type = ?,
            file_path = ?,
            original_file_name = ?,
            modified_at = ?,
            size_bytes = ?
        WHERE id = ?;
        """;

        try (var psmt = conn.prepareStatement(sql)) {

            psmt.setString(1, note.getTitle());
            psmt.setString(2, note.getTextContent());
            psmt.setString(3, note.getContentType());
            psmt.setString(4, note.getFilePath());
            psmt.setString(5, note.getOriginalFileName());
            psmt.setString(6, note.getModifiedAt().toString());
            psmt.setInt(7, note.getSizeBytes());
            psmt.setString(8, note.getId());

            psmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteNote(String id) {
        var sql = """
                DELETE FROM notes WHERE id = ?;
        """;

        Note note = getNote(id);

        try{
            var psmt = conn.prepareStatement(sql);
            psmt.setString(1, id);

            if(note.getType() == NoteType.FILE){
                Path path = Paths.get(note.getFilePath());
                Files.deleteIfExists(path);
            }

            psmt.executeUpdate();

        }catch (SQLException | IOException e){
            throw new RuntimeException(e);
        }
    }

    private Note getNoteFromResultSet(ResultSet rs) throws SQLException {
        Note note = new Note();
        note.setId(rs.getString("id"));

        // Safety check for Enum parsing
        String typeStr = rs.getString("type");
        if (typeStr != null) {
            note.setType(NoteType.valueOf(typeStr));
        }

        note.setTitle(rs.getString("title"));
        note.setTextContent(rs.getString("text_content"));
        note.setContentType(rs.getString("content_type"));
        note.setFilePath(rs.getString("file_path"));
        note.setOriginalFileName(rs.getString("original_file_name"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            note.setCreatedAt(LocalDateTime.parse(createdAtStr));
        }

        String modifiedAtStr = rs.getString("modified_at");
        if (modifiedAtStr != null) {
            note.setModifiedAt(LocalDateTime.parse(modifiedAtStr));
        }

        note.setSizeBytes(rs.getInt("size_bytes"));
        return note;
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

