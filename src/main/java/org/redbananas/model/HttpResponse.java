package org.redbananas.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class HttpResponse {
    String protocol;
    Integer responseCode;
    String reasonPhrase;

    Set<Header> headers;

    byte[] responseBody;

}
