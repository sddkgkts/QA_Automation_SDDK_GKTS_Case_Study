package com.insider.hooks;

import com.insider.api.services.PetStoreAPI;
import com.insider.context.ScenarioContext;
import com.insider.utils.ConfigReader;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * APIHooks
 *
 * Cucumber lifecycle hooks for API test scenarios.
 * Runs only for scenarios tagged with {@code @api}.
 *
 * Responsibilities:
 *  - Configure RestAssured base URI
 *  - Initialise PetStoreAPI singleton and store it in ScenarioContext
 *  - Attach the last API response to the report on failure
 *  - Reset RestAssured and clean up ScenarioContext after each scenario
 */
public class APIHooks {

    private static final Logger logger = LoggerFactory.getLogger(APIHooks.class);

    // ── @Before ──────────────────────────────────────────────────────────────

    /**
     * Runs before every scenario tagged {@code @api}.
     * Configures RestAssured and prepares the PetStoreAPI singleton.
     *
     * @param scenario the current Cucumber scenario
     */
    @Before(order = 10, value = "@api")
    public void setUpAPI(Scenario scenario) {
        logger.info("╔══════════════════════════════════════════════════╗");
        logger.info("║  🔷 API SCENARIO STARTING                        ║");
        logger.info("║  📋 {}", scenario.getName());
        logger.info("║  🏷  Tags: {}", scenario.getSourceTagNames());
        logger.info("╚══════════════════════════════════════════════════╝");

        // Clear any leftover state from a previous scenario
        ScenarioContext.clear();
        ScenarioContext.set(ScenarioContext.SCENARIO_NAME, scenario.getName());

        // Configure RestAssured
        String baseUri = ConfigReader.getInstance().getProperty("api.petstore.url");
        RestAssured.baseURI = baseUri;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Store PetStoreAPI singleton in context
        PetStoreAPI petStoreAPI = PetStoreAPI.getInstance();
        ScenarioContext.set("petStoreAPI", petStoreAPI);

        logger.info("✅ RestAssured configured — Base URI: {}", baseUri);
        logger.info("✅ PetStoreAPI singleton ready");
    }

    // ── @After ───────────────────────────────────────────────────────────────

    /**
     * Runs after every scenario tagged {@code @api}.
     * Attaches the last response body to the report on failure, then resets state.
     *
     * @param scenario the current Cucumber scenario
     */
    @After(order = 10, value = "@api")
    public void tearDownAPI(Scenario scenario) {
        String status = scenario.isFailed() ? "FAILED ❌" : "PASSED ✅";
        logger.info("╔══════════════════════════════════════════════════╗");
        logger.info("║  🔷 API SCENARIO FINISHED — {}", status);
        logger.info("║  📋 {}", scenario.getName());
        logger.info("╚══════════════════════════════════════════════════╝");

        // On failure attach last API response to Cucumber report
        if (scenario.isFailed()) {
            Response lastResponse = ScenarioContext.get(ScenarioContext.API_RESPONSE, Response.class);
            if (lastResponse != null) {
                String details = String.format(
                    "HTTP Status: %d%nResponse Body:%n%s",
                    lastResponse.getStatusCode(),
                    lastResponse.getBody().asPrettyString()
                );
                scenario.attach(details.getBytes(), "text/plain", "Last API Response");
                logger.error("❌ Last API Response — Status: {}, Body: {}",
                    lastResponse.getStatusCode(),
                    lastResponse.getBody().asString());
            }
        }

        // Reset RestAssured global state
        RestAssured.reset();

        // Remove ThreadLocal context
        ScenarioContext.remove();

        logger.info("✅ API teardown complete");
    }
}
