package org.wso2.am.integration.backend.service;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SSLServerSendImmediateResponse extends AbstractSSLServer{

    public void run(int port, String content, String statusCode, String location) throws Exception {
        try {
            // Create a ServerSocket to listen on that port.
            System.setProperty("javax.net.ssl.keyStore",location);
            System.setProperty("javax.net.ssl.keyStorePassword", "wso2carbon");
            ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
            ss = ssf.createServerSocket(port);
            System.out.println("SSL Server Started!");

            do {
                Socket client = ss.accept();
                System.out.println(" Accepting the client connection ...");
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream());

                // Start sending our reply, using the HTTP 1.1 protocol
                System.out.println(" Sending immediate response ( size:"+ content.getBytes().length +") without reading the request");
                out.print("HTTP/1.1 " + statusCode + " OK\r\n"); // Version & status code
                out.print("Access-Control-Expose-Headers:\r\n");
                out.print("Access-Control-Allow-Origin: *\r\n");
                out.print("X-Correlation-ID: 9f22c69b-6673-4326-8aff-0c0c097cd3c0\r\n");
                out.print(
                        "Access-Control-Allow-Headers: authorization,Access-Control-Allow-Origin,Content-Type," +
                                "SOAPAction,apikey,testKey,Authorization\r\n");
                out.print("Content-Type: application/json\r\n");
                out.print("Date: Tue, 14 Dec 2021 08:15:17 GMT\r\n");
                out.print("Content-Length:  " + content.getBytes().length + "\r\n");
                out.print("Connection: Close\r\n");
                out.print("\r\n");
                out.print(content + "\r\n");
                out.flush();
                System.out.println(" Closing server connection ....");
                out.close();
                client.close();
            } while (true);
        } catch (Exception e) {
            System.err.println("Error" + e);
        }
    }
}
