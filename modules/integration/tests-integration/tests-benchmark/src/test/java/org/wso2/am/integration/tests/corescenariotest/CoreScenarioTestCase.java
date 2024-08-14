package org.wso2.am.integration.tests.corescenariotest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.compass.core.util.Assert;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.am.integration.clients.publisher.api.ApiException;
import org.wso2.am.integration.clients.store.api.v1.dto.APIDTO;
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

    @Factory(dataProvider = "userModeDataProvider")
    public CoreScenarioTestCase(TestUserMode userMode) {

        this.userMode = userMode;
    }

    @DataProvider
    public static Object[][] userModeDataProvider() {

        return new Object[][]{
                new Object[]{TestUserMode.SUPER_TENANT_ADMIN},
                new Object[]{TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init(userMode);
        apiEndPointUrl = gatewayUrlsWrk.getWebAppURLHttp() + API_END_POINT_POSTFIX_URL;
        providerName = user.getUserName();
    }

    @Test
    public void invokeCreatedApi(Method method)
            throws InterruptedException, IOException, ParseException, APIManagerIntegrationTestException, ApiException,
            org.wso2.am.integration.clients.store.api.ApiException, XPathExpressionException, JSONException {

        scenario = "INVOKE_API";
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
                HTTPSClientUtils.doGet(getAPIInvocationURLHttps(context, API_VERSION_1_0_0) + "", requestHeaders);
        assertEquals(invokeResponse.getResponseCode(),
                200, "Response code mismatched");
//        System.out.println(invokeResponse.getData());
       restAPIStore.deleteApplication(applicationID);
    }

    @AfterMethod()
    public void resetScenarioName() {

        System.setProperty("testName", "");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpArtifacts() throws Exception {

        super.cleanUp();
    }

    public String createAnApi(String apiName, String context)
            throws APIManagerIntegrationTestException, ApiException, MalformedURLException {
        //Create the api creation request object
        APIRequest apiRequest;
        apiRequest = new APIRequest(apiName, context, new URL("https://localhost:8100/"));
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
}


