package dev.mrudul.model;

import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static dev.mrudul.service.RequestRouter.BASE_PATH;

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

    public String getUrl(){
        String[] parts = url.split("\\?", 2);
        String path = parts[0];
        String query = parts.length > 1 ? "?" + parts[1] : "";
        if (path.startsWith(BASE_PATH)) {
            path = path.substring(BASE_PATH.length());
            if (path.isEmpty()) path = "/";
        }
        return path + query;
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
