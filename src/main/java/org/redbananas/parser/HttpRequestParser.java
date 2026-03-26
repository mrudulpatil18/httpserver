package org.redbananas.parser;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.redbananas.model.CommonHeaders;
import org.redbananas.model.Header;
import org.redbananas.model.HttpRequest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class HttpRequestParser {

    private final Character SPACE_DELIMITER = ' ';
    private final Character COLON_DELIMITER = ':';
    private final Character CR= '\r';
    private final Character LF = '\n';

    private List<Character> headerSection;
    private List<Character> bodySection;
    private List<Character> method;
    private List<Character> protocol;
    private List<Character> url;

    void splitHttpRequest(InputStream input) throws IOException {

        // parse method
        method = getMetaData(input, MetaData.METHOD);

        // parse url
        url = getMetaData(input, MetaData.URL);

        // parse version/protocol
        protocol = getMetaData(input, MetaData.PROTOCOL);

        // parse headers
        headerSection = getHeaders(input);

    }

    public HttpRequest parseHttpRequest(InputStream input) throws IOException {
        splitHttpRequest(input);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setMethod(method.stream().map(String::valueOf).collect(Collectors.joining()));
        httpRequest.setProtocol(protocol.stream().map(String::valueOf).collect(Collectors.joining()));
        httpRequest.setUrl(url.stream().map(String::valueOf).collect(Collectors.joining()));
        httpRequest.setHeaders(parseHeaders(headerSection));

//        if(httpRequest.getHeaders().stream().map(header -> {header.getHeaderName()}).anyMatch(""))
        // body
        bodySection = getBody(input);
        httpRequest.setRequestBody(bodySection.stream().map(String::valueOf).collect(Collectors.joining()));
        return httpRequest;
    }

    Set<Header> parseHeaders(List<Character> headerSection) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currString = new StringBuilder();

        for(Character c : headerSection) {
            if(c.equals(COLON_DELIMITER)) {
                tokens.add(currString.toString());
                currString.setLength(0);
            }else if(c.equals(LF) && !currString.isEmpty() && currString.charAt(currString.length() - 1) == CR) {
                tokens.add(currString.substring(0, currString.length()-1));
                currString.setLength(0);
            }else{
                currString.append(c);
            }
        }
        if(!currString.isEmpty()){
            tokens.add(currString.toString());
        }

        if(tokens.size() % 2 == 1){
            throw new RuntimeException("invalid headers");
        }

        Set<Header> headers = new LinkedHashSet<>();
        for(int i = 0; i < tokens.size(); i+=2){
            headers.add(new Header(tokens.get(i).trim(), tokens.get(i+1).trim()));
        }

        return headers;
    }



    List<Character> getMetaData(InputStream input, MetaData metaDataType) throws IOException {
        List<Character> chars = new ArrayList<>();
        char c;

        while (true) {
            c = (char) input.read();
            if (c == SPACE_DELIMITER && metaDataType != MetaData.PROTOCOL) {
                return chars;
            }else if(c == LF && chars.getLast() == CR && metaDataType == MetaData.PROTOCOL) {
                return chars.subList(0, chars.size()-1);
            }
            chars.add(c);
        }
    }

    List<Character> getHeaders(InputStream input) throws IOException {
        List<Character> chars = new ArrayList<>();
        char c;

        while (true) {
            c = (char) input.read();
            if(c == LF && chars.size() >= 3 && chars.subList(chars.size()-3, chars.size()).toString().equals(String.valueOf(List.of(CR, LF, CR)))) {
                return chars.subList(0, chars.size()-3);
            }
            chars.add(c);
        }
    }

    List<Character> getBody(InputStream input) throws IOException {
        List<Character> chars = new ArrayList<>();
        char c;

        while (true) {
            c = (char) input.read();
            if(c == (char) -1) {
                return chars;
            }
            chars.add(c);
        }
    }

   enum MetaData {
        METHOD,
        PROTOCOL,
        URL
    }




    public HttpRequest parseHttpRequest0(InputStream input) throws IOException {

        HttpRequest httpRequest = new HttpRequest();
        List<Character> chars = new ArrayList<>();
//        System.out.println(Arrays.toString(request.readAllBytes()));
        List<String> tokens = new ArrayList<>();
        StringBuilder currString = new StringBuilder();
        while(true){
            Character c = (char) input.read();
            chars.add(c);
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
                    chars.add(c1);
                    if(!currString.isEmpty()){
                        tokens.add(currString.toString());
                        currString.setLength(0);
                    }
                }
            }
            else if(chars.size() > 4 && chars.get(chars.size()-4).equals(CR) && chars.get(chars.size()-3).equals(LF) && chars.get(chars.size()-2).equals(CR) && chars.get(chars.size()-1).equals(LF)){
                break;
            }
            else{
                currString.append(c);
            }
        }

        System.out.println("TOKENS: \n" + tokens);
        
        ListIterator<String> i = tokens.listIterator();

        httpRequest.setMethod(i.next());
        httpRequest.setUrl(i.next());
        httpRequest.setProtocol(i.next());
        httpRequest.setHeaders(new HashSet<Header>());

        while(true){
            if(i.nextIndex() == tokens.size() -1){
                break;
            }

            Header h = new Header();
            h.setHeaderName(i.next());
            h.setHeaderValue(i.next());
            httpRequest.getHeaders().add(h);
        }
        if(i.hasNext()){
            httpRequest.setRequestBody(i.next());
        }

        return httpRequest;
    }



    public void unparseHttpRequest(OutputStream outputStream, HttpRequest request) throws IOException {
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(request.getMethod());
        writer.write(SPACE_DELIMITER);
        writer.write(request.getUrl());
        writer.write(SPACE_DELIMITER);
        writer.write(request.getProtocol());
        writer.write(CR);
        writer.write(LF);

        for(Header header : request.getHeaders()){
            writer.write(header.getHeaderName());
            writer.write(COLON_DELIMITER);
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

