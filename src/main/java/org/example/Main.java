package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Intiatate Echo Client/Server (c/s) ?");
        if(sc.nextLine().equals("c")){
            setupEchoClient(12345);
        }else{
            setupEchoServer(12345);
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
                    out.println(new StringBuilder(input).reverse().toString());
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
