package org.redbananas.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Data
@NoArgsConstructor
public class HttpResponse {
    String protocol;
    Integer responseCode;
    String reasonPhrase;

    Set<Header> headers;

    byte[] responseBody;

    public String log() {
        StringBuilder sb = new StringBuilder();

        // Status line
        sb.append(protocol)
                .append(" ")
                .append(responseCode)
                .append(" ")
                .append(reasonPhrase)
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
        if (responseBody != null && responseBody.length > 0) {
            sb.append(new String(responseBody, StandardCharsets.UTF_8));
        }

        return sb.toString();
    }

}
