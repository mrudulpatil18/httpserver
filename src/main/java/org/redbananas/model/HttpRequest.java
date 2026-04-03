package org.redbananas.model;

import lombok.Data;

import java.util.Set;

@Data
public class HttpRequest {
    String method;
    String url;
    String protocol;

    Set<Header> headers;

    byte[] requestBody;
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
