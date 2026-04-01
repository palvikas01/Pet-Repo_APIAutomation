package utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rest-Assured filter that logs requests and responses to SLF4J.
 * - INFO: request line and response status
 * - DEBUG: headers/cookies and pretty-printed bodies
 *
 * Control verbosity via logback-test.xml, e.g.:
 *   <logger name="utils" level="DEBUG"/>  // to see bodies
 */
public class Slf4jRestAssuredFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(Slf4jRestAssuredFilter.class);

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {

        try {
            log.info("REST REQUEST: {} {}", requestSpec.getMethod(), requestSpec.getURI());
            if (requestSpec.getHeaders() != null && !requestSpec.getHeaders().asList().isEmpty()) {
                log.debug("Request headers: {}", requestSpec.getHeaders());
            }
            if (requestSpec.getCookies() != null && !requestSpec.getCookies().asList().isEmpty()) {
                log.debug("Request cookies: {}", requestSpec.getCookies());
            }
            Object body = requestSpec.getBody();
            if (body != null) {
                log.debug("Request body:\n{}", body);
            }
        } catch (Exception e) {
            log.warn("Failed to log request", e);
        }

        Response response = ctx.next(requestSpec, responseSpec);

        try {
            log.info("REST RESPONSE: {} | code={} | contentType={}",
                    response.getStatusLine(), response.getStatusCode(), response.getContentType());
            String respBody = null;
            try {
                respBody = response.getBody().asPrettyString();
            } catch (Exception ignored) {}
            if (respBody != null && !respBody.isEmpty()) {
                log.debug("Response body:\n{}", respBody);
            }
        } catch (Exception e) {
            log.warn("Failed to log response", e);
        }

        return response;
    }
}
