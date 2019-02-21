package id.semcon;

import id.semcon.engine.DataValidationEngine;
import id.semcon.engine.BaseValidationEngine;
import id.semcon.engine.UsagePolicyEngine;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

public class Service {

    public static final String CONTENT_DATA = "content-data";
    public static final String CONTENT_CONSTRAINTS = "content-constraints";
    public static final String USAGE_POLICY = "usage-policy";
    public static final String BASE_CONFIG = "base-config";
    public static final String IMAGE_CONSTRAINTS = "image-constraints";

    private static final Logger log = LoggerFactory.getLogger(Service.class);
    private static final UsagePolicyEngine usagePolicyEngine = new UsagePolicyEngine();
    private static final BaseValidationEngine baseValidationEngine = new BaseValidationEngine();
    private static final DataValidationEngine dataValidationEngine = new DataValidationEngine();

    public static void main(String[] args) {
        log.info("starting semantic services");
        Service service = new Service();
        service.establishRoutes();
        log.info("semantic services started!");
    }

    public void establishRoutes() {

        // set port
        port(2806);

        // hello world for testing
        get("/hello", (request, response) -> "hello");

        // init.trig validation
        post("/api/validate/init", (request, response) -> {
            // default response
            response.status(500);
            response.type("application/json");
            String body = request.body();
            String validationResult;
            try {
                // full json content
                JSONObject rootObject = new JSONObject(body);
                String baseConfigString = rootObject.getString(BASE_CONFIG);
                String imageConstraints = rootObject.getString(IMAGE_CONSTRAINTS);
                validationResult = baseValidationEngine.baseConfigValidation(baseConfigString, imageConstraints);
                if (validationResult.isEmpty()) {
                    response.status(200);
                }
            } catch (Exception e) {
                validationResult = e.getMessage();
            }
            return validationResult;
        });

        // usage policy checker
        post("/api/validate/usage-policy", (request, response) -> {
            // default response
            response.status(500);
            response.type("application/json");
            String body = request.body();
            log.info(request.headers().toString());
            log.info(body);
            String validationResult;
            try {
                // full json content
                JSONObject rootObject = new JSONObject(body);
                String jsonBody = rootObject.getString(USAGE_POLICY);
                validationResult = usagePolicyEngine.policyCheck(jsonBody);
                if (validationResult.isEmpty()) {
                    response.status(200);
                }
            } catch (Exception e) {
                validationResult = e.getMessage();
            }

            return validationResult;
        });

        // usage policy checker
        post("/api/validate/data", (request, response) -> {
            // default response
            response.status(500);
            response.type("application/json");
            String body = request.body();
            log.info(request.headers().toString());
            log.info(body);
            String validationResult;
            try {
                // full json content
                JSONObject rootObject = new JSONObject(body);
                String contentData = rootObject.getString(CONTENT_DATA);
                String contentConstraints = rootObject.getString(CONTENT_CONSTRAINTS);
                validationResult = dataValidationEngine.dataCheck(contentData, contentConstraints);
                if (validationResult.isEmpty()) {
                    response.status(200);
                }
            } catch (Exception e) {
                validationResult = e.getMessage();
            }

            return validationResult;
        });
    }
}
