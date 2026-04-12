package org.redbananas.engine;

import org.redbananas.model.Note;

import java.time.format.DateTimeFormatter;

public class TemplateUtils {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String getNoteHtml(Note note) {
        return applyCommon(getNoteTemplate(), note);
    }

    public static String getLinkHtml(Note note) {
        return applyCommon(getLinkTemplate(), note);
    }

    public static String getFileHtml(Note note) {
        return applyCommon(getFileTemplate(), note)
                .replace("${original_file_name}", escape(note.getOriginalFileName()));
    }

    // ONE place for replacements
    private static String applyCommon(String template, Note note) {
        return template
                .replace("${time_modified}", note.getModifiedAt().format(FORMATTER))
                .replace("${title}", escape(note.getTitle()))
                .replace("${text_content}", escape(note.getTextContent()))
                .replace("${id}", String.valueOf(note.getId()));
    }

    // Prevent XSS
    private static String escape(String s) {
        if (s == null) return "";
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // snippets and text
    private static String getNoteTemplate() {
        return """
                <div class="box" data-type="text">
                    <div class="timestamp">${time_modified}}</div>
                    <div class="note-body" onclick="primaryAction(this)">
                        <div class="note-content">
                            <strong>${title}</strong><br/>
                            ${text_content}
                        </div>
                    </div>
                    <div class="actions-group">
                        <a href="/note/${id}" class="action">⤢</a>
                        <div class="action" onclick="deleteNote(${id})">✕</div>
                    </div>
                </div>
                """;
    }

    // link
    private static String getLinkTemplate() {
        return """
                <div class="box" data-type="link" data-url="${text_content}">
                       <div class="timestamp">${time_modified}</div>
                       <div class="note-body" onclick="primaryAction(this)">
                           <div class="note-content">
                               <strong>${title}</strong><br/>
                               ${text_content}
                           </div>
                       </div>
                       <div class="actions-group">
                           <a href="/note/${id}" class="action">⤢</a>
                           <div class="action" onclick="deleteNote(${id})">✕</div>
                       </div>
                   </div>
                """;
    }

    // Files
    private static String getFileTemplate() {
        return """
               <div class="box" data-type="file" data-url="/download?id=${id}">
                    <div class="timestamp">${time_modified}}</div>
                    <div class="note-body" onclick="primaryAction(this)">
                        <div class="note-content">
                            <strong>${original_file_name}</strong><br/>
                        </div>
                    </div>
                    <div class="actions-group">
                        <a href="/note/${id}" class="action">⤢</a>
                        <div class="action" onclick="deleteNote(${id})">✕</div>
                    </div>
                </div>
               """;
    }

}
