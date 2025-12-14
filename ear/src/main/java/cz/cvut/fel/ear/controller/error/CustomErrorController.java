package cz.cvut.fel.ear.controller.error;

import cz.cvut.fel.ear.controller.response.ResponseWrapper;
import cz.cvut.fel.ear.controller.response.ResponseWrapper.ResponseInfoCode;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller responsible for handling global application errors.
 * This controller intercepts requests forwarded to the "/error" path (default Spring Boot error path)
 * and provides a standardized JSON response instead of the default "Whitelabel Error Page".
 * It serves as a fallback for errors that occur outside of standard Controller execution,
 * such as malformed URLs (400), non-existent endpoints (404), or internal server errors (500).
 */
@RestController
public class CustomErrorController implements ErrorController {

    /**
     * Handles the error request and constructs a standardized JSON response.
     *
     * @param request the HttpServletRequest containing error attributes (e.g., status code)
     * @return a ResponseEntity containing the standardized ResponseWrapper data and the corresponding HTTP status
     */
    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        // Retrieve the HTTP status code from the request attributes
        Object statusAttribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        // Default to INTERNAL_SERVER_ERROR if status is missing
        int statusCode = (statusAttribute != null)
                ? Integer.parseInt(statusAttribute.toString())
                : HttpStatus.INTERNAL_SERVER_ERROR.value();

        ResponseWrapper generator = new ResponseWrapper();

        if (statusCode == HttpStatus.BAD_REQUEST.value()) {
            // Handles 400 - e.g., malformed URL (//), missing parameters, etc.
            generator.setResponseInfoMessage(ResponseInfoCode.ERROR_INVALID_REQUEST_FORMAT);
            return buildResponse(generator, HttpStatus.BAD_REQUEST);

        } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
            // Handles 404 - endpoint does not exist
            generator.setResponseInfoMessage(ResponseInfoCode.ERROR_ENDPOINT_NOT_FOUND);
            return buildResponse(generator, HttpStatus.NOT_FOUND);

        } else if (statusCode == HttpStatus.FORBIDDEN.value() || statusCode == HttpStatus.UNAUTHORIZED.value()) {
            // Handles 401/403 - Security exceptions that fell through to the error page
            generator.setResponseInfoMessage(ResponseInfoCode.DENIED_AUTHORIZATION);
            return buildResponse(generator, HttpStatus.valueOf(statusCode));

        } else {
            // Fallback for 500 and other unexpected errors
            generator.setResponseInfoMessage(ResponseInfoCode.ERROR_INTERNAL_SERVER_ERROR);
            return buildResponse(generator, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Helper method to build the final ResponseEntity from the ResponseWrapper.
     *
     * @param wrapper the populated ResponseWrapper instance
     * @param status  the HTTP status to return
     * @return the complete ResponseEntity with the map body
     */
    private ResponseEntity<Map<String, Object>> buildResponse(ResponseWrapper wrapper, HttpStatus status) {
        return new ResponseEntity<>(wrapper.getResponse(), status);
    }

    /**
     * Returns the path of the error page.
     *
     * @return the error path
     */
    public String getErrorPath() {
        return "/error";
    }
}