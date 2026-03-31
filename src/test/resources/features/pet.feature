Feature: Pet API Positive Testing

  Scenario: Create a pet, update status, and verify via GET
    Given I have a pet payload with name "doggie", category "Dogs", status "available" and tags "friendly,small"
    When I send POST request to add pet
    Then pet response status should be 200
    And created pet id should be captured
    When I update the pet status to "sold"
    Then pet response status should be 200
    When I get the pet by id
    Then pet status should be "sold"

  Scenario: Search pets by status
    When I search pets by status "available"
    Then all returned pets should have status "available"
