Feature: User API Testing

Scenario: Create user from with valid data
  Given I have user payload from Excel "TC01" in sheet "user"
  When I send POST request to create user
  Then response status should be 200
  And response content type should be JSON
  And response should contain success message

       When I login with the created username and password
    Then login should be successful with status 200
    And login response message should contain "logged in user session"

    When I get user by the created username
    Then user response should contain fields id, username, email, userStatus
    And user response fields should match the created payload

