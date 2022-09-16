@pet @api
Feature: Verify PET endpoint

  Background:
    Given I use "https://petstore.swagger.io/v2" as base URI

  @pet1
  Scenario Outline: Verify findByStatus functionality of PET API
    When I make "GET" call to "/pet/findByStatus?status=<STATUS>"
    Then I get response code "200"
    And I verify at least "1" pet details is available in the response
    Examples:
      | STATUS    |
      | available |
      | pending   |
      | sold      |

  @pet2
  Scenario: Verify add new pet and delete added pet functionality of PET API
    Given I make REST service headers with the below fields
      | Content-Type     | Accept           |
      | application/json | application/json |
    Given I read request body from "addNewPetRequest"
    When I make "POST" call to "/pet"
    Then I get response code "200"
    And I capture the ID from response
    And I make "DELETE" call to "/pet"
    And I get response code "200"
    And I make "DELETE" call to "/pet"
    And I get response code "404"


    
