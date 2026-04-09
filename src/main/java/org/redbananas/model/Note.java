package org.redbananas.model;

import lombok.Data;

import java.util.Date;

@Data
public class Note {
    String id;
    TextNoteType type;
    String title;
    String textContent;

    String contentType; //MIME Type

    // int sizeBytes;

    String filePath;
    String originalFileName;

    Date createdAt;
    Date modifiedAt;

    // either filepath or textcontent
}
