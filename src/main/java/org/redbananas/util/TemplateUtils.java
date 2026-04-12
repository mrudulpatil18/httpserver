package org.redbananas.util;

import org.redbananas.model.Note;
import java.time.format.DateTimeFormatter;

public class TemplateUtils {

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String getNoteDetailsTemplate(Note note) {
        if (note == null || note.getType() == null) return "INVALID NOTE TYPE";

        return switch (note.getType()) {
            case NOTE -> "<p style=\"font-size: 1.2rem; white-space: pre-wrap;\">${TEXT_CONTENT}</p>";
            case LINK -> """
                    <div class="text-center" style="padding: 2rem;">
                        <p>External Resource:</p>
                        <a href="${TEXT_CONTENT}" target="_blank" class="button" style="font-size: 1.5rem;">➜ VISIT LINK</a>
                        <p style="margin-top: 1rem; font-size: 0.8rem;">${TEXT_CONTENT}</p>
                    </div>
                    """;
            case SNIPPET -> """
                    <div class="code-container">
                        <pre><code>${TEXT_CONTENT}</code></pre>
                    </div>
                    """;
            case FILE -> """
                    <div class="alert alert-info" style="display: flex; align-items: center; justify-content: space-between;">
                        <div>
                            <strong>FILE:</strong> ${ORIGINAL_FILE_NAME}<br>
                            <small>${SIZE_BYTES} bytes • ${CONTENT_TYPE}</small>
                        </div>
                        <a href="/download?id=${ID}" class="button" style="background: #00ff00;">⬇ DOWNLOAD</a>
                    </div>
                    """;
            default -> "INVALID NOTE TYPE";
        };
    }

    public static String getNoteHtml(Note note) {
        return applyCommon(getUnifiedTemplate(), note, "📄", "copyToClipboard('${raw_content}')", "📋", null);
    }

    public static String getLinkHtml(Note note) {
        return applyCommon(getUnifiedTemplate(), note, "🔗", "window.open('${raw_content}', '_blank')", "↗", null);
    }

    public static String getFileHtml(Note note) {
        String displayTitle = (note.getTitle() == null || note.getTitle().isEmpty())
                ? "FILE" : note.getTitle();

        // Pass the original file name as the custom preview
        String filePreview = note.getOriginalFileName() != null ? note.getOriginalFileName() : "unknown_file";

        return applyCommon(getUnifiedTemplate(), note, "📁", "location.href='/download?id=${id}'", "⬇", filePreview)
                .replace("${title}", escape(displayTitle));
    }

    private static String applyCommon(String template, Note note, String icon, String actionJs, String actionLabel, String customPreview) {
        // Logic for preview: Use customPreview if provided, otherwise fallback to textContent
        String previewText;
        if (customPreview != null) {
            previewText = customPreview;
        } else {
            String rawContent = note.getTextContent() != null ? note.getTextContent() : "";
            previewText = rawContent.replace("\n", " ").replace("\r", "");
        }

        if (previewText.length() > 80) previewText = previewText.substring(0, 77) + "...";

        return template
                .replace("${icon}", icon)
                .replace("${action_js}", actionJs)
                .replace("${action_label}", actionLabel)
                .replace("${preview}", escape(previewText)) // Now this always finds the placeholder
                .replace("${raw_content}", escapeJs(note.getTextContent()))
                .replace("${time_modified}", note.getModifiedAt() != null ? note.getModifiedAt().format(FORMATTER) : "N/A")
                .replace("${title}", escape(note.getTitle() == null || note.getTitle().isEmpty() ? "UNTITLED" : note.getTitle()))
                .replace("${id}", String.valueOf(note.getId()));
    }

    private static String getUnifiedTemplate() {
        return """
        <div class="list-item">
            <div class="item-body" onclick="location.href='/note/${id}'">
                <span class="item-icon">${icon}</span>
                <span class="item-title">${title}</span>
                <span class="item-preview">${preview}</span>
                <span class="item-meta">${time_modified}</span>
            </div>
            <div class="item-actions">
                <div class="action-btn main-action" onclick="event.stopPropagation(); ${action_js}" title="${action_label}">
                    ${action_label}
                </div>
                <div class="action-btn delete-trigger" onclick="event.stopPropagation(); deleteNote('${id}')">
                    ✕
                </div>
            </div>
        </div>
        """;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
    }
}