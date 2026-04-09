package org.redbananas.service;

import org.redbananas.model.*;
import org.redbananas.util.StaticFileUtil;

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

    private Header getContentTypeHeader(String filePath) {
        // text/html for HTML documents.
        // text/plain for plain text.
        // text/css for Cascading Style Sheets.
        // text/javascript for JavaScript files.
        // text/markdown for Markdown files.
        // application/octet-stream for binary files where user action is expected.

        String fileExtension = filePath.substring(filePath.lastIndexOf("."));
        return switch (fileExtension) {
            case ".html" -> new Header("Content-Type", "text/html; charset=utf-8");
            case ".css" -> new Header("Content-Type", "text/css;charset=utf-8");
            case ".js" -> new Header("Content-Type", "application/javascript;charset=utf-8");
            default -> new Header("Content-Type", "text/plain; charset=utf-8");
        };
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

        HttpResponse res = new HttpResponse();

        res.setProtocol("HTTP/1.1");
        res.setResponseCode(303);
        res.setReasonPhrase("See Other");
        res.setHeaders(Set.of(new Header("Location", "/"), new Header("Content-Length", "0")));
        res.setResponseBody(noteId.getBytes());
        return res;
    }

    private HttpResponse handleUploadNote(HttpRequest request) {
        return null;
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
}


// /post add-note - note, link, snippet - done
// TODO: /post upload-file
// TODO: get and show notes on homepage
// TODO: /get/item/:id