@api @petstore
Feature: PetStore API — CRUD Operations

  Background:
    Given PetStore API is initialized

  @smoke
  Scenario: Create a new pet successfully
    When Create a new pet with name "AutoTestPet"
    Then Pet is created successfully with status 200
    And Pet name is "AutoTestPet"

  @smoke
  Scenario: Retrieve a pet by ID
    When Create a new pet with name "TestPet123"
    And Save the pet ID
    And Retrieve pet by ID
    Then Response status is 200
    And Pet name is "TestPet123"

  @regression
  Scenario: Update a pet's name
    When Create a new pet with name "OriginalName"
    And Save the pet ID
    And Update pet name to "UpdatedName"
    Then Response status is 200
    And Pet name is "UpdatedName"

  @regression
  Scenario: Delete a pet
    When Create a new pet with name "DeleteMe"
    And Save the pet ID
    And Delete the pet
    Then Response status is 200

  @regression @negative
  Scenario: Retrieve pet with invalid ID returns 404
    When Retrieve pet with invalid ID
    Then Response status is 404

  @regression @parametrize
  Scenario Outline: Find pets by status returns non-empty list
    When Search for pets with status "<status>"
    Then Pet list is not empty

    Examples:
      | status    |
      | available |
      | pending   |
      | sold      |
