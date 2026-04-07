package org.redbananas.parser;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.redbananas.model.Header;
import org.redbananas.model.HttpRequest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Data
@NoArgsConstructor
public class HttpRequestParser {

    private final Character SPACE_DELIMITER = ' ';
    private final Character COLON_DELIMITER = ':';
    private final Character CR= '\r';
    private final Character LF = '\n';


    public HttpRequest parseHttpRequest(InputStream inputStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        HttpRequest httpRequest = new HttpRequest();

        String currLine = readLine(bis);
        String[] arr = currLine.split(String.valueOf(SPACE_DELIMITER));
        httpRequest.setMethod(arr[0]);
        httpRequest.setUrl(arr[1]);
        httpRequest.setProtocol(arr[2]);

        Set<Header> headers = new HashSet<>();
        while (true) {
            currLine = readLine(bis);
            if(currLine.isEmpty()) break;

            arr = currLine.split(String.valueOf(COLON_DELIMITER));
            headers.add(new Header(arr[0].trim(), arr[1].trim()));
        }
        httpRequest.setHeaders(headers);

        Optional<Header> contentLengthHeader =
                headers.stream()
                        .filter(h -> h.getHeaderName().equalsIgnoreCase("Content-Length"))
                        .findFirst();

        if(contentLengthHeader.isPresent()) {
            int length = Integer.parseInt(contentLengthHeader.get().getHeaderValue());
            byte[] body = bis.readNBytes(length);
            httpRequest.setRequestBody(body);
        }
        return httpRequest;

    }

    private String readLine(BufferedInputStream bis) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = (char) bis.read();
            if(c == CR) {
                continue;
            }if(c == LF) {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }


    public void unparseHttpRequest(OutputStream outputStream, HttpRequest request) throws IOException {
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(request.getMethod());
        writer.write(SPACE_DELIMITER);
        writer.write(request.getUrl());
        writer.write(SPACE_DELIMITER);
        writer.write(request.getProtocol());
        writer.write(CR);
        writer.write(LF);

        for(Header header : request.getHeaders()){
            writer.write(header.getHeaderName());
            writer.write(COLON_DELIMITER);
            writer.write(SPACE_DELIMITER);
            writer.write(header.getHeaderValue());
            writer.write(CR);
            writer.write(LF);
        }
        writer.write(CR);
        writer.write(LF);

        writer.write(request.getRequestBody().toString());

        writer.flush();
    }
}

