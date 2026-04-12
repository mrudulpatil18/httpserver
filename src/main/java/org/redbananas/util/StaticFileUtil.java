package org.redbananas.util;

import org.redbananas.engine.TemplateUtils;
import org.redbananas.model.Header;
import org.redbananas.model.HttpRequest;
import org.redbananas.model.HttpResponse;
import org.redbananas.model.Note;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StaticFileUtil {

    private static byte[] readFile(String serverPath) throws IOException {
        try{
        Path path = Paths.get(serverPath);
        return Files.readAllBytes(path);
        } catch(Exception e){
            System.out.println("Error reading file " + serverPath + ": " + e.getMessage());
            return Files.readAllBytes(Paths.get("static/404.html"));
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
        // TODO:
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
                    "        The page you are looking for does not exist.\n" +
                    "    </div>");
        }else{
            htmlFileContents = htmlFileContents.replace("${NOTES_PLACEHOLDER}", notesHtml);
        }
        response.setResponseBody(htmlFileContents.getBytes());
        Header contentLengthHeader = new Header("Content-Length", String.valueOf(response.getResponseBody().length));
        response.setHeaders(Stream.concat(Arrays.stream(headers), Stream.of(contentLengthHeader)).collect(Collectors.toSet()));

        return response;
    }

    public static HttpResponse generateNotePage(HttpRequest request, String filePath, Header[] headers, Note note) throws IOException {
        // TODO:
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


}