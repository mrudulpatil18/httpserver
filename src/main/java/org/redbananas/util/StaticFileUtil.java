package org.redbananas.util;

import org.redbananas.model.Header;
import org.redbananas.model.HttpRequest;
import org.redbananas.model.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class StaticFileUtil {

    private static byte[] readFile(String serverPath){
        Path path = Paths.get(serverPath);
        try{
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpResponse generateResponse(HttpRequest request, String filePath){
        HttpResponse response = new HttpResponse();
        response.setProtocol(request.getProtocol());
        response.setResponseCode(200);
        response.setReasonPhrase("OK");
        Header contentTypeHeader = new Header("Content-Type", "text/html; charset=utf-8");
        byte[] file = readFile(filePath);
        Header contentLengthHeader = new Header("Content-Length", String.valueOf(file.length));
        response.setHeaders(Set.of(contentTypeHeader, contentLengthHeader));
        response.setResponseBody(file);

        return response;
    }

}