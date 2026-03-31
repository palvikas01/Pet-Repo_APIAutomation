Feature: Pet API Negative Testing

  Scenario: Create pet with invalid status
    Given I have invalid pet payload
    When I send POST request to create pet
    Then pet negative response status should be 400 or 200

  Scenario Outline: Create pet with missing mandatory field
    Given I have base pet payload
    And I remove pet field "name" from payload
    When I send POST request to create pet
    Then pet negative response status should be 400 or 200

    Examples:
      | missingField |
      | name         |
      | photoUrls    |

  Scenario: Get pet with non-existing id
    When I send GET request with invalid pet id
    Then pet negative response status should be 404

  Scenario: Update pet with invalid id
    Given I have valid pet payload
    When I send PUT request with invalid id
    Then pet negative response status should be 400 or 200

  Scenario: Update pet with malformed JSON body
    When I send PUT request with malformed JSON body
    Then pet negative response status should be 400 or 200
