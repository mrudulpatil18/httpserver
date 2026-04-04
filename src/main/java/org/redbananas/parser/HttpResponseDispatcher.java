package org.redbananas.parser;

import lombok.NoArgsConstructor;
import org.redbananas.model.Header;
import org.redbananas.model.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

@NoArgsConstructor
public class HttpResponseDispatcher {
    private final Character SPACE_DELIMITER = ' ';
    private final Character COLON_DELIMITER = ':';
    private final Character CR= '\r';
    private final Character LF = '\n';

    public void sendResponse(OutputStream outputStream, HttpResponse res) {
        PrintWriter out = new PrintWriter(outputStream, false);
//        out.println("HTTP/1.1 200 OK");
        out.print(res.getProtocol() + SPACE_DELIMITER + res.getResponseCode() + SPACE_DELIMITER + res.getReasonPhrase() + CR + LF);
        for(Header header : res.getHeaders()){
            out.print(header.getHeaderName() + COLON_DELIMITER + SPACE_DELIMITER + header.getHeaderValue() + CR + LF);
        }
        out.print("Content-Length: " + res.getResponseBody().length + CR + LF);
        out.print(CR + LF);
        out.flush();
        try {
            outputStream.write(res.getResponseBody());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
