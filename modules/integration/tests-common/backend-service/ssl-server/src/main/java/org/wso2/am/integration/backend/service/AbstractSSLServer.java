package org.wso2.am.integration.backend.service;

import java.io.IOException;
import java.net.ServerSocket;

public class AbstractSSLServer {
    public String ServerkeyStoreLocation = "/Users/jithmir/Work/Product_APIM_fork_2/product-apim/modules/integration/tests-integration/tests-benchmark/target/carbontmp1723009938405/wso2am-4.4.0-SNAPSHOT/repository/resources/security/wso2carbon.jks";//ScenarioTests.ServerKeyStorePath;
    public static String ServerkeyStorePassword = "wso2carbon";//ScenarioTests.ServerKeyStorePassword;
    public ServerSocket ss;
    public static final String CRLF = "\r\n";
    public boolean isserverdone = true;

    public void run(int port, String content, String statusCode, String location) throws Exception {}

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
