package dev.mrudul.util;

import dev.mrudul.model.Header;
import dev.mrudul.model.HttpRequest;
import dev.mrudul.model.HttpResponse;
import dev.mrudul.model.Note;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.mrudul.util.TemplateUtils.FORMATTER;

public class StaticFileUtil {

    private static byte[] readFile(String serverPath) {
        try {
            return ResourceLoaderUtil.readBytes(serverPath);
        } catch (Exception e) {
            return ResourceLoaderUtil.readBytes("static/404.html");
        }
    }

    public static HttpResponse generateResponse(HttpRequest request, String filePath, Header[] headers) throws IOException {
        HttpResponse response = new HttpResponse();
        response.setProtocol(request.getProtocol());
        response.setResponseCode(200);
        response.setReasonPhrase("OK");
        byte[] file = readFile(filePath);
        Header contentLengthHeader = new Header("Content-Length", String.valueOf(file.length));
        response.setHeaders(Stream.concat(Arrays.stream(headers), Stream.of(contentLengthHeader)).collect(Collectors.toSet()));
        response.setResponseBody(file);

        return response;
    }
    public static HttpResponse generateHomePage(HttpRequest request, String filePath, Header[] headers, List<Note> notes) throws IOException {
        HttpResponse response = new HttpResponse();
        response.setProtocol(request.getProtocol());
        response.setResponseCode(200);
        response.setReasonPhrase("OK");
        byte[] file = readFile(filePath);
        String htmlFileContents = new String(file);
        String notesHtml = "";
        for(Note note : notes){
            if(note.getType() == null){continue;}
            switch (note.getType()){
                case FILE -> notesHtml = notesHtml.concat(TemplateUtils.getFileHtml(note) + "\n");
                case LINK -> notesHtml = notesHtml.concat(TemplateUtils.getLinkHtml(note) + "\n");
                case NOTE, SNIPPET ->  notesHtml = notesHtml.concat(TemplateUtils.getNoteHtml(note) + "\n");
                default ->  notesHtml = notesHtml.concat("\n");
            }
        }
        if(notesHtml.isEmpty()){
            htmlFileContents = htmlFileContents.replace("${NOTES_PLACEHOLDER}", " <div class=\"alert alert-info\">\n" +
                    "        Add Notes to display.\n" +
                    "    </div>");
        }else{
            htmlFileContents = htmlFileContents.replace("${NOTES_PLACEHOLDER}", notesHtml);
        }
        response.setResponseBody(htmlFileContents.getBytes(StandardCharsets.UTF_8));
        Header contentLengthHeader = new Header("Content-Length", String.valueOf(response.getResponseBody().length));
        response.setHeaders(Stream.concat(Arrays.stream(headers), Stream.of(contentLengthHeader)).collect(Collectors.toSet()));

        return response;
    }

    public static HttpResponse generateNotePage(HttpRequest request,  Header[] headers, Note note) throws IOException {
        HttpResponse response = new HttpResponse();
        response.setProtocol(request.getProtocol());
        response.setResponseCode(200);
        response.setReasonPhrase("OK");
        byte[] file = readFile("static/note_details.html");
        String htmlFileContents = new String(file);

        if(note != null) {
            htmlFileContents = htmlFileContents
                    .replace("${CONTENT_BLOCK}", TemplateUtils.getNoteDetailsTemplate(note))
                    .replace("${TITLE}", safe(note.getTitle()))
                    .replace("${ID}", safe(note.getId()))
                    .replace("${CREATED_AT}", note.getCreatedAt() != null ? note.getCreatedAt().format(FORMATTER) : "")
                    .replace("${MODIFIED_AT}", note.getModifiedAt() != null ? note.getModifiedAt().format(FORMATTER) : "")
                    .replace("${TEXT_CONTENT}", safe(note.getTextContent()))
                    .replace("${ORIGINAL_FILE_NAME}", safe(note.getOriginalFileName()))
                    .replace("${CONTENT_TYPE}", safe(note.getContentType()))
                    .replace("${SIZE_BYTES}", String.valueOf(note.getSizeBytes()))
                    .replace("${TYPE}", safe(note.getType()));
        }

        Header contentLengthHeader = new Header("Content-Length", String.valueOf(htmlFileContents.getBytes(StandardCharsets.UTF_8).length));
        response.setHeaders(Stream.concat(Arrays.stream(headers), Stream.of(contentLengthHeader)).collect(Collectors.toSet()));
        response.setResponseBody(htmlFileContents.getBytes(StandardCharsets.UTF_8));

        return response;
    }

    private static String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }


}