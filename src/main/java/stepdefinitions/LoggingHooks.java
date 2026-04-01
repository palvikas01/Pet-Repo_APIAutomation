package stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Slf4jRestAssuredFilter;
import net.serenitybdd.core.Serenity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Global Cucumber hooks to initialize REST logging and add scenario boundary logs.
 * Placed in the 'stepdefinitions' package so it is picked up by the existing glue.
 */
public class LoggingHooks {

    private static final Logger log = LoggerFactory.getLogger(LoggingHooks.class);

    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        // Set a global Rest-Assured filter that logs requests/responses to SLF4J
        RestAssured.filters(new Slf4jRestAssuredFilter());

        // Mark scenario start in logs
        log.info("=== Starting scenario: {} ===", scenario.getName());
    }

    @After(order = 0)
    public void afterScenario(Scenario scenario) {
        // Mark scenario completion in logs
        log.info("=== Finished scenario: {} | failed={} ===", scenario.getName(), scenario.isFailed());

        // Attach execution log to Serenity report (if present)
        try {
            Path logFile = Paths.get("target", "logs", "tests.log");
            if (Files.exists(logFile)) {
                String content = Files.readString(logFile);
                Serenity.recordReportData()
                        .withTitle("Execution log")
                        .andContents(content);
            }
        } catch (Exception e) {
            log.warn("Could not attach execution log to Serenity report", e);
        }
    }
}
