package org.redbananas.model;

import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Data
public class HttpRequest {
    String method;
    String url;
    String protocol;

    Set<Header> headers;

    byte[] requestBody;

    public String log() {
        StringBuilder sb = new StringBuilder();

        // Request line
        sb.append(method)
                .append(" ")
                .append(url)
                .append(" ")
                .append(protocol)
                .append("\r\n");

        // Headers
        if (headers != null) {
            for (Header h : headers) {
                sb.append(h.getHeaderName())
                        .append(": ")
                        .append(h.getHeaderValue())
                        .append("\r\n");
            }
        }

        sb.append("\r\n"); // header-body separator

        // Body (readable)
        if (requestBody != null && requestBody.length > 0) {
            sb.append(new String(requestBody, StandardCharsets.UTF_8));
        }

        return sb.toString();
    }
}

// structure of req for ref of HTTP1.1
//  | METHOD | SP | URL | SP | VERSION | CRLF |
//  | HEADER_NAME ":" SP HEADER_VALUE | CRLF |
//  ..........................................
//  | HEADER_NAME ":" SP HEADER_VALUE | CRLF |
//  | CRLF |
//  | MESSAGE_BODY |



// \s : 32
// \r : 13
// \n : 10
