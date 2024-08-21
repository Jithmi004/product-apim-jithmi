//package org.wso2.am.integration.backend.service;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//
//public class Test {
//
//    public static void main(String[] args) throws Exception {
//        String content = readFile("/Users/jithmir/Work/IntegrationTests/HTTPCoreScenarioTests/src/main/resources/2KB.json");
//        // This class contains main methods to run the back end servers separately
//
//        ////////////// Backend server list SSLServerSendImediatexxx /////////////
//
//        System.out.println(" >>>>>>>>>>>>>>>>>>>>>>>>>>> Start sslServerSendImmediate200 backend");
//        SSLServerSendImmediateResponse200 server = new SSLServerSendImmediateResponse200();
////        server.run(8100, content,"/Users/jithmir/Work/Product_APIM_fork_2/product-apim/modules/integration/tests-integration/tests-benchmark/target/carbontmp1723608646259/wso2am-4.4.0-SNAPSHOT/repository/resources/security/wso2carbon.jks");
//    }
//
//    public static String readFile(String fileLocation) throws IOException {
//        BufferedReader br = new BufferedReader(new FileReader(fileLocation));
//        try {
//            StringBuilder sb = new StringBuilder();
//            String line = br.readLine();
//
//            while (line != null) {
//                sb.append(line);
//                sb.append(System.lineSeparator());
//                line = br.readLine();
//            }
//            String everything = sb.toString();
//            return everything;
//        } finally {
//            br.close();
//        }
//    }
//}
