package stepdefinitions;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class NegativeUserSteps {
    private static final String BASE_URI = "https://petstore.swagger.io/v2";
    Response response;
    Map<String, Object> payload;

    @Given("I have base user payload")
    public void i_have_base_user_payload() {
        long id = System.currentTimeMillis();
        String username = "testuser_" + id;
        String password = "12345";

        payload = new HashMap<>();
        payload.put("id", id);
        payload.put("username", username);
        payload.put("firstName", "John");
        payload.put("lastName", "Doe");
        payload.put("email", "john@test.com");
        payload.put("password", password);
        payload.put("phone", "9999999999");
        payload.put("userStatus", 1);
    }

    @And("I remove field {string} from payload")
    public void i_remove_field_from_payload(String field) {
        if (payload != null) {
            payload.remove(field);
        }
    }

    @When("I send POST request to create user \\(negative\\)")
    public void i_send_post_request_to_create_user_negative() {
        response = given()
            .baseUri(BASE_URI)
            .header("Content-Type", "application/json")
            .body(payload)
        .when()
            .post("/user");

        // Print details (note: Surefire may capture stdout to target/surefire-reports by default)
        System.out.println("NEGATIVE CREATE - Status: " + response.getStatusLine());
        System.out.println("NEGATIVE CREATE - Body:\n" + response.asPrettyString());
    }

    @When("I login with a non-existing user")
    public void i_login_with_a_non_existing_user() {
        String nonExisting = "nonexistent_" + System.currentTimeMillis();
        response = given()
            .baseUri(BASE_URI)
            .queryParam("username", nonExisting)
            .queryParam("password", "wrongpass")
        .when()
            .get("/user/login");

        System.out.println("NEGATIVE LOGIN - Username: " + nonExisting);
        System.out.println("NEGATIVE LOGIN - Status: " + response.getStatusLine());
        System.out.println("NEGATIVE LOGIN - Body:\n" + response.asPrettyString());
    }

    @When("I get user by username {string}")
    public void i_get_user_by_username(String user) {
        response = given()
            .baseUri(BASE_URI)
            .pathParam("username", user)
        .when()
            .get("/user/{username}");

        System.out.println("NEGATIVE GET - Username: " + user);
        System.out.println("NEGATIVE GET - Status: " + response.getStatusLine());
        System.out.println("NEGATIVE GET - Body:\n" + response.asPrettyString());
    }

    @Then("negative response status should be {int}")
    public void response_status_should_be(int expected) {
        assertEquals(
                "Unexpected status. Body:\n" + (response != null ? response.asPrettyString() : "<no response>"),
                expected,
                response.getStatusCode()
        );
    }

    @Then("error message should contain {string}")
    public void error_message_should_contain(String expected) {
        String msg = response.jsonPath().getString("message");
        assertTrue("Expected message to contain: " + expected + " but was: " + msg,
                msg != null && msg.contains(expected));
    }
}
