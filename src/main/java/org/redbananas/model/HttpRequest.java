package org.redbananas.model;

import lombok.Data;

import java.util.Set;

@Data
public class HttpRequest {
    String method;
    String url;
    String httpVersion;

    Set<Header> headers;

    String requestBody;
}
