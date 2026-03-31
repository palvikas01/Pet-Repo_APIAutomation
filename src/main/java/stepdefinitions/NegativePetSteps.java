package stepdefinitions;
import java.util.HashMap;
import java.util.Map;
//import static org.testng.Assert.assertEquals;
import static org.junit.Assert.assertEquals;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class NegativePetSteps {
   Response response;
   Map<String, Object> payload;
   int petId;
   String baseUrl = "https://petstore.swagger.io/v2";

   // ---------------- NEGATIVE ----------------
   @Given("I have invalid pet payload")
   public void invalidPetPayload() {
       payload = new HashMap<>();
       payload.put("id", System.currentTimeMillis());
       payload.put("name", "doggie");
       payload.put("status", "invalid_status"); // wrong value
       // minimal spec fields to pass schema, but invalid status value
       payload.put("photoUrls", java.util.Arrays.asList("string"));
   }

   @When("I send POST request to create pet")
   public void createPet() {
       response =
               given()
                       .baseUri(baseUrl)
                       .header("Content-Type", "application/json")
                       .body(payload)
               .when()
                       .post("/pet");
   }

   @When("I send GET request with invalid pet id")
   public void getInvalidPet() {
       response =
               given()
                       .baseUri(baseUrl)
               .when()
                       .get("/pet/999999999"); // non-existing
   }

   @When("I send PUT request with invalid id")
   public void updateInvalidPet() {
       if (payload == null) {
           // ensure a base payload exists so PUT has a body
           i_have_base_pet_payload();
       }
       payload.put("id", -1); // invalid id
       response =
               given()
                       .baseUri(baseUrl)
                       .header("Content-Type", "application/json")
                       .body(payload)
               .when()
                       .put("/pet");
   }


   @Then("pet negative response status should be {int}")
   public void pet_negative_response_status_should_be(int statusCode) {
       assertEquals(response.getStatusCode(), statusCode);
   }

   @Then("pet negative response status should be 400 or 200")
   public void pet_negative_response_status_should_be_400_or_200() {
       int code = response != null ? response.getStatusCode() : -1;
       org.junit.Assert.assertTrue(
           "Expected 400 or 200 but was: " + code + ". Body:\n" + (response != null ? response.asPrettyString() : "<no response>"),
           code == 400 || code == 200
       );
   }

   // ---------------- New steps to support negative feature ----------------

   // Build a valid/base payload (id, name, photoUrls per spec, and valid status)
   @Given("I have base pet payload")
   public void i_have_base_pet_payload() {
       payload = new HashMap<>();
       payload.put("id", System.currentTimeMillis());
       payload.put("name", "doggie");
       payload.put("photoUrls", java.util.Arrays.asList("string"));
       payload.put("status", "available");
   }

   // Alias for readability in tests that say "valid"
   @Given("I have valid pet payload")
   public void i_have_valid_pet_payload() {
       i_have_base_pet_payload();
   }

   // Remove a field by key from payload (e.g., "name", "photoUrls")
@io.cucumber.java.en.And("I remove pet field {string} from payload")
   public void i_remove_field_from_payload(String field) {
       if (payload != null) {
           payload.remove(field);
       }
   }

   // Optional alias to keep compatibility with prior step text
   @When("I send POST request to create pet (negative)")
   public void createPetNegative() {
       createPet();
   }


   // Malformed JSON body for update
   @When("I send PUT request with malformed JSON body")
   public void putMalformedJson() {
       String badJson = "{\"id\": 123, \"name\": \"doggie\", }"; // trailing comma => malformed
       response = given()
               .baseUri(baseUrl)
               .header("Content-Type", "application/json")
               .body(badJson)
           .when()
               .put("/pet");
   }

   // ---------------- E2E FLOW ----------------
   @Given("I create a new pet")
   public void createNewPet() {
       petId = (int) (System.currentTimeMillis() % 100000);
       payload = new HashMap<>();
       payload.put("id", petId);
       payload.put("name", "doggie");
       payload.put("status", "available");
       payload.put("photoUrls", java.util.Arrays.asList("string"));
       response =
               given()
                       .baseUri(baseUrl)
                       .header("Content-Type", "application/json")
                       .body(payload)
               .when()
                       .post("/pet");
       assertEquals(response.getStatusCode(), 200);
   }

   @When("I get pet by id")
   public void getPetById() {
       response =
               given()
                       .baseUri(baseUrl)
               .when()
                       .get("/pet/" + petId);
   }

   @Then("pet details should be correct")
   public void validatePetDetails() {
       assertEquals(response.getStatusCode(), 200);
       assertEquals(response.jsonPath().getInt("id"), petId);
       assertEquals(response.jsonPath().getString("status"), "available");
   }

   @When("I update pet status")
   public void updatePet() {
       payload.put("status", "sold");
       response =
               given()
                       .baseUri(baseUrl)
                       .header("Content-Type", "application/json")
                       .body(payload)
               .when()
                       .put("/pet");
       assertEquals(response.getStatusCode(), 200);
   }

   @Then("updated pet status should be verified")
   public void verifyUpdatedPet() {
       response =
               given()
                       .baseUri(baseUrl)
               .when()
                       .get("/pet/" + petId);
       assertEquals(response.getStatusCode(), 200);
       assertEquals(response.jsonPath().getString("status"), "sold");
   }
}
