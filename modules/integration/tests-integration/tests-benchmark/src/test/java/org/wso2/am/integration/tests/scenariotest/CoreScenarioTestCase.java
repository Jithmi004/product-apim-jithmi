package org.wso2.am.integration.tests.scenariotest;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.ssl.SSLServer;
import org.compass.core.util.Assert;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.am.integration.backend.service.AbstractSSLServer;
import org.wso2.am.integration.clients.publisher.api.ApiException;
import org.wso2.am.integration.clients.store.api.v1.dto.ApplicationDTO;
import org.wso2.am.integration.clients.store.api.v1.dto.ApplicationKeyDTO;
import org.wso2.am.integration.clients.store.api.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.am.integration.test.utils.APIManagerIntegrationTestException;
import org.wso2.am.integration.test.utils.base.APIMIntegrationBaseTest;
import org.wso2.am.integration.test.utils.base.APIMIntegrationConstants;
import org.wso2.am.integration.test.utils.bean.APILifeCycleAction;
import org.wso2.am.integration.test.utils.bean.APIRequest;
import org.wso2.am.integration.test.utils.http.HTTPSClientUtils;
import org.wso2.am.integration.tests.benchmarktest.BenchmarkUtils;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.am.integration.backend.service.SSLServerSendImmediateResponse;
import org.wso2.am.integration.backend.service.SimpleHTTPServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
public class CoreScenarioTestCase extends APIMIntegrationBaseTest {
    protected static final String TIER_UNLIMITED = "Unlimited";
    private static final String JDBC_METRIC = "jdbc";
    private static final String EXTERNAL_API_METRIC = "http";
    private final String API_END_POINT_POSTFIX_URL = "am/sample/pizzashack/v1/api/menu";
    private final String API_VERSION_1_0_0 = "1.0.0";
    BenchmarkUtils benchmarkUtils = new BenchmarkUtils();
    List<String> idList = new ArrayList<String>();
    List<String> apiIdList = new ArrayList<>();
    private String apiUUID;
    private String applicationID;
    private String testName;
    private String context;
    private LocalTime startTime;
    private String providerName;
    private String apiEndPointUrl;
    private String scenario;
    String Content1MB;

