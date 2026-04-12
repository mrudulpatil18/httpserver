package org.redbananas.parser;

import lombok.NoArgsConstructor;
import org.redbananas.model.Header;
import org.redbananas.model.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;

@NoArgsConstructor
public class HttpResponseDispatcher {
    private final Character SPACE_DELIMITER = ' ';
    private final Character COLON_DELIMITER = ':';
    private final Character CR= '\r';
    private final Character LF = '\n';

    public void sendResponse(OutputStream outputStream, HttpResponse res) throws IOException {
            outputStream.write((res.getProtocol() + SPACE_DELIMITER + res.getResponseCode() + SPACE_DELIMITER + res.getReasonPhrase() + CR + LF).getBytes());
            for(Header header : res.getHeaders()){
                outputStream.write((header.getHeaderName() + COLON_DELIMITER + SPACE_DELIMITER + header.getHeaderValue() + CR + LF).getBytes());
            }
            outputStream.write((""+ CR + LF).getBytes());
            if(res.getResponseBody() != null){
                outputStream.write(res.getResponseBody());
            }
            outputStream.flush();

    }
}
