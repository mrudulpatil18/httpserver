package dev.mrudul.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Note {
    String id;
    NoteType type;
    String title;
    String textContent;

    String contentType; //MIME Type

     int sizeBytes; // for files

    String filePath;
    String originalFileName;

    LocalDateTime createdAt;
    LocalDateTime modifiedAt;

    // either filepath or textcontent
}
