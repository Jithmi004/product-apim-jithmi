package org.wso2.am.integration.backend.service;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;


public class SimpleHTTPServer {

//    public static void main(String[] args) {
//        // Create a new thread to run the server
//        Thread serverThread = new Thread(() -> {
//            try {
//                // Initialize the server to listen on port 8100
//                HttpServer server = HttpServer.create(new InetSocketAddress(8100), 0);
//                server.createContext("/", new MyHandler());
//                server.setExecutor(null); // creates a default executor
//                server.start();
//                System.out.println("Server is listening on port 8100...");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//
//        // Start the server thread
//        serverThread.start();
//
//        // Main thread can continue executing other tasks
//        System.out.println("Main thread is free to do other things...");
//    }

    // Define the handler that processes incoming HTTP requests
    public static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Respond with status code 200 for any request method
            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            os.close();
        }
    }
}

