package org.redbananas;

import org.redbananas.model.HttpRequest;
import org.redbananas.model.HttpResponse;
import org.redbananas.parser.HttpRequestParser;
import org.redbananas.parser.HttpResponseDispatcher;
import org.redbananas.router.RequestRouter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final RequestRouter router = new RequestRouter();

    public static void main(String[] args)  {
        setupHttpServer(12345);
    }

    public static void setupHttpServer(int port) {
        try(ServerSocket sc = new ServerSocket(port)){
            System.out.println("Listening for clients.....");
            Socket socket = sc.accept();
            System.out.println("Connection Established: ");
            printSocketDetails(socket);
            try(OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream()
            ){
                HttpRequestParser httpRequestParser = new HttpRequestParser();
                HttpRequest request = httpRequestParser.parseHttpRequest(in);
                System.out.println(request);
                HttpResponse response = router.route(request);
                System.out.println(response);
                HttpResponseDispatcher httpResponseDispatcher = new HttpResponseDispatcher();
                httpResponseDispatcher.sendResponse(out, response);
            }
            catch (IOException e){
                throw new RuntimeException(e);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void printSocketDetails(Socket s){
        System.out.println("InetAddress: " + s.getInetAddress());
        System.out.println("LocalPort: " + s.getLocalPort());
        System.out.println("LocalSocketAddress: " + s.getLocalSocketAddress());
        System.out.println("Port: " + s.getPort());
        System.out.println("----------------------------------------------------");
    }
}