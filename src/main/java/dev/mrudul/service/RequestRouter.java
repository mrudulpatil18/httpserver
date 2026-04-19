package dev.mrudul.service;

import dev.mrudul.model.*;
import dev.mrudul.parser.MultipartBodyParser;
import dev.mrudul.util.ResourceLoaderUtil;
import dev.mrudul.util.StaticFileUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestRouter {

    DbService dbService;
    public static String BASE_PATH = "";

    public RequestRouter() {
        this.dbService = new DbService();
    }

    private String fixPath(HttpRequest request) {
        String reqPath = request.getUrl();
        System.out.println("TRYING TO READ: " + reqPath);

        if (reqPath.equals("/")) {
            reqPath = reqPath + "index";
        } else if (reqPath.endsWith(".css")) {
            reqPath = "/css" + reqPath;
        } else if (reqPath.endsWith(".js")) {
            reqPath = "/js" + reqPath;
        }

        reqPath = "static" + reqPath;

        if (!reqPath.contains(".")) {
            reqPath = reqPath + ".html";
        }

        if(!reqPath.contains(".html")) {
            reqPath = reqPath.replace("/note", "");
            reqPath = reqPath.replace("/edit", "");
        }


        try {
            ResourceLoaderUtil.readBytes(reqPath);
            return reqPath;
        } catch (Exception e) {
            if (reqPath.startsWith("static/note/") || reqPath.startsWith("static/edit")) {
                return reqPath;
            }
            return "static/404.html";
        }
    }


    public HttpResponse route(HttpRequest request) throws IOException {

        if (request.getMethod().equals("GET")) {
            if(request.getUrl().startsWith("/download")){
                return handleDownload(request);
            }
            String filePath = fixPath(request);
            Header[] headers = new Header[]{getContentTypeHeader(filePath)};

            if(filePath.equals("static/index.html")){
                List<Note> notes = dbService.getAllNotes();
                return StaticFileUtil.generateHomePage(request, filePath, headers, notes);
            }else if(filePath.startsWith("static/note/")){
                Note note = dbService.getNote(filePath.replace("static/note/", "").replace(".html", ""));
                return StaticFileUtil.generateNotePage(request, headers, note);
            }else if(filePath.startsWith("static/edit")){
                Note note = dbService.getNote(filePath.replace("static/edit/", "").replace(".html", ""));
                return StaticFileUtil.generateEditPage(request, headers, note);
            }

            return StaticFileUtil.generateResponse(request, filePath, headers);
        } else if (request.getMethod().equals("POST")) {
            if(request.getUrl().equals("/note")) {
                return handleAddNote(request);
            }else if(request.getUrl().equals("/upload")) {
                return handleUploadNote(request);
            }else if(request.getUrl().startsWith("/edit")){
                return handleEditNote(request);
            }
        }else if(request.getMethod().equals("DELETE")) {
            if (request.getUrl().startsWith("/api/note/")) {
                return handleDeleteNote(request);
            }
        }
        HttpResponse res = new HttpResponse();
        res.setProtocol("HTTP/1.1");
        res.setResponseCode(303);
        res.setReasonPhrase("See Other");
        res.setHeaders(Set.of(new Header("Location", BASE_PATH + "/"), new Header("Content-Length", "0")));
        res.setResponseBody("randomValue".getBytes());
        return res;
    }

    private HttpResponse handleEditNote(HttpRequest request) {
        try {
            String url = request.getUrl();                // "/edit/abc-123"
            String noteId = url.replaceFirst("^/edit/", "");
            Note note = dbService.getNote(noteId);
            if (note == null) {
                return redirect404(noteId);
            }

            // Parse multipart form data (the edit form always uses multipart)
            MultipartBodyParser multipart = new MultipartBodyParser(request);
            var parts = multipart.parse();

            String newTitle = null;
            String newContent = null;
            byte[] newFileBytes = null;
            String newFileName = null;
            String newContentType = null;

            for (FormDataBody part : parts) {
                String fieldName = getFieldName(part.getHeaders());
                if ("title".equals(fieldName)) {
                    newTitle = new String(part.getBody(), StandardCharsets.UTF_8);
                } else if ("content".equals(fieldName)) {
                    newContent = new String(part.getBody(), StandardCharsets.UTF_8);
                } else if ("file".equals(fieldName)) {
                    newFileName = getOriginalFileName(part.getHeaders());
                    newFileBytes = part.getBody();
                    newContentType = getContentType(part.getHeaders());
                }
            }

            // Apply title change (always present)
            if (newTitle != null && !newTitle.isBlank()) {
                note.setTitle(newTitle);
            }

            // Handle based on note type
            if (note.getType() == NoteType.FILE) {
                // If a new file was uploaded, replace the existing one
                if (newFileBytes != null && newFileBytes.length > 0) {
                    String extension = newFileName.substring(newFileName.lastIndexOf('.') + 1);
                    String newPath = "/var/lib/raw-notes/files/" + note.getId() + "." + extension;
                    dbService.saveFile(newFileBytes, newPath);
                    note.setFilePath(newPath);
                    note.setOriginalFileName(newFileName);
                    note.setSizeBytes(newFileBytes.length);
                    note.setContentType(newContentType);
                }
            } else {
                // For TEXT, SNIPPET, LINK: update the text content
                if (newContent != null) {
                    note.setTextContent(newContent);
                }
            }

            note.setModifiedAt(LocalDateTime.now());
            dbService.updateNote(note);

            // Redirect to the detail page
            HttpResponse res = new HttpResponse();
            res.setProtocol("HTTP/1.1");
            res.setResponseCode(303);
            res.setReasonPhrase("See Other");
            res.setHeaders(Set.of(
                    new Header("Location", BASE_PATH + "/note/" + noteId),
                    new Header("Content-Length", "0")
            ));
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return redirect404(null);
        }
    }

    private String getFieldName(Set<Header> headers) {
        for (Header header : headers) {
            if (header.getHeaderName().equalsIgnoreCase("Content-Disposition")) {
                String value = header.getHeaderValue();
                // Look for name="fieldName"
                Matcher m = Pattern.compile("name=\"([^\"]+)\"").matcher(value);
                if (m.find()) {
                    return m.group(1);
                }
            }
        }
        return null;
    }

    private HttpResponse handleDownload(HttpRequest request) {

        String noteId = getQueryParam(request.getUrl(), "id");
        Note note = dbService.getNote(noteId);
        byte[] fileContent;
        try {
            fileContent = dbService.readFile(note.getFilePath());
        }catch (Exception e){
            return redirect404(noteId);
        }

        HttpResponse res = new HttpResponse();
        res.setProtocol("HTTP/1.1");
        res.setResponseCode(200);
        res.setReasonPhrase("OK");

        Set<Header> resHeaders = new HashSet<>();
        resHeaders.add(new Header("Content-Type", note.getContentType()));
        resHeaders.add(new Header("Content-Length", String.valueOf(note.getSizeBytes())));
        resHeaders.add(new Header("Content-Disposition", "attachment; filename=\"" + note.getOriginalFileName() + "\""));
        res.setHeaders(resHeaders);


        res.setHeaders(resHeaders);
        res.setResponseBody(fileContent);

        return res;

    }

    private String getQueryParam(String url, String key){
        int i = url.indexOf("?");
        if(i == -1){return null;}

        String query = url.substring(i+1);
        for(String param : query.split("&")){
            String[] params = param.split("=");
            if(params.length == 2 && params[0].equals(key)){
                return params[1];
            }
        }
        return null;
    }

    private HttpResponse handleDeleteNote(HttpRequest request) {
        String noteId = request.getUrl().replace("/api/note/", "");
        dbService.deleteNote(noteId);
        HttpResponse res = new HttpResponse();
        res.setProtocol("HTTP/1.1");
        res.setResponseCode(204);
        res.setReasonPhrase("No Content");
        res.setHeaders(Set.of(new Header("Location", BASE_PATH + "/"), new Header("Content-Length", "0")));

        return res;
    }

    private HttpResponse handleAddNote(HttpRequest request) {
        var data = getFormData(request);
        Note note = generateTextNote(data);

        String noteId = dbService.addNote(note);

        return redirectHome(noteId);
    }

    private HttpResponse handleUploadNote(HttpRequest request) {
        MultipartBodyParser multipartBodyParser = new MultipartBodyParser(request);
        var contents = multipartBodyParser.parse();
        Note note = new Note();
        String extension = "";
        String filetype = "";
        byte[] fileBytes = new byte[0];
        for(FormDataBody part: contents) {
            String fileName = getOriginalFileName(part.getHeaders());
            if(fileName != null) {
                note.setOriginalFileName(fileName);
                extension = fileName.split("\\.")[1];
                fileBytes = part.getBody();
                note.setSizeBytes(fileBytes.length);
                filetype = getContentType(part.getHeaders());
                System.out.println("Uploading file: " + fileName);
            }

            if(isTitle(part.getHeaders())) {
                note.setTitle(new String(part.getBody(), StandardCharsets.UTF_8));
            }
        }


        note.setId(UUID.randomUUID().toString());
        note.setType(NoteType.FILE);
        note.setFilePath("/var/lib/raw-notes/files/" + note.getId() + "." + extension);
        note.setCreatedAt(LocalDateTime.now());
        note.setModifiedAt(note.getCreatedAt());
        note.setContentType(filetype);

        dbService.saveFile(fileBytes, note.getFilePath());

        String noteId = dbService.addNote(note);

        return redirectHome(noteId);
    }

    private HttpResponse redirectHome(String note){
        HttpResponse res = new HttpResponse();

        res.setProtocol("HTTP/1.1");
        res.setResponseCode(303);
        res.setReasonPhrase("See Other");
        res.setHeaders(Set.of(new Header("Location", BASE_PATH + "/"), new Header("Content-Length", "0")));
        res.setResponseBody(note.getBytes());
        return res;

    }

    private HttpResponse redirect404(String noteId) {
        HttpResponse res = new HttpResponse();

        res.setProtocol("HTTP/1.1");
        res.setResponseCode(303);
        res.setReasonPhrase("See Other");
        res.setHeaders(Set.of(new Header("Location", BASE_PATH + "/404"), new Header("Content-Length", "0")));
        res.setResponseBody(noteId != null ? noteId.getBytes(): "random".getBytes());
        return res;

    }

    private boolean isTitle(Set<Header> headers){
        for(Header header: headers){
            if(header.getHeaderName().equalsIgnoreCase("Content-Disposition")){
                if(header.getHeaderValue().contains("title")) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getOriginalFileName(Set<Header> headers) {
        for (Header header : headers) {
            if(header.getHeaderName().equalsIgnoreCase("Content-Disposition")) {
                String s = header.getHeaderValue();
                int i = s.indexOf("filename=");
                if(i==-1)return null;

                String fn = s.substring(i+9).trim();

                if(fn.startsWith("\"") && fn.endsWith("\"")){
                    fn = fn.substring(1, fn.length()-1);
                }

                return fn;
            }
        }
        return null;
    }

    private String getContentType(Set<Header> headers) {
        for (Header header : headers) {
            if(header.getHeaderName().equalsIgnoreCase("Content-Type")) {
                return header.getHeaderValue();
            }
        }
        return "";
    }


    Map<String, String> getFormData(HttpRequest request) {
        Map<String, String> formData = new HashMap<>();

        String body = new String(request.getRequestBody(), StandardCharsets.UTF_8);

        for (String pair : body.split("&")) {
            if (pair.isEmpty()) continue;
            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";
            formData.put(key, value);
        }
        return formData;
    }

    Note generateTextNote(Map<String, String> data) {
        Note note = new Note();
        note.setId(UUID.randomUUID().toString());
        note.setTextContent(data.get("content"));
        note.setTitle(data.get("title"));
        note.setType(NoteType.valueOf(data.get("type").toUpperCase()));
        note.setContentType("text/plain");

        note.setCreatedAt(LocalDateTime.now());
        note.setModifiedAt(note.getCreatedAt());
        return note;
    }



    private Header getContentTypeHeader(String filePath) {

        String fileExtension = filePath.substring(filePath.lastIndexOf("."));
        return switch (fileExtension) {
            case ".html" -> new Header("Content-Type", "text/html; charset=utf-8");
            case ".css" -> new Header("Content-Type", "text/css;charset=utf-8");
            case ".js" -> new Header("Content-Type", "application/javascript;charset=utf-8");
            default -> new Header("Content-Type", "text/plain; charset=utf-8");
        };
    }
}
