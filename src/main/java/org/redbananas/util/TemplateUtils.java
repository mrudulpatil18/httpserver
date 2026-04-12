package org.redbananas.util;

import org.redbananas.model.Note;

import java.time.format.DateTimeFormatter;

public class TemplateUtils {

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String getNoteDetailsTemplate(Note note) {
        if (note == null || note.getType() == null) {
            return "INVALID NOTE TYPE";
        }
        switch (note.getType()) {
            case NOTE -> {
                return "<p style=\"font-size: 1.2rem; white-space: pre-wrap;\">${TEXT_CONTENT}</p>";
            }
            case LINK -> {
                return """
                        <div class="text-center" style="padding: 2rem;">
                            <p>External Resource:</p>
                            <a href="${TEXT_CONTENT}" target="_blank" class="button" style="font-size: 1.5rem;">➜ VISIT LINK</a>
                            <p style="margin-top: 1rem; font-size: 0.8rem;">${TEXT_CONTENT}</p>
                        </div>
                """;
            }
            case SNIPPET -> {
                return """
                        <div class="code-container">
                            <pre><code>${TEXT_CONTENT}</code></pre>
                        </div>
                """;
            }
            case FILE ->  {
                return """
                       <div class="alert alert-info" style="display: flex; align-items: center; justify-content: space-between;">
                           <div>
                               <strong>FILE:</strong> ${ORIGINAL_FILE_NAME}<br>
                               <small>${SIZE_BYTES} bytes • ${CONTENT_TYPE}</small>
                           </div>
                           <a href="/download?id=${ID}" class="button" style="background: #00ff00;">⬇ DOWNLOAD</a>
                       </div>
                """;
            }
            default -> {
                return  "INVALID NOTE TYPE";
            }

        }
    }

    public static String getNoteHtml(Note note) {
        if (note == null) {
            return "INVALID NOTE";
        }
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
