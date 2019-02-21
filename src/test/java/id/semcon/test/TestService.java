package id.semcon.test;

import id.semcon.Service;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

public class TestService {

    private static final Logger log = LoggerFactory.getLogger(TestService.class);

    public static TestResponse request(String method, String path, String requestBody) {
        try {
            URL url = new URL("http://localhost:2806" + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            if (method.equals("POST")) {
                connection.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter osWriter = new OutputStreamWriter(connection.getOutputStream());
                osWriter.write(requestBody);
                osWriter.flush();
            }
            connection.connect();
            try {
                String body = IOUtils.toString(connection.getInputStream(), "UTF-8");
                return new TestResponse(connection.getResponseCode(), body);
            } catch (IOException e) {
                String error = IOUtils.toString(connection.getErrorStream(), "UTF-8");
                return new TestResponse(connection.getResponseCode(), error);
            }

        } catch (IOException e) {
            e.printStackTrace();
            fail("Sending request failed: " + e.getMessage());
            return new TestResponse();
        }
    }

    @BeforeClass public static void setUp() throws Exception {
        Service.main(null);
        awaitInitialization();

    }

    @AfterClass public static void tearDown() throws Exception {
        stop();
    }

    @Test public void testHello() {
        String testUrl = "/hello";
        TestResponse res = request("GET", testUrl, null);
        assertEquals(200, res.status);
        assertEquals("hello", res.body);
        log.info(res.toString());
    }

    @Test public void testUsagePolicy() throws IOException {
        String testUrl = "/api/validate/usage-policy";

        InputStream complexUsagePolicyIS =
                TestService.class.getClassLoader().getResourceAsStream("usage-policy/complex-template.ttl");
        String usagePolicyString = IOUtils.toString(complexUsagePolicyIS, "UTF-8");
        complexUsagePolicyIS.close();
        TestResponse res = initTestResponse(Service.USAGE_POLICY, usagePolicyString, testUrl);
        assertEquals(200, res.status);

        InputStream complexUsagePolicyISFalse =
                TestService.class.getClassLoader().getResourceAsStream("usage-policy/false-complex-template.ttl");
        usagePolicyString = IOUtils.toString(complexUsagePolicyISFalse, "UTF-8");
        complexUsagePolicyISFalse.close();
        res = initTestResponse(Service.USAGE_POLICY, usagePolicyString, testUrl);
        assertEquals(500, res.status);

        InputStream usagePolicyIS =
                TestService.class.getClassLoader().getResourceAsStream("usage-policy/template.ttl");
        usagePolicyString = IOUtils.toString(usagePolicyIS, "UTF-8");
        usagePolicyIS.close();
        res = initTestResponse(Service.USAGE_POLICY, usagePolicyString, testUrl);
        assertEquals(200, res.status);

        InputStream usagePolicyISFalse =
                TestService.class.getClassLoader().getResourceAsStream("usage-policy/false-template.ttl");
        usagePolicyString = IOUtils.toString(usagePolicyISFalse, "UTF-8");
        usagePolicyISFalse.close();
        res = initTestResponse(Service.USAGE_POLICY, usagePolicyString, testUrl);
        assertEquals(500, res.status);
    }

    @Test public void testInitValidation() throws IOException {
        String testUrl = "/api/validate/init";

        InputStream initIS = TestService.class.getClassLoader().getResourceAsStream("init/example-init.trig");
        String initString = IOUtils.toString(initIS, "UTF-8");
        initIS.close();
        InputStream constraintsIS = TestService.class.getClassLoader().getResourceAsStream("init/image-constraints.trig");
        String imageConstraints = IOUtils.toString(constraintsIS, "UTF-8");
        constraintsIS.close();

        JSONObject object = new JSONObject();
        object.put(Service.BASE_CONFIG, initString);
        object.put(Service.IMAGE_CONSTRAINTS, imageConstraints);

        TestResponse res = request("POST", testUrl, object.toString());
        //        System.out.println(res.body);
        assertEquals(200, res.status);
    }

    @Test public void testZamgInitValidation() throws IOException {
        String testUrl = "/api/validate/init";

        InputStream initIS = TestService.class.getClassLoader().getResourceAsStream("init/zamg-init.trig");
        String initString = IOUtils.toString(initIS, "UTF-8");
        initIS.close();
        InputStream constraintsIS = TestService.class.getClassLoader().getResourceAsStream("init/image-constraints.trig");
        String imageConstraints = IOUtils.toString(constraintsIS, "UTF-8");
        constraintsIS.close();

        JSONObject object = new JSONObject();
        object.put(Service.BASE_CONFIG, initString);
        object.put(Service.IMAGE_CONSTRAINTS, imageConstraints);

        TestResponse res = request("POST", testUrl, object.toString());
        //        System.out.println(res.body);
        assertEquals(200, res.status);
    }

    @Test public void testZamgValidDataValidation() throws IOException {
        String testUrl = "/api/validate/data";

        InputStream initIS = TestService.class.getClassLoader().getResourceAsStream("data/zamg-data.json");
        String initString = IOUtils.toString(initIS, "UTF-8");
        initIS.close();

        JSONObject object = new JSONObject(initString);

        TestResponse res = request("POST", testUrl, object.toString());
        //        System.out.println(res.body);
        assertEquals(200, res.status);
    }

    @Test public void testZamgInvalidDataValidation() throws IOException {
        String testUrl = "/api/validate/data";

        InputStream initIS = TestService.class.getClassLoader().getResourceAsStream("data/zamg-data-invalid.json");
        String initString = IOUtils.toString(initIS, "UTF-8");
        initIS.close();

        JSONObject object = new JSONObject(initString);

        TestResponse res = request("POST", testUrl, object.toString());
        //        System.out.println(res.body);
        assertEquals(500, res.status);
    }

    private TestResponse initTestResponse(String jsonKey, String usagePolicyString, String testUrl) {

        JSONObject object = new JSONObject();
        object.put(jsonKey, usagePolicyString);
        return request("POST", testUrl, object.toString());
    }

    public static class TestResponse {

        public final String body;
        public final int status;

        public TestResponse() {
            body = "";
            status = 500;
        }

        public TestResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(System.lineSeparator());
            sb.append("status: ").append(status).append(System.lineSeparator());
            sb.append("body: ").append(body);

            return sb.toString();
        }
    }

}
