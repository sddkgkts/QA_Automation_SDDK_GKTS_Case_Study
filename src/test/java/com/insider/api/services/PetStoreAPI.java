package com.insider.api.services;

import com.insider.api.models.Pet;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

/**
 * PetStoreAPI
 *
 * Singleton service class for PetStore API operations.
 * Wraps all REST Assured calls for the {@code /pet} endpoints.
 *
 * Endpoints covered:
 *   POST   /pet                  — create
 *   GET    /pet/{id}             — read by ID
 *   PUT    /pet                  — update
 *   DELETE /pet/{id}             — delete
 *   GET    /pet/findByStatus     — find by status
 */
public class PetStoreAPI {

    private static final Logger logger = LoggerFactory.getLogger(PetStoreAPI.class);
    private static volatile PetStoreAPI instance;

    private PetStoreAPI() {}

    /**
     * Returns the singleton PetStoreAPI instance (double-checked locking).
     */
    public static PetStoreAPI getInstance() {
        if (instance == null) {
            synchronized (PetStoreAPI.class) {
                if (instance == null) {
                    instance = new PetStoreAPI();
                    logger.info("✅ PetStoreAPI singleton created");
                }
            }
        }
        return instance;
    }

    // ── CRUD Operations ───────────────────────────────────────────────────────

    /**
     * Create a new pet — POST /pet.
     *
     * @param pet pet to create
     * @return API response
     */
    public Response createPet(Pet pet) {
        logger.info("➕ Creating pet: '{}'", pet.getName());
        Response response = given()
            .contentType("application/json")
            .accept("application/json")
            .body(pet)
        .when()
            .post("/pet")
        .then()
            .log().ifError()
            .extract().response();
        logger.info("✅ createPet — Status: {}", response.getStatusCode());
        return response;
    }

    /**
     * Retrieve a pet by ID — GET /pet/{petId}.
     *
     * @param petId pet identifier
     * @return API response
     */
    public Response getPetById(Long petId) {
        logger.info("📖 Retrieving pet — ID: {}", petId);
        Response response = given()
            .accept("application/json")
        .when()
            .get("/pet/" + petId)
        .then()
            .log().ifError()
            .extract().response();
        logger.info("✅ getPetById — Status: {}", response.getStatusCode());
        return response;
    }

    /**
     * Update an existing pet — PUT /pet.
     *
     * @param pet pet with updated fields
     * @return API response
     */
    public Response updatePet(Pet pet) {
        logger.info("✏  Updating pet — ID: {}, New name: '{}'", pet.getId(), pet.getName());
        Response response = given()
            .contentType("application/json")
            .accept("application/json")
            .body(pet)
        .when()
            .put("/pet")
        .then()
            .log().ifError()
            .extract().response();
        logger.info("✅ updatePet — Status: {}", response.getStatusCode());
        return response;
    }

    /**
     * Delete a pet by ID — DELETE /pet/{petId}.
     *
     * @param petId pet identifier
     * @return API response
     */
    public Response deletePet(Long petId) {
        logger.info("🗑  Deleting pet — ID: {}", petId);
        Response response = given()
            .accept("application/json")
        .when()
            .delete("/pet/" + petId)
        .then()
            .log().ifError()
            .extract().response();
        logger.info("✅ deletePet — Status: {}", response.getStatusCode());
        return response;
    }

    /**
     * Find pets by status — GET /pet/findByStatus?status={status}.
     *
     * @param status available | pending | sold
     * @return API response
     */
    public Response getPetsByStatus(String status) {
        logger.info("🔍 Finding pets by status: '{}'", status);
        Response response = given()
            .accept("application/json")
            .queryParam("status", status)
        .when()
            .get("/pet/findByStatus")
        .then()
            .log().ifError()
            .extract().response();
        int count = response.getStatusCode() == 200
            ? response.jsonPath().getList("$").size() : 0;
        logger.info("✅ getPetsByStatus — Status: {}, Count: {}", response.getStatusCode(), count);
        return response;
    }

    /**
     * Retrieve a pet using an invalid ID (negative test).
     * Expects HTTP 404 from the API.
     *
     * @return API response
     */
    public Response getPetByInvalidId() {
        Long invalidId = -999999L;
        logger.info("🔴 Retrieving pet with invalid ID: {} (negative test)", invalidId);
        Response response = given()
            .accept("application/json")
        .when()
            .get("/pet/" + invalidId)
        .then()
            .log().ifError()
            .extract().response();
        logger.info("✅ getPetByInvalidId — Status: {}", response.getStatusCode());
        return response;
    }
}
