# Project Logging Guide

This project now uses SLF4J (API) with Logback (implementation) for consistent, configurable logging across Cucumber/Serenity tests and Rest-Assured HTTP calls.

## What was added

1. Dependencies (pom.xml)
   - org.slf4j:slf4j-api:2.0.13
   - ch.qos.logback:logback-classic:1.4.14

2. Logback configuration (tests)
   - File: src/test/resources/logback-test.xml
   - Outputs:
     - Console with concise pattern
     - Rolling file target/logs/tests.log (daily rollover, 7-day history)
   - Library log levels:
     - io.restassured → WARN (reduce noise)
     - org.apache.http → WARN
     - net.serenitybdd → INFO
     - root → INFO

3. Rest-Assured SLF4J filter:
   - File: src/main/java/utils/Slf4jRestAssuredFilter.java
   - Logs requests/responses via SLF4J
     - INFO: request method/URI and response status
     - DEBUG: headers, cookies, and pretty-printed bodies

4. Global Cucumber hooks:
   - File: src/main/java/stepdefinitions/LoggingHooks.java
   - Registers the Rest-Assured SLF4J filter for every scenario
   - Adds scenario start/finish boundary logs
   - Attaches the execution log (target/logs/tests.log) into each Serenity report as “Execution log”

5. Code updates:
   - Replaced System.out.println with SLF4J logging in:
     - stepdefinitions/Store.java
     - stepdefinitions/UserSteps.java
     - stepdefinitions/NegativeUserSteps.java
     - stepdefinitions/PetSteps.java

## Using the logger in step classes

- Add imports:
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;

- Create a logger:
  private static final Logger log = LoggerFactory.getLogger(MyClass.class);

- Log messages:
  log.info("Message with {}", arg);
  log.debug("Payload:\n{}", jsonBody);
  log.warn("Something unexpected: {}", details);
  log.error("Failure", exception);

SLF4J placeholders {} are evaluated lazily and avoid string concatenation unless the level is enabled.

## Controlling verbosity (logback-test.xml)

- Change the root level:
  <root level="DEBUG">…</root> // more verbose
  <root level="INFO">…</root>  // default

- Increase details for specific packages/classes:
  <logger name="utils" level="DEBUG"/>
  <logger name="stepdefinitions" level="DEBUG"/>
  <logger name="io.restassured" level="INFO"/> <!-- to see request/response lines -->

- Reduce noise from libraries:
  <logger name="org.apache.http" level="WARN"/>

- Where to put configuration:
  - src/test/resources/logback-test.xml applies to test runs.
  - If you also need logging for main code outside tests, create src/main/resources/logback.xml.

## Rest-Assured logging

- Automatically enabled via the global hook: LoggingHooks.beforeScenario.
- INFO level:
  - REST REQUEST: <METHOD> <URI>
  - REST RESPONSE: <StatusLine> | code | contentType
- DEBUG level:
  - Request/response headers/cookies
  - Pretty-printed bodies

To see bodies, enable DEBUG for the utils package in logback-test.xml:
  <logger name="utils" level="DEBUG"/>

## Where to find the logs

- Console output (Maven/IDE console):
  Uses a compact pattern: time, thread, level, logger, message.

- File logs:
  target/logs/tests.log (rolled daily, keeps 7 days).

- Serenity report:
  After each scenario, logs are attached as “Execution log” in the HTML report:
  target/site/serenity/index.html

## Running tests to generate logs

- From the project root:
  mvn clean verify
  or
  mvn test

Serenity aggregates reports in target/site/serenity. The file logs are under target/logs.

## Tips

- Prefer parameterized logging over string concatenation.
- Log at INFO for high-level flow and key checkpoints.
- Use DEBUG for large payloads or diagnostics (and keep it disabled by default on CI to control log size).
- Keep secrets out of logs or mask them before logging.

## Troubleshooting

- No logs in console:
  - Ensure logback-test.xml is in src/test/resources.
  - Ensure only one SLF4J binding (logback-classic) is on the classpath.

- Duplicate or conflicting bindings:
  - Run mvn dependency:tree and check for other slf4j bindings (e.g., slf4j-simple). Exclude as needed.

- Serenity report doesn’t show “Execution log”:
  - Ensure target/logs/tests.log exists after a run.
  - Check LoggingHooks.afterScenario didn’t warn about failing to attach.
