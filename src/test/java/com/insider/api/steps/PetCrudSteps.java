package com.insider.api.steps;

import com.insider.api.models.Pet;
import com.insider.api.services.PetStoreAPI;
import com.insider.context.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PetCrudSteps {

    private static final Logger logger = LoggerFactory.getLogger(PetCrudSteps.class);

    private PetStoreAPI petStoreAPI() {
        return ScenarioContext.get("petStoreAPI", PetStoreAPI.class);
    }

    private void saveResponse(Response response) {
        ScenarioContext.set(ScenarioContext.API_RESPONSE, response);
    }

    @Step("Verifying PetStore API is initialized")
    @Given("PetStore API is initialized")
    public void initializeAPI() {
        Assert.assertNotNull(petStoreAPI(), "PetStoreAPI singleton was not initialised by APIHooks!");
        logger.info("✅ PetStore API is ready — instance: {}", petStoreAPI());
    }

    @Step("Creating a new pet with name '{0}'")
    @When("Create a new pet with name {string}")
    public void createNewPet(String petName) {
        logger.info("➕ Creating new pet with name '{}'...", petName);
        Pet newPet = Pet.createPet(petName);
        Response response = petStoreAPI().createPet(newPet);
        saveResponse(response);
        ScenarioContext.set(ScenarioContext.CURRENT_PET, newPet);
        if (response.getStatusCode() == 200) {
            Long petId = response.jsonPath().getLong("id");
            newPet.setId(petId);
            ScenarioContext.set(ScenarioContext.SAVED_PET_ID, petId);
            logger.info("✅ Pet created — ID: {}, Name: '{}'", petId, petName);
        } else {
            logger.warn("⚠  Pet creation returned status: {}", response.getStatusCode());
        }
    }

    @Step("Saving the pet ID from last response")
    @And("Save the pet ID")
    public void savePetId() {
        Response lastResponse = ScenarioContext.get(ScenarioContext.API_RESPONSE, Response.class);
        Assert.assertNotNull(lastResponse, "No response available to save ID from!");
        Long petId = lastResponse.jsonPath().getLong("id");
        ScenarioContext.set(ScenarioContext.SAVED_PET_ID, petId);
        Pet currentPet = ScenarioContext.get(ScenarioContext.CURRENT_PET, Pet.class);
        if (currentPet != null) {
            currentPet.setId(petId);
        }
        logger.info("✅ Pet ID saved: {}", petId);
    }

    @Step("Retrieving pet by saved ID")
    @And("Retrieve pet by ID")
    public void retrievePetById() {
        Long petId = ScenarioContext.get(ScenarioContext.SAVED_PET_ID, Long.class);
        Assert.assertNotNull(petId, "No pet ID saved in context!");
        logger.info("📖 Retrieving pet — ID: {}", petId);
        Response response = petStoreAPI().getPetById(petId);
        saveResponse(response);
        logger.info("✅ Pet retrieved — Status: {}", response.getStatusCode());
    }

    @Step("Retrieving pet with invalid ID (negative test)")
    @When("Retrieve pet with invalid ID")
    public void retrievePetWithInvalidId() {
        logger.info("🔴 Retrieving pet with invalid ID (negative test)...");
        Response response = petStoreAPI().getPetByInvalidId();
        saveResponse(response);
        logger.info("✅ Negative test request sent — Status: {}", response.getStatusCode());
    }

    @Step("Updating pet name to '{0}'")
    @And("Update pet name to {string}")
    public void updatePetName(String newName) {
        Pet currentPet = ScenarioContext.get(ScenarioContext.CURRENT_PET, Pet.class);
        Assert.assertNotNull(currentPet, "No current pet in context to update!");
        logger.info("✏  Updating pet name: '{}' → '{}'", currentPet.getName(), newName);
        currentPet.setName(newName);
        Response response = petStoreAPI().updatePet(currentPet);
        saveResponse(response);
        logger.info("✅ Pet updated — Status: {}", response.getStatusCode());
    }

    @Step("Deleting the pet by saved ID")
    @And("Delete the pet")
    public void deletePet() {
        Long petId = ScenarioContext.get(ScenarioContext.SAVED_PET_ID, Long.class);
        Assert.assertNotNull(petId, "No pet ID saved in context to delete!");
        logger.info("🗑  Deleting pet — ID: {}", petId);
        Response response = petStoreAPI().deletePet(petId);
        saveResponse(response);
        logger.info("✅ Delete request sent — Status: {}", response.getStatusCode());
    }

    @Step("Searching for pets with status '{0}'")
    @When("Search for pets with status {string}")
    public void searchPetsByStatus(String status) {
        logger.info("🔍 Searching for pets with status '{}'...", status);
        Response response = petStoreAPI().getPetsByStatus(status);
        saveResponse(response);
        logger.info("✅ Search complete — Status: {}", response.getStatusCode());
    }
}
