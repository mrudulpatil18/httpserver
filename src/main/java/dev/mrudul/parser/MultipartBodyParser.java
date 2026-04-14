package dev.mrudul.parser;

import dev.mrudul.model.FormDataBody;
import dev.mrudul.model.Header;
import dev.mrudul.model.HttpRequest;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class MultipartBodyParser {

    HttpRequest request;
    String boundary;
    byte[] body;

    public MultipartBodyParser(HttpRequest request) {
        this.request = request;
        this.boundary = getRequestBodyBoundary(request);
        this.body = request.getRequestBody();
    }

    public List<FormDataBody> parse() {
        List<FormDataBody> result = new ArrayList<>();
        List<byte[]> parts = splitBodyData();

        for(byte[] part : parts) {
            FormDataBody formDataBody = parsePart(part);
            if(formDataBody != null) {
                result.add(formDataBody);
            }
        }
        return result;
    }

    private FormDataBody parsePart(byte[] part) {
        int split = indexOf(part, "\r\n\r\n".getBytes(StandardCharsets.UTF_8), 0);
        if(split == -1) {return null;}

        byte[] headerBytes = Arrays.copyOfRange(part, 0, split);
        byte[] bodyBytes = Arrays.copyOfRange(part, split + 4, part.length);

        Set<Header> headers = parseHeaders(headerBytes);
        FormDataBody formDataBody = new FormDataBody();
        formDataBody.setHeaders(headers);
        formDataBody.setBody(bodyBytes);
        return formDataBody;

    }

    private Set<Header> parseHeaders(byte[] headerBytes) {
        Set<Header> headers = new HashSet<>();
        String headerText = new String(headerBytes, StandardCharsets.UTF_8);
        String[] liens = headerText.split("\r\n");
        for (String lien : liens) {
            int i  = lien.indexOf(':');
            if(i  == -1) continue;

            String name = lien.substring(0, i).trim();
            String value = lien.substring(i + 1).trim();
            headers.add(new Header(name, value));
        }
        return headers;
    }



    private List<byte[]> splitBodyData() {
        List<byte[]> parts = new ArrayList<>();
        if (body == null || boundary == null) return parts;

        byte[] first = firstBoundary();
        byte[] middle = middleBoundary();
        byte[] last = finalBoundary();

        int start = indexOf(body, first, 0);
        if (start == -1) return parts;

        start += first.length;

        while (true) {
            int nextMiddle = indexOf(body, middle, start);
            int nextLast = indexOf(body, last, start);

            if (nextLast != -1 && (nextMiddle == -1 || nextLast < nextMiddle)) {
                parts.add(Arrays.copyOfRange(body, start, nextLast));
                break;
            }

            if (nextMiddle == -1) break;

            parts.add(Arrays.copyOfRange(body, start, nextMiddle));
            start = nextMiddle + middle.length;
        }

        return parts;
    }

    private int indexOf(byte[] outer, byte[] inner, int from) {
        outerloop:
        for (int i = from; i <= outer.length - inner.length; i++) {
            for (int j = 0; j < inner.length; j++) {
                if (outer[i + j] != inner[j]) {
                    continue outerloop;
                }
            }
            return i;
        }
        return -1;
    }

    private String getRequestBodyBoundary(HttpRequest request) {
        for (Header h : request.getHeaders()) {
            if ("Content-Type".equalsIgnoreCase(h.getHeaderName())) {
                String v = h.getHeaderValue();
                int i = v.indexOf("boundary=");
                if (i == -1) return null;
                String b = v.substring(i + 9).trim();
                if (b.startsWith("\"") && b.endsWith("\"")) {
                    b = b.substring(1, b.length() - 1);
                }
                return b;
            }
        }
        return null;
    }

    private byte[] firstBoundary() {
        return ("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);
    }

    private byte[] middleBoundary() {
        return ("\r\n--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);
    }

    private byte[] finalBoundary() {
        return ("\r\n--" + boundary + "--").getBytes(StandardCharsets.UTF_8);
    }

}
