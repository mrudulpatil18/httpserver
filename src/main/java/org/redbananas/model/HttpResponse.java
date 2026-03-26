package org.redbananas.model;

import lombok.Data;

import java.util.Set;

@Data
public class HttpResponse {
    String protocol;
    Integer responseCode;
    String reasonPhrase;

    Set<Header> headers;

    String responseBody;

}
