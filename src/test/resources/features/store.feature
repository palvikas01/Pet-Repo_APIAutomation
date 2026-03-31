Feature: In this we will covering Store API automation

@store
Scenario:Place a order for a existing pet snd verify
Given I have a valid order payload for the captured petID with quantity 1,status "placed" and completed "true"
When I send post request to place a order for pet 
Then order response status should be 200
When I send get request to get the order
Then print response and validate the status


@store
Scenario: Verify the store inventory request
When I send get request to get store inventrory
Then print response and validate the status 




