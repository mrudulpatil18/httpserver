package org.redbananas.parser;

import org.redbananas.model.Header;
import org.redbananas.model.HttpRequest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HttpRequestParser {

    private final Character SPACE_DELIMITER = ' ';
    private final Character CR= '\r';
    private final Character LF = '\n';

    public HttpRequest parseHttpRequest(InputStream input) throws IOException {

        HttpRequest httpRequest = new HttpRequest();

//        System.out.println(Arrays.toString(request.readAllBytes()));
        List<String> tokens = new ArrayList<>();
        StringBuilder currString = new StringBuilder();
        while(true){
            Character c = (char) input.read();
            if(c.equals((char)-1)){
                if(!currString.isEmpty()){
                    tokens.add(currString.toString());
                    currString.setLength(0);
                }
                break;
            }
            else if(c.equals(SPACE_DELIMITER)){
                if(!currString.isEmpty()){
                    tokens.add(currString.toString());
                    currString.setLength(0);
                }
            }else if (c.equals(CR)){
                Character c1 = (char) input.read();
                if(c1.equals(LF)){
                    if(!currString.isEmpty()){
                        tokens.add(currString.toString());
                        currString.setLength(0);
                    }
                }
            }
            else{
                currString.append(c);
            }
        }

        System.out.println("TOKENS: \n" + tokens);
        // convert tokens to http request

        return httpRequest;
    }


    public void unparseHttpRequest(OutputStream outputStream, HttpRequest request) throws IOException {
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(request.getMethod());
        writer.write(SPACE_DELIMITER);
        writer.write(request.getUrl());
        writer.write(SPACE_DELIMITER);
        writer.write(request.getHttpVersion());
        writer.write(CR);
        writer.write(LF);

        for(Header header : request.getHeaders()){
            writer.write(header.getHeaderName());
            writer.write(SPACE_DELIMITER);
            writer.write(header.getHeaderValue());
            writer.write(CR);
            writer.write(LF);
        }
        writer.write(CR);
        writer.write(LF);

        writer.write(request.getRequestBody());

        writer.flush();
    }
}


// structure of req for ref of HTTP1.1
//  | METHOD | \s | URL | \s | VERSION | \r\n |
//  | header_name | \s | header_value | \r\n |
//  ..........................................
//  | header_name | \s | header_value | \r\n |
//  | \r\n |
//  | RequestBody |

// \s : 32
// \r : 13
// \n : 10