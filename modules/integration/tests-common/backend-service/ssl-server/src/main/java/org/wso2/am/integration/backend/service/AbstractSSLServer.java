package org.wso2.am.integration.backend.service;

import java.io.IOException;
import java.net.ServerSocket;

public class AbstractSSLServer {

    public ServerSocket ss;

    public AbstractSSLServer() {
        System.out.println("initiating "+ this.getClass().getSimpleName() +" server ");
    }

    public boolean isserverdone = true;

    public void run(int port, String content,String location) throws Exception {}

    public void shutdownServer() throws InterruptedException {
        try {
            while (!isserverdone){
                Thread.sleep(10);
            }
            System.out.println("Shutting down the "+ this.getClass().getSimpleName() +" server");
            ss.close();
        } catch (IOException e) {
            System.out.println("Error while shutting down the server ");
        }
    }
}
