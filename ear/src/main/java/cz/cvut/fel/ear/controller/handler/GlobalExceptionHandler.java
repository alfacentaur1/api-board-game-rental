package cz.cvut.fel.ear.controller.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import cz.cvut.fel.ear.controller.response.ResponseWrapper;
import cz.cvut.fel.ear.controller.response.ResponseWrapper.ErrorMessageCode;
import cz.cvut.fel.ear.controller.response.ResponseWrapper.ResponseInfoCode;
import cz.cvut.fel.ear.exception.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.StreamSupport;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles @Valid on RequestBody DTOs
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleDtoValidation(MethodArgumentNotValidException exception) {
        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseInfoCode.ERROR_VALIDATION);

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            generator.addResponseInfoError(
                    resolveValidationCode(error.getCode()),
                    error.getField()
            );
        }
        return buildResponse(generator, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidLoanReturnException.class)
    public ResponseEntity<Map<String, Object>>handleInvalidLoanReturnState(InvalidLoanReturnException exception) {
        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseInfoCode.ERROR_VALIDATION);
        generator.addResponseInfoError(ErrorMessageCode.INVALID_LOAN_NOT_APPROVED, "loanId");

        return buildResponse(generator, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientKarmaException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientKarma(InsufficientKarmaException exception) {
        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseInfoCode.ERROR_VALIDATION);
        generator.addResponseInfoError(ErrorMessageCode.INVALID_FIELD_VALUE, "karma");
        return buildResponse(generator, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityReferenceException.class)
    public ResponseEntity<ResponseWrapper> handleEntityReferencedException(EntityReferenceException e) {

        ResponseWrapper response = new ResponseWrapper();
        response.setResponseInfoMessage(ResponseInfoCode.ERROR_RESOURCE_IN_USE);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }


    /**
     * Handles @Validated on Controller parameters
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleUrlValidation(ConstraintViolationException exception) {
        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseInfoCode.ERROR_VALIDATION);

        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String fieldName = extractFieldName(violation.getPropertyPath());
            String annotation = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();

            generator.addResponseInfoError(resolveValidationCode(annotation), fieldName);
        }
        return buildResponse(generator, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles JSON formatting errors
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonErrors(HttpMessageNotReadableException exception) {
        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseInfoCode.ERROR_VALIDATION);

        // Empty body check
        if (exception.getMessage() != null && exception.getMessage().startsWith("Required request body is missing")) {
            generator.addResponseInfoError(ErrorMessageCode.MISSING_BODY, "requestBody");
            return buildResponse(generator, HttpStatus.BAD_REQUEST);
        }

        Throwable cause = exception.getCause();

        // Invalid dataType
        if (cause instanceof InvalidFormatException ife) {
            String fieldName = extractJsonFieldName(ife.getPath());
            if (ife.getTargetType().isEnum()) {
                generator.addResponseInfoError(ErrorMessageCode.INVALID_FIELD_VALUE, fieldName);
            } else if (ife.getTargetType() == java.time.LocalDate.class) {
                generator.addResponseInfoError(ErrorMessageCode.INVALID_DATE_FORMAT, fieldName);
            } else {
                generator.addResponseInfoError(ErrorMessageCode.INVALID_FIELD_TYPE, fieldName);
            }
        }

        // Missing  field
        else if (cause instanceof MismatchedInputException mie) {
            String fieldName = extractJsonFieldName(mie.getPath());
            String msg = mie.getOriginalMessage();
            if (msg != null && (msg.contains("missing") || msg.contains("null"))) {
                generator.addResponseInfoError(ErrorMessageCode.MISSING_FIELD, fieldName);
            } else {
                generator.addResponseInfoError(ErrorMessageCode.INVALID_FIELD_TYPE, fieldName);
            }
        }

        // Syntax error
        else if (cause instanceof JsonParseException jpe) {
            handleJsonParseException(jpe, generator);
        }
        else {
            generator.setResponseInfoMessage(ResponseInfoCode.INVALID_BODY_FORMAT);
        }

        return buildResponse(generator, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles URL type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseInfoCode.ERROR_VALIDATION);
        generator.addResponseInfoError(ErrorMessageCode.INVALID_FIELD_TYPE, exception.getName());
        return buildResponse(generator, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseInfoCode.ERROR_METHOD_NOT_SUPPORTED, exception.getMethod());
        return buildResponse(generator, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(InvalidRatingScoreException.class)
    public ResponseEntity<Map<String, Object>> handleNumOutOfRange(InvalidRatingScoreException exception) {
        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseInfoCode.ERROR_VALIDATION);
        generator.addResponseInfoError(ErrorMessageCode.NUMBER_OUT_OF_RANGE, "rating");
        return buildResponse(generator, HttpStatus.BAD_REQUEST);
    }

    /**
     * Consolidates EntityNotFoundException and ItemNotInResource
     */
    @ExceptionHandler({EntityNotFoundException.class, ItemNotInResource.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(Exception exception) {
        ResponseWrapper generator = new ResponseWrapper();

        if (exception instanceof EntityNotFoundException e) {
            if (e.getResouce() == null) {
                generator.setResponseInfoMessage(ResponseInfoCode.ERROR_ITEM_NOT_FOUND, e.getItem());
            } else {
                generator.setResponseInfoMessage(ResponseInfoCode.ERROR_ITEM_NOT_IN_RESOURCE, e.getItem(), e.getResouce());
            }
        } else if (exception instanceof ItemNotInResource e) {
            generator.setResponseInfoMessage(ResponseInfoCode.ERROR_ITEM_NOT_IN_RESOURCE, e.getItem(), e.getResource());
        }

        return buildResponse(generator, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles Item already exists
     */
    @ExceptionHandler({
            EntityAlreadyExistsException.class,
            BoardGameAlreadyInCategoryException.class,
            GameAlreadyInFavoritesException.class
    })
    public ResponseEntity<Map<String, Object>> handleConflicts(Exception exception) {
        ResponseWrapper generator = new ResponseWrapper();

        if (exception instanceof EntityAlreadyExistsException e) {
            if (e.getResouce() == null) {
                generator.setResponseInfoMessage(ResponseInfoCode.ERROR_ALREADY_EXISTS, e.getItem());
            } else {
                generator.setResponseInfoMessage(ResponseInfoCode.ERROR_ITEM_ALREADY_EXISTS_IN_RESOURCE, e.getItem(), e.getResouce());
            }
        } else if (exception instanceof ItemAlreadyInSourceI e) {

            generator.setResponseInfoMessage(ResponseInfoCode.ERROR_ITEM_ALREADY_EXISTS_IN_RESOURCE, e.getItem(), e.getSource());
        }

        return buildResponse(generator, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException exception) {
        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseInfoCode.ERROR_ENDPOINT_NOT_FOUND);
        return buildResponse(generator, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationException(AuthorizationDeniedException exception) {
        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseInfoCode.DENIED_AUTHORIZATION);
        return buildResponse(generator, HttpStatus.FORBIDDEN);
    }
    /**
     * Handles other handler exceptions like missing parameters or type mismatches
     * e.g. when a required request parameter is missing or mapping is api// and not api/
     */
    @ExceptionHandler({
            org.springframework.beans.TypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.bind.ServletRequestBindingException.class
    }
    )
    public ResponseEntity<Map<String, Object>> handleBadRequestExceptions(Exception exception) {
        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseInfoCode.ERROR_INVALID_REQUEST_FORMAT);
        return buildResponse(generator, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catches all unhandled exceptions to prevent information leakage and code 500 responses
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return new ResponseEntity<>(ex.getMessage() + " : " + ex.getClass().getSimpleName(), HttpStatus.BAD_REQUEST);
    }


    private ResponseEntity<Map<String, Object>> buildResponse(ResponseWrapper generator, HttpStatus status) {
        return new ResponseEntity<>(generator.getResponse(), status);
    }

    private ErrorMessageCode resolveValidationCode(String annotation) {
        if (annotation == null) return ErrorMessageCode.INVALID_FIELD_VALUE;
        return switch (annotation) {
            case "NotNull", "NotEmpty", "NotBlank" -> ErrorMessageCode.MISSING_FIELD;
            case "Min", "Max", "DecimalMin", "DecimalMax", "Size", "Range" -> ErrorMessageCode.NUMBER_OUT_OF_RANGE;
            default -> ErrorMessageCode.INVALID_FIELD_VALUE;
        };
    }

    private String extractFieldName(Path path) {
        return StreamSupport.stream(path.spliterator(), false)
                .reduce((first, second) -> second)
                .map(Path.Node::getName)
                .orElse("unknown_field");
    }

    private String extractJsonFieldName(java.util.List<com.fasterxml.jackson.databind.JsonMappingException.Reference> path) {
        return (path != null && !path.isEmpty()) ? path.get(path.size() - 1).getFieldName() : "unknown_field";
    }

    private void handleJsonParseException(JsonParseException jpe, ResponseWrapper generator) {
        String fieldName = "JSON value";
        if (jpe.getProcessor() instanceof JsonParser parser) {
            try {
                String currentName = parser.getCurrentName();
                if (currentName != null) fieldName = currentName;
            } catch (Exception ignored) {}
        }

        String msg = jpe.getMessage();
        if (msg != null && (msg.contains("expected a valid value") || msg.contains("Unexpected character"))) {
            generator.addResponseInfoError(ErrorMessageCode.INVALID_FIELD_VALUE, fieldName);
        } else {
            generator.addResponseInfoError(ErrorMessageCode.INVALID_BODY_FORMAT, fieldName);
        }
    }
}