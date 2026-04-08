package org.redbananas.model;

import lombok.Data;

@Data
public class Note {
    String id;
    String title;
    String content;
    TextNoteType type;
}
