package org.redbananas.service;

import org.redbananas.model.*;
import org.redbananas.parser.MultipartBodyParser;
import org.redbananas.util.StaticFileUtil;

import javax.swing.*;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

public class RequestRouter {

    DbService dbService;

    public RequestRouter() {
        this.dbService = new DbService();
    }

    private String fixPath(HttpRequest request) {
        String reqPath = request.getUrl();
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
        return reqPath;
    }


    public HttpResponse route(HttpRequest request) throws IOException {

        if (request.getMethod().equals("GET")) {
            String filePath = fixPath(request);
            Header[] headers = new Header[]{getContentTypeHeader(filePath)};
            return StaticFileUtil.generateResponse(request, filePath, headers);
        } else if (request.getMethod().equals("POST")) {
            if(request.getUrl().equals("/note")) {
                return handleAddNote(request);
            }else if(request.getUrl().equals("/upload")) {
                return handleUploadNote(request);
            }
        }
        HttpResponse res = new HttpResponse();
        res.setProtocol("HTTP/1.1");
        res.setResponseCode(303);
        res.setReasonPhrase("See Other");
        res.setHeaders(Set.of(new Header("Location", "/page1"), new Header("Content-Length", "0")));
        res.setResponseBody("randomValue".getBytes());
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
        note.setFilePath("data/" + note.getId() + "." + extension );
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
        res.setHeaders(Set.of(new Header("Location", "/"), new Header("Content-Length", "0")));
        res.setResponseBody(note.getBytes());
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


// TODO: get and show notes on homepage
// TODO: /get/item/:id
// TODO: handle edge cases in body parsing ( meh meh )