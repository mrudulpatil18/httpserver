package org.redbananas.router;

import lombok.NoArgsConstructor;
import org.redbananas.model.Header;
import org.redbananas.util.StaticFileUtil;
import org.redbananas.model.HttpRequest;
import org.redbananas.model.HttpResponse;

@NoArgsConstructor
public class RequestRouter {

    private String fixPath(HttpRequest request){
        String reqPath = request.getUrl();
        if(reqPath.equals("/")){
            reqPath = reqPath + "index";
        }
        reqPath = "static/" + reqPath.substring(1);

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
        return null;
    }

    public HttpResponse route(HttpRequest request){
        String filePath = fixPath(request);
        return StaticFileUtil.generateResponse(request, filePath);
    };

}
