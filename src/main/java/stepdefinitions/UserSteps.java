package stepdefinitions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import utils.Excel;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class UserSteps {
   private static final Logger log = LoggerFactory.getLogger(UserSteps.class);
   Response response;
   String username;
String password;
   Map<String, Object> payload;

   @Given("I have user payload from Excel {string} in sheet {string}")
public void createUserPayloadFromExcel(String testCaseId, String sheetName) {
    List<Map<String, String>> rows = Excel.readSheetAsMaps("testdata/Api_Automation_DataSheet.xlsx", sheetName);
    Map<String, String> data = Excel.firstBy(rows, "TestCaseId", testCaseId)
        .orElseThrow(() -> new IllegalArgumentException("No Excel row with TestCaseId=" + testCaseId));

    payload = new HashMap<>();
    long id = data.getOrDefault("id", "").isBlank() ? System.currentTimeMillis()
            : Long.parseLong(data.get("id"));
    username = data.getOrDefault("username", "user_" + id);
    password = data.getOrDefault("password", "12345");

    payload.put("id", id);
    payload.put("username", username);
    payload.put("firstName", data.getOrDefault("firstName", "John"));
    payload.put("lastName", data.getOrDefault("lastName", "Doe"));
    payload.put("email", data.getOrDefault("email", "john@test.com"));
    payload.put("password", password);
    payload.put("phone", data.getOrDefault("phone", "9999999999"));
    payload.put("userStatus", Integer.parseInt(data.getOrDefault("userStatus", "1")));
}

//    @Given("I have valid user payload")
//    public void createUserPayload() {
//     payload = new HashMap<>();
//     long id = System.currentTimeMillis();
//     username = "testuser_" + id;
//     password = "12345";
//     payload.put("id", id);
//     payload.put("username", username);
//     payload.put("firstName", "John");
//     payload.put("lastName", "Doe");
//     payload.put("email", "john@test.com");
//     payload.put("password", password);
//     payload.put("phone", "9999999999");
//     payload.put("userStatus", 1);
//    }
   @When("I send POST request to create user")
   public void sendPostRequest() {
       baseURI = "https://petstore.swagger.io/v2";
       response =
               given()
                       .header("Content-Type", "application/json")
                       .body(payload)
               .when()
                        .post("/user");
                        // Log response details
    log.info("Status line: {}", response.getStatusLine());
    log.info("Content-Type: {}", response.getContentType());
    log.info("Response body:\n{}", response.asPrettyString());
   }
   @Then("response status should be 200")
   public void validateStatusCode() {
    log.info("Status code: {}", response.getStatusCode());
    log.debug("Body:\n{}", response.asPrettyString());
       assertEquals(200, response.getStatusCode());
   }
   @And("response content type should be JSON")
   public void validateContentType() {
       assertTrue("Content-Type should be application/json", response.getContentType().contains("application/json"));
   }
   @And("response should contain success message")
   public void validateResponseBody() {
       assertNotNull("Response message should not be null", response.jsonPath().get("message"));
       //assertEquals(response.jsonPath().get("code"), 200);
   }


   @When("I login with the created username and password")
    public void loginWithCreatedUser() {
    response = given()
        .baseUri("https://petstore.swagger.io/v2")
        .queryParam("username", username)
        .queryParam("password", password)
    .when()
        .get("/user/login");

        log.info("username is {}", username);
}

@Then("login should be successful with status 200")
public void loginStatusOk() {
     log.info("Status code: {}", response.getStatusCode());
    assertEquals(200, response.getStatusCode());
}

@Then("login response message should contain {string}")
public void loginMessageContains(String expected) {
    String msg = response.jsonPath().getString("message");
    // Petstore returns something like "logged in user session:xxxxx"
    assertTrue("Actual message: " + msg, msg != null && msg.contains(expected));
}

@When("I get user by the created username")
public void getUserByCreatedUsername() {
    response = given()
        .baseUri("https://petstore.swagger.io/v2")
        .pathParam("username", username)
    .when()
        .get("/user/{username}");
        log.info("GET user response:\n{}", response.asPrettyString());
}

@Then("user response should contain fields id, username, email, userStatus")
public void userResponseHasFields() {
    assertNotNull("id should be present", response.jsonPath().get("id"));
    assertNotNull("username should be present", response.jsonPath().get("username"));
    assertNotNull("email should be present", response.jsonPath().get("email"));
    assertNotNull("userStatus should be present", response.jsonPath().get("userStatus"));

}

@Then("user response fields should match the created payload")
public void userResponseMatchesPayload() {
    assertEquals("Username mismatch", username, response.jsonPath().getString("username"));
    assertEquals("Email mismatch", payload.get("email"), response.jsonPath().getString("email"));
    assertEquals("userStatus mismatch", (int) payload.get("userStatus"), response.jsonPath().getInt("userStatus"));
    // Optional: id should match the created one
    assertEquals("id mismatch", ((Number) payload.get("id")).longValue(), response.jsonPath().getLong("id"));
}
}
