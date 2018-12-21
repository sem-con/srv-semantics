package id.semcon;

import id.semcon.engine.InitValidationEngine;
import id.semcon.engine.UsagePolicyEngine;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

public class Service {

    public static final String USAGE_POLICY = "usage-policy";
    public static final String INIT_CONFIG = "init-config";
    public static final String BASE_CONSTRAINTS = "base-constraints";

    private static final Logger log = LoggerFactory.getLogger(Service.class);
    private static final UsagePolicyEngine usagePolicyEngine = new UsagePolicyEngine();
    private static final InitValidationEngine initValidationEngine = new InitValidationEngine();

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
                String initConfigString = rootObject.getString(INIT_CONFIG);
                String baseConstraints = rootObject.getString(BASE_CONSTRAINTS);
                validationResult = initValidationEngine.initConfigValidation(initConfigString, baseConstraints);
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
    }
}
