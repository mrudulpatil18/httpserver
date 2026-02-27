package org.redbananas;

import org.redbananas.model.Header;
import org.redbananas.model.HttpRequest;
import org.redbananas.parser.HttpRequestParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args)  {
//        Scanner sc = new Scanner(System.in);
//        System.out.println("Intiatate Echo Client/Server (c/s) ?");
//        if(sc.nextLine().equals("c")){
//            setupEchoClient(12345);
//        }else{
//            setupEchoServer(12345);
//        }

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = new HttpRequest();
        request.setMethod("GET");
        request.setUrl("/");
        request.setHttpVersion("HTTP1.1");
        Header header = new Header();
        header.setHeaderName("but_whyyyyy");
        header.setHeaderValue("lo_siento_wilson");
        request.setHeaders(Set.of(header));
        request.setRequestBody("LALALALALALLALALALALLAALLALALLALALA\n");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            parser.unparseHttpRequest(output, request);
        }catch (IOException e){
            System.out.println(e);
        }

        System.out.println("OUTPUT STREAM: \n" + output.toString());
        System.out.println("-------------------------------");

        InputStream input = new ByteArrayInputStream(output.toByteArray());
        try {
             HttpRequest req = parser.parseHttpRequest(input);
            System.out.println(req);
        } catch (IOException e) {
            System.out.println("e: "+ e);;
        }



    }

    public static void setupEchoClient(int port){
        try (Socket socket = new Socket("localhost", port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in)))
        {
            System.out.println("Connection Established");
            printSocketDetails(socket);
            String input;
            while((input = stdIn.readLine()) != null){
                out.println(input);
                System.out.println("Reversed echo: " + in.readLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setupEchoServer(int port) {
       try(ServerSocket sc = new ServerSocket(port)){
           System.out.println("Listening for clients.....");
           Socket socket = sc.accept();
           System.out.println("Connection Established: ");
           printSocketDetails(socket);
           try(PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
           ){
                String input;
                while((input = in.readLine()) != null){

                    System.out.printf("Client (%s):%s Message: %s\n", socket.getInetAddress(), socket.getLocalPort(), input);
                    out.println(new StringBuilder(input).reverse());
                }
           }catch (IOException e){
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
