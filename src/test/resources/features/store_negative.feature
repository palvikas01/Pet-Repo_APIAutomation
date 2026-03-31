Feature: Store API Negative Testing


  Scenario Outline: Place order with invalid petId returns client error
    Given I have a store order payload for petId <petId> with quantity 1, status "placed" and complete true
    When I send POST request to place store order
    Then store negative response status should be 400 or 200

    Examples:
      | petId        |
      | -1           |
      | 999999999999 |

  
  Scenario: Get order with non-existing id returns not found
    Given I have a non-existing store orderId
    When I send GET request to get store order by id
    Then store negative response status should be 404
    And store error message should contain "Order not found"

  
  Scenario: Deleting an order twice returns 404 on second attempt
    Given I have a store order payload for petId 123456 with quantity 1, status "placed" and complete true
    When I send POST request to place store order
    Then store response status should be 200
    And created order id should be captured

    When I send DELETE request to delete store order by id
    Then store response status should be 200

    When I send DELETE request to delete store order by id again
    Then store negative response status should be 404
    And store error message should contain "Order not found"
