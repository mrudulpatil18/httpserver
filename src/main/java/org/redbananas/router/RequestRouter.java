package org.redbananas.router;

import lombok.NoArgsConstructor;
import org.redbananas.model.Header;
import org.redbananas.util.StaticFileUtil;
import org.redbananas.model.HttpRequest;
import org.redbananas.model.HttpResponse;

import java.io.IOException;
import java.util.Set;

@NoArgsConstructor
public class RequestRouter {

    private String fixPath(HttpRequest request){
        String reqPath = request.getUrl();
        if(reqPath.equals("/")) {
            reqPath = reqPath + "index";
        }else if(reqPath.endsWith(".css")) {
            reqPath = "/css" + reqPath;
        }else if(reqPath.endsWith(".js")) {
            reqPath = "/js" + reqPath;
        }
        reqPath = "static" + reqPath;

        if(!reqPath.contains(".")){
            reqPath = reqPath + ".html";
        }
        return reqPath;
    }

    private Header getContentTypeHeader(String filePath){
//        text/html for HTML documents.
//        text/plain for plain text.
//        text/css for Cascading Style Sheets.
//        text/javascript for JavaScript files.
//        text/markdown for Markdown files.
//        application/octet-stream for binary files where user action is expected.

        String fileExtension = filePath.substring(filePath.lastIndexOf("."));
        return switch (fileExtension) {
            case ".html" -> new Header("Content-Type", "text/html; charset=utf-8");
            case ".css" -> new Header("Content-Type", "text/css;charset=utf-8");
            case ".js" -> new Header("Content-Type", "application/javascript;charset=utf-8");
            default -> new Header("Content-Type", "text/plain; charset=utf-8");
        };
    }

    public HttpResponse route(HttpRequest request) throws IOException {

        if(request.getMethod().equals("GET")) {
            String filePath = fixPath(request);
            Header[] headers = new Header[]{getContentTypeHeader(filePath)};
            return StaticFileUtil.generateResponse(request, filePath, headers);
        }
        else if(request.getMethod().equals("POST")) {
            // TODO: handle POST Reqs, potentially use and add sqlite
            HttpResponse res = new HttpResponse();
            res.setProtocol("HTTP/1.1");
            res.setResponseCode(303);
            res.setReasonPhrase("See Other");
            res.setHeaders(Set.of(new Header("Location", "/page1"), new Header("Content-Length", "0")));
            res.setResponseBody("randomValue".getBytes());
            return res;
        }
        return new HttpResponse();
    }

}