    {
        try {
            Content1MB = readThisFile("/Users/jithmir/Work/IntegrationTests/HTTPCoreScenarioTests/src/main/resources/1MB.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String Content2KB;

    {
        try {
            Content2KB = readThisFile("/Users/jithmir/Work/IntegrationTests/HTTPCoreScenarioTests/src/main/resources/2KB.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Factory(dataProvider = "userModeDataProvider")
    public CoreScenarioTestCase(TestUserMode userMode) {

        this.userMode = userMode;
    }

    @DataProvider
    public static Object[][] userModeDataProvider() {

        return new Object[][]{
                new Object[]{TestUserMode.SUPER_TENANT_ADMIN},
//                new Object[]{TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init(userMode);
        apiEndPointUrl = gatewayUrlsWrk.getWebAppURLHttp() + API_END_POINT_POSTFIX_URL;
        providerName = user.getUserName();
    }

    //create an publish an api
//    @Test
//    public void createRestApi(Method method)
//            throws IOException, InterruptedException, ParseException, APIManagerIntegrationTestException, ApiException {
//
//        scenario = "API_CREATE";
//        testName = method.getName();
//        benchmarkUtils.setTenancy(userMode);
//        LocalTime startTime = benchmarkUtils.getCurrentTimeStampAndSetCorrelationID(testName);
//        apiUUID = createAnApi("NewAPI2", "sampleContext");
//        HttpResponse response = restAPIPublisher.changeAPILifeCycleStatus(apiUUID, APILifeCycleAction.PUBLISH.getAction(), null);
//        apiIdList.add(apiUUID);
//    }

    @Test
    public void invokeCreatedApi(Method method)
            throws InterruptedException, IOException, ParseException, APIManagerIntegrationTestException, ApiException,
            org.wso2.am.integration.clients.store.api.ApiException, XPathExpressionException, JSONException {

        scenario = "INVOKE_API";
        System.out.println("Before server start");
        String serverKeyStoreLocation = findServerKeyStoreLocation();

        //SSLServerSendImmediateResponse server = StartServer(new SSLServerSendImmediateResponse(), Content2KB, 8100, serverKeyStoreLocation);
        StartHTTPServer();
        System.out.println("After server start");
        ArrayList grantTypes = new ArrayList();
        Map<String, String> requestHeaders;
        testName = method.getName();
        benchmarkUtils.setTenancy(userMode);
        context = "context_" + testName;
        apiUUID = createAnApi(testName, context);
        apiIdList.add(apiUUID);
        grantTypes.add(APIMIntegrationConstants.GRANT_TYPE.CLIENT_CREDENTIAL);
        restAPIPublisher.changeAPILifeCycleStatus(apiUUID, APILifeCycleAction.PUBLISH.getAction(), null);
        createAPIRevisionAndDeployUsingRest(apiUUID, restAPIPublisher);
        waitForAPIDeployment();
        HttpResponse applicationResponse = restAPIStore.createApplication("Application_" + testName,
                "Test Application For Benchmark",
                APIMIntegrationConstants.APPLICATION_TIER.UNLIMITED,
                ApplicationDTO.TokenTypeEnum.JWT);
        applicationID = applicationResponse.getData();
        restAPIStore.subscribeToAPI(apiUUID, applicationID, TIER_UNLIMITED);
        ApplicationKeyDTO apiKeyDTO = restAPIStore
                .generateKeys(applicationID, "3600", null, ApplicationKeyGenerateRequestDTO.KeyTypeEnum.PRODUCTION, null,
                        grantTypes);
        String accessToken = apiKeyDTO.getToken().getAccessToken();
        startTime = benchmarkUtils.getCurrentTimeStampAndSetCorrelationID(testName);
        requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Authorization", "Bearer " + accessToken);
        requestHeaders.put("activityID", System.getProperty("testName"));
        HttpResponse invokeResponse =
                HTTPSClientUtils.doGet("http://localhost:8100/context_invokeCreatedApi/1.0.0" + "", requestHeaders);
        assertEquals(invokeResponse.getResponseCode(),
                200, "Response code mismatched");
        restAPIStore.deleteApplication(applicationID);
    }

    public String createAnApi(String apiName, String context)
            throws APIManagerIntegrationTestException, ApiException, MalformedURLException {
        //Create the api creation request object
        APIRequest apiRequest;
        apiRequest = new APIRequest(apiName, context, new URL("http://localhost:8100/"));
        apiRequest.setVersion(API_VERSION_1_0_0);
        apiRequest.setTiersCollection(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest.setTier(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest.setProvider(providerName);
        //Add the API using the API publisher.
        HttpResponse apiResponse = restAPIPublisher.addAPI(apiRequest);
        apiUUID = apiResponse.getData();
        assertEquals(apiResponse.getResponseCode(), Response.Status.CREATED.getStatusCode(),
                "Create API Response Code is invalid." + apiUUID);
        idList.add(apiUUID);
        return apiUUID;
    }

    public SSLServerSendImmediateResponse StartServer(SSLServerSendImmediateResponse server, String responseContent, int port, String location) throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                System.out.println(" >>>>> Start " + server.getClass().getSimpleName() + " backend with response content length : "+ responseContent.getBytes().length);
                server.run(port, responseContent, "200", location);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        // Giving grace period to start the server
        Thread.sleep(500);
        return server;
    }
    public void StartHTTPServer() throws InterruptedException {
        // Create a new thread to run the server
        Thread serverThread = new Thread(() -> {
            try {
                // Initialize the server to listen on port 8100
                HttpServer server = HttpServer.create(new InetSocketAddress(8100), 0);
                server.createContext("/", new SimpleHTTPServer.MyHandler());
                server.setExecutor(null); // creates a default executor
                server.start();
                System.out.println("Server is listening on port 8100...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Start the server thread
        serverThread.start();
        Thread.sleep(500);

        // Main thread can continue executing other tasks
        System.out.println("Main thread is free to do other things...");
    }
    public String readThisFile(String fileLocation) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileLocation));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            return everything;
        } finally {
            br.close();
        }
    }
    public String findServerKeyStoreLocation(){
        File directory = new File("/Users/jithmir/Work/Product_APIM_fork_2/product-apim/modules/integration/tests-integration/tests-benchmark/target");
        String prefix = "carbon";
        String serverKeyStoreLocation = "";

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    // Check if the file is a directory and starts with the prefix
                    if (file.isDirectory() && file.getName().startsWith(prefix)) {
                        System.out.println(file.getAbsolutePath());
                        serverKeyStoreLocation = file.getAbsolutePath();
                    }
                }
            } else {
                System.out.println("The directory is empty or an I/O error occurred.");
            }
        } else {
            System.out.println("The provided path is not a directory.");
        }
        serverKeyStoreLocation += "/wso2am-4.4.0-SNAPSHOT/repository/resources/security/wso2carbon.jks";
        return serverKeyStoreLocation;
    }
}
