package dev.mrudul.util;

import dev.mrudul.model.Note;

import java.time.format.DateTimeFormatter;

public class TemplateUtils {

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Unified helper to handle the preview logic correctly
    private static String applyCommon(String template, Note note, String customPreview) {

        String previewText = (customPreview != null) ? customPreview :
                (note.getTextContent() != null
                        ? note.getTextContent().replace("\n", " ").replace("\r", "")
                        : "");

        if (previewText.length() > 80) previewText = previewText.substring(0, 77) + "...";

        return template
                .replace("${raw_content}", escapeJs(note.getTextContent()))
                .replace("${type}", note.getType().name())
                .replace("${preview}", escape(previewText))
                .replace("${time_modified}",
                        note.getModifiedAt() != null
                                ? note.getModifiedAt().format(FORMATTER)
                                : "N/A")
                .replace("${title}",
                        escape(note.getTitle() == null || note.getTitle().isEmpty()
                                ? "UNTITLED"
                                : note.getTitle()))
                .replace("${id}", String.valueOf(note.getId()));
    }

    public static String getNoteHtml(Note note) {
        return applyCommon(getUnifiedTemplate(), note, null);
    }

    public static String getLinkHtml(Note note) {
        return applyCommon(getUnifiedTemplate(), note, null);
    }

    public static String getSnippetHtml(Note note) {
        return applyCommon(getUnifiedTemplate(), note, null);
    }

    public static String getFileHtml(Note note) {
        String filePreview = note.getOriginalFileName() != null
                ? note.getOriginalFileName()
                : "unknown_file";

        return applyCommon(getUnifiedTemplate(), note, filePreview);
    }

    private static String getUnifiedTemplate() {
        return """
                <div class="list-item">
                    <div class="item-body" onclick="location.href='note/${id}'">
                        <span class="item-title">${title}</span>
                        <span class="item-preview">${preview}</span>
                        <span class="item-meta">${time_modified}</span>
                    </div>
                    <div class="item-actions">
                        <div class="action-btn"
                             data-type="${type}"
                             data-content="${raw_content}"
                             data-id="${id}"
                             onclick="event.stopPropagation(); handleAction(this)">
                            ⋯
                        </div>
                        <div class="action-btn delete-trigger"
                             onclick="event.stopPropagation(); deleteNote('${id}')">
                            ×
                        </div>
                    </div>
                </div>
                """;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
    }

    // Switch expression for details page
    public static String getNoteDetailsTemplate(Note note) {
        if (note == null || note.getType() == null) return "INVALID NOTE TYPE";

        return switch (note.getType()) {
            case NOTE -> """
                    <div style="display:flex; justify-content:flex-end; margin-bottom:6px;">
                        <button
                            data-content="${TEXT_CONTENT_ESC}"
                            onclick="copyFromDetails(this)"
                            style="
                                font-size:0.7rem;
                                padding:2px 6px;
                                border:1px solid #000;
                                background:transparent;
                                cursor:pointer;
                                opacity:0.6;
                            ">
                            copy
                        </button>
                    </div>
                    <p style="font-size: 1.2rem; white-space: pre-wrap;">${TEXT_CONTENT}</p>
                    """;

            case SNIPPET -> """
                    <div style="display:flex; justify-content:flex-end; margin-bottom:6px;">
                        <button
                            data-content="${TEXT_CONTENT_ESC}"
                            onclick="copyFromDetails(this)"
                            style="
                                font-size:0.7rem;
                                padding:2px 6px;
                                border:1px solid #000;
                                background:transparent;
                                cursor:pointer;
                                opacity:0.6;
                            ">
                            copy
                        </button>
                    </div>
                    <div class="code-container">
                        <pre><code>${TEXT_CONTENT}</code></pre>
                    </div>
                    """;

            case LINK -> """
                    <div class="text-center">
                        <a href="${TEXT_CONTENT}" target="_blank" class="button">↗ VISIT LINK</a>
                    </div>
                    """;

            case FILE -> """
                    <div class="alert alert-info">
                        <strong>FILE:</strong> ${ORIGINAL_FILE_NAME}<br>
                        <a href="download?id=${ID}" class="button mt-1">⬇ DOWNLOAD</a>
                    </div>
                    """;

            default -> "INVALID";
        };
    }
}