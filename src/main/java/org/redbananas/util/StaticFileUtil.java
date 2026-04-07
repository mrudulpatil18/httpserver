package org.redbananas.util;

import org.redbananas.model.Header;
import org.redbananas.model.HttpRequest;
import org.redbananas.model.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
        if(!((new File(filePath)).exists())){
            byte[] file = readFile("static/404.html");
            Arrays.stream(headers).findFirst().ifPresent(header -> {
                if(header.getHeaderName().equals("Content-Type")){
                    header.setHeaderValue("text/html; charset=utf-8");
                }
            });
        }
        byte[] file = readFile(filePath);
        Header contentLengthHeader = new Header("Content-Length", String.valueOf(file.length));
        response.setHeaders(Stream.concat(Arrays.stream(headers), Stream.of(contentLengthHeader)).collect(Collectors.toSet()));
        response.setResponseBody(file);

        return response;
    }

}