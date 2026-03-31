Feature: User API Negative Testing


  Scenario Outline: Create user with missing mandatory field <missingField>
    Given I have base user payload
    And I remove field "<missingField>" from payload
    When I send POST request to create user (negative)
    Then negative response status should be 200
    # And error message should contain "Invalid input"

    Examples:
      | missingField | 
      | username     |
      | password     |
 
  # 2 Login with a non-existing user
  Scenario: Login with a non-existing user
    When I login with a non-existing user
    Then negative response status should be 200
    And error message should contain "logged in user session"

  # 3 Get user with invalid username format
  Scenario: Get user with invalid username format
    When I get user by username "Shree"
    Then negative response status should be 404
    # And error message should contain "User not found"
