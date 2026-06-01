package com.insider.api.steps;

import com.insider.context.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PetAssertionSteps {

    private static final Logger logger = LoggerFactory.getLogger(PetAssertionSteps.class);

    private Response lastResponse() {
        Response response = ScenarioContext.get(ScenarioContext.API_RESPONSE, Response.class);
        Assert.assertNotNull(
            response,
            "No API response found in context. Has a CRUD step run before this assertion?"
        );
        return response;
    }

    @Step("Verifying pet was created with HTTP 200")
    @Then("Pet is created successfully with status 200")
    public void verifyPetCreatedSuccessfully() {
        logger.info("🔍 Verifying pet creation — expected HTTP 200...");
        int actual = lastResponse().getStatusCode();
        Assert.assertEquals(
            actual, 200,
            "Expected HTTP 200 for pet creation, got: " + actual
            + "\nResponse body: " + lastResponse().getBody().asString()
        );
        logger.info("✅ Pet created successfully (HTTP 200)");
    }

    @Step("Verifying response status is {0}")
    @Then("Response status is {int}")
    public void verifyResponseStatus(int expectedStatus) {
        logger.info("🔍 Verifying response status — expected: {}", expectedStatus);
        int actual = lastResponse().getStatusCode();
        Assert.assertEquals(
            actual, expectedStatus,
            "Expected HTTP " + expectedStatus + ", got: " + actual
            + "\nResponse body: " + lastResponse().getBody().asString()
        );
        logger.info("✅ Response status verified: {}", actual);
    }

    @Step("Verifying pet name is '{0}'")
    @And("Pet name is {string}")
    public void verifyPetName(String expectedName) {
        logger.info("🔍 Verifying pet name — expected: '{}'", expectedName);
        String actual = lastResponse().jsonPath().getString("name");
        Assert.assertEquals(
            actual, expectedName,
            "Expected pet name '" + expectedName + "', got: '" + actual + "'"
        );
        logger.info("✅ Pet name verified: '{}'", actual);
    }

    @Step("Verifying pet list is not empty")
    @Then("Pet list is not empty")
    public void verifyPetListNotEmpty() {
        logger.info("🔍 Verifying pet list is not empty...");
        int status = lastResponse().getStatusCode();
        Assert.assertEquals(status, 200, "Expected HTTP 200 for pet list, got: " + status);
        int count = lastResponse().jsonPath().getList("$").size();
        Assert.assertTrue(count > 0, "Pet list is empty — no pets returned by the API!");
        logger.info("✅ Pet list is not empty — {} pet(s) returned", count);
    }
}
