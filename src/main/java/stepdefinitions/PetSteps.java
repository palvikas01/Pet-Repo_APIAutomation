package stepdefinitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue; */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PetSteps {
    private static final Logger log = LoggerFactory.getLogger(PetSteps.class);
    private static final String BASE_URI = "https://petstore.swagger.io/v2";

    Response response;
    Long petId;
    Map<String, Object> petPayload;
    List<Map<String, Object>> petsList;

    @Given("I have a pet payload with name {string}, category {string}, status {string} and tags {string}")
    public void i_have_a_pet_payload(String name, String categoryName, String status, String tagsCsv) {
        petId = System.currentTimeMillis();

        Map<String, Object> category = new HashMap<>();
        category.put("id", 1);
        category.put("name", categoryName);

        List<Map<String, Object>> tags = new ArrayList<>();
        if (tagsCsv != null && !tagsCsv.trim().isEmpty()) {
            String[] parts = tagsCsv.split(",");
            for (int i = 0; i < parts.length; i++) {
                String t = parts[i].trim();
                if (!t.isEmpty()) {
                    Map<String, Object> tag = new HashMap<>();
                    tag.put("id", i + 1);
                    tag.put("name", t);
                    tags.add(tag);
                }
            }
        }

        petPayload = new HashMap<>();
        petPayload.put("id", petId);
        petPayload.put("category", category);
        petPayload.put("name", name);
        // keep at least one photoUrl per spec
        List<String> photoUrls = new ArrayList<>();
        photoUrls.add("string");
        petPayload.put("photoUrls", photoUrls);
        petPayload.put("tags", tags);
        petPayload.put("status", status);
    }

    @When("I send POST request to add pet")
    public void i_send_post_request_to_add_pet() {
        response = given()
            .baseUri(BASE_URI)
            .header("Content-Type", "application/json")
            .body(petPayload)
        .when()
            .post("/pet");

        log.info("PET CREATE - Status: {}", response.getStatusLine());
        log.info("PET CREATE - Body:\n{}", response.asPrettyString());
    }

    @Then("pet response status should be {int}")
    public void pet_response_status_should_be(int expected) {
        assertEquals(
            "Unexpected status. Body:\n" + (response != null ? response.asPrettyString() : "<no response>"),
            expected,
            response.getStatusCode()
        );
    }

    @And("created pet id should be captured")
    public void created_pet_id_should_be_captured() {
        Long idFromResponse = null;
        try {
            idFromResponse = response.jsonPath().getLong("id");
        } catch (Exception ignored) {}
        if (idFromResponse != null && idFromResponse > 0) {
            petId = idFromResponse;
        }
        assertNotNull("Pet id should not be null", petId);
        // ensure payload stays in sync
        if (petPayload != null) {
            petPayload.put("id", petId);
        }
        log.info("Captured petId: {}", petId);
    }

    @When("I update the pet status to {string}")
    public void i_update_the_pet_status_to(String newStatus) {
        assertNotNull("Pet payload must exist before updating", petPayload);
        // ensure id/name present when updating
        assertNotNull("Pet id must be available for update", petId);
        petPayload.put("id", petId);
        petPayload.put("status", newStatus);

        response = given()
            .baseUri(BASE_URI)
            .header("Content-Type", "application/json")
            .body(petPayload)
        .when()
            .put("/pet");

        log.info("PET UPDATE - Status: {}", response.getStatusLine());
        log.info("PET UPDATE - Body:\n{}", response.asPrettyString());
    }

    @When("I get the pet by id")
    public void i_get_the_pet_by_id() {
        assertNotNull("Pet id must be available to GET", petId);
        response = given()
            .baseUri(BASE_URI)
            .pathParam("petId", petId)
        .when()
            .get("/pet/{petId}");

        log.info("PET GET - petId: {}", petId);
        log.info("PET GET - Status: {}", response.getStatusLine());
        log.info("PET GET - Body:\n{}", response.asPrettyString());
    }

    @Then("pet status should be {string}")
    public void pet_status_should_be(String expected) {
        String actual = response.jsonPath().getString("status");
        assertEquals("Unexpected pet status", expected, actual);
    }

    @When("I search pets by status {string}")
    public void i_search_pets_by_status(String status) {
        response = given()
            .baseUri(BASE_URI)
            .queryParam("status", status)
        .when()
            .get("/pet/findByStatus");

        log.info("PET FIND BY STATUS - Status: {}", response.getStatusLine());
        // Don't print entire list to avoid huge logs
        log.info("PET FIND BY STATUS - Count: {}", response.jsonPath().getList("").size());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) (List<?>) response.jsonPath().getList("");
        petsList = list;
    }

    @Then("all returned pets should have status {string}")
    public void all_returned_pets_should_have_status(String expected) {
        assertNotNull("Pets list should be initialized", petsList);
        for (Map<String, Object> p : petsList) {
            Object st = p.get("status");
            assertTrue("Status field should be string or null", st == null || st instanceof String);
            if (st != null) {
                assertEquals("A pet had unexpected status", expected, (String) st);
            }
        }
    }
}
