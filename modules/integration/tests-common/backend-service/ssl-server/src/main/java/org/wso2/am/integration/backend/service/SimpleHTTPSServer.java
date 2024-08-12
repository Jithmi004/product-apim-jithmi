package org.wso2.am.integration.backend.service;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;

public class SimpleHTTPSServer {

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

