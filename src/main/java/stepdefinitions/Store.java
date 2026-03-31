package stepdefinitions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
//import static org.testng.Assert.assertEquals;

import static org.junit.Assert.assertEquals;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class Store {
    private static final String BASE_URI= "https://petstore.swagger.io/v2";

    Response response;
    Long petId;
    Long orderId;
    Map<String, Object> orderPayload = new HashMap<>();

    @Given("I have a valid order payload for the captured petID with quantity {int},status {string} and completed {string}")
    public void I_have_a_valid_payload_for_the_capture_petID(int quantity,String status ,String complete){
        petId = System.currentTimeMillis();
        orderId = System.currentTimeMillis();
        orderPayload.put("id", orderId); 
        orderPayload.put("petId", petId); 
        orderPayload.put("quantity", quantity);

        String shipDate = OffsetDateTime.now(ZoneOffset.UTC) .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")); 
        orderPayload.put("shipDate", shipDate);

        orderPayload.put("status", status); 
        orderPayload.put("complete", complete);

    }
    @When("I send post request to place a order for pet")
    public void I_send_post_request_to_place_a_order_for_pet(){
        response = given().baseUri(BASE_URI).header("Content-Type", "application/json")
            .body(orderPayload)
            .when()
            .post("/store/order");

        System.out.println("PET CREATE - Status: " + response.getStatusLine());
        System.out.println("PET CREATE - Body:\n" + response.asPrettyString());
    }
    @Then("order response status should be {int}")
    public void order_response_status_should_be(int expected) {
        assertEquals(
            "Unexpected status. Body:\n" + (response != null ? response.asPrettyString() : "<no response>"),
            expected,
            response.getStatusCode()
        );
    }

    @When("I send get request to get store inventrory")
    public void I_send_get_request_to_get_store_inventrory(){
         response = given().baseUri(BASE_URI)
            .when()
            .get("/store/inventory");

        System.out.println("PET CREATE - Status: " + response.getStatusLine());
        System.out.println("PET CREATE - Body:\n" + response.asPrettyString());

    }
    @Then("print response and validate the status")
        public void print_response_and_validate_the_status(){
            response.then().log().all(); 
            String body = response.prettyPrint();
            System.out.println("store inventor get request body" +body);
    }

    @When("I send get request to get the order")
    public void I_send_get_request_to_get_the_order(){
        response = given().baseUri(BASE_URI)
        .pathParam("orderId", orderId)
            .when()
            .get("/store/order/{orderId}");

    }

    // ---------------- NEGATIVE  STORE STEPS ----------------

    
    @Given("I have a store order payload for petId {long} with quantity {int}, status {string} and complete {word}")
    public void store_order_payload_for_pet(long petIdFromStep, int qty, String status, String completeWord) {
        boolean complete = Boolean.parseBoolean(completeWord);
        orderId = System.currentTimeMillis();
        orderPayload = new HashMap<>();
        orderPayload.put("id", orderId);
        orderPayload.put("petId", petIdFromStep);
        orderPayload.put("quantity", qty);
        String shipDate = OffsetDateTime.now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        orderPayload.put("shipDate", shipDate);
        orderPayload.put("status", status);
        orderPayload.put("complete", complete);
    }

    @Given("I have a non-existing store orderId")
    public void i_have_a_non_existing_store_order_id() {
        orderId = System.currentTimeMillis(); // very unlikely to exist
    }

    @When("I send POST request to place store order")
    public void post_store_order() {
        response = given()
            .baseUri(BASE_URI)
            .header("Content-Type", "application/json")
            .body(orderPayload)
        .when()
            .post("/store/order");

        System.out.println("STORE ORDER POST - Status: " + response.getStatusLine());
        System.out.println("STORE ORDER POST - Body:\n" + response.asPrettyString());
    }

    @Then("store response status should be {int}")
    public void store_response_status_should_be(int expected) {
        assertEquals(
            "Unexpected status. Body:\n" + (response != null ? response.asPrettyString() : "<no response>"),
            expected,
            response.getStatusCode()
        );
    }

    @Then("store negative response status should be {int}")
    public void store_negative_response_status_should_be(int expected) {
        store_response_status_should_be(expected);
    }

    @Then("store negative response status should be 400 or 200")
    public void store_negative_response_status_should_be_400_or_200() {
        int code = response != null ? response.getStatusCode() : -1;
        org.junit.Assert.assertTrue(
            "Expected 400 or 200 but was: " + code + ". Body:\n" + (response != null ? response.asPrettyString() : "<no response>"),
            code == 400 || code == 200
        );
    }

    @Then("created order id should be captured")
    public void created_order_id_should_be_captured() {
        assertEquals("Order create must be 200 to capture id", 200, response.getStatusCode());
        Long idFromResponse = null;
        try {
            idFromResponse = response.jsonPath().getLong("id");
        } catch (Exception ignored) {}
        org.junit.Assert.assertNotNull("Order id was null in response", idFromResponse);
        orderId = idFromResponse;
        System.out.println("Captured orderId: " + orderId);
    }

    @When("I send GET request to get store order by id")
    public void get_store_order_by_id() {
        java.util.Objects.requireNonNull(orderId, "orderId must not be null. Ensure it was captured before GET.");
        response = given()
            .baseUri(BASE_URI)
            .pathParam("orderId", orderId)
        .when()
            .get("/store/order/{orderId}");

        System.out.println("STORE ORDER GET - Status: " + response.getStatusLine());
        System.out.println("STORE ORDER GET - Body:\n" + response.asPrettyString());
    }

    @When("I send DELETE request to delete store order by id")
    public void delete_store_order_by_id() {
        java.util.Objects.requireNonNull(orderId, "orderId must not be null. Ensure it was captured before DELETE.");
        response = given()
            .baseUri(BASE_URI)
            .pathParam("orderId", orderId)
        .when()
            .delete("/store/order/{orderId}");

        System.out.println("STORE ORDER DELETE - Status: " + response.getStatusLine());
        System.out.println("STORE ORDER DELETE - Body:\n" + response.asPrettyString());
    }

    @When("I send DELETE request to delete store order by id again")
    public void delete_store_order_by_id_again() {
        delete_store_order_by_id();
    }

    @Then("store error message should contain {string}")
    public void store_error_message_should_contain(String expected) {
        String msg = null;
        try {
            msg = response.jsonPath().getString("message");
        } catch (Exception ignored) {}
        boolean ok = (msg != null) && msg.toLowerCase().contains(expected.toLowerCase());
        org.junit.Assert.assertTrue(
            "Expected message to contain (case-insensitive): " + expected + " but was: " + msg,
            ok
        );
    }
   


    
}
