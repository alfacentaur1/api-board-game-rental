package cz.cvut.fel.ear.controller.handler;

import cz.cvut.fel.ear.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<Map<String,String>> buildErrorResponse(String message, HttpStatus status){
        Map<String,String> errorBody = new HashMap<>();
        errorBody.put("error", message);
        return ResponseEntity.status(status).body(errorBody);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> handleIllegalArgumentException(IllegalArgumentException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BoardGameNotFoundInCategory.class)
    public ResponseEntity<Map<String,String>> handleBoardGameNotFoundInCategory(BoardGameNotFoundInCategory exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BoardGameAlreadyInCategoryException.class)
    public ResponseEntity<Map<String,String>> handleBoardGameAlreadyInCategoryException(BoardGameAlreadyInCategoryException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<Map<String,String>> handleCategoryAlreadyExistsException(CategoryAlreadyExistsException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<Map<String,String>> handleEntityAlreadyExistsException(EntityAlreadyExistsException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleEntityNotFoundException(EntityNotFoundException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GameAlreadyInFavoritesException.class)
    public ResponseEntity<Map<String,String>> handleGameAlreadyInFavoritesException(GameAlreadyInFavoritesException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCommentRangeException.class)
    public ResponseEntity<Map<String,String>> handleInvalidCommentRangeException(InvalidCommentRangeException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<Map<String,String>> handleInvalidDateException(InvalidDateException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRatingScoreException.class)
    public ResponseEntity<Map<String,String>> handleInvalidRatingScoreException(InvalidRatingScoreException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotAvalaibleInStockException.class)
    public ResponseEntity<Map<String,String>> handleNotAvailableInStockException(NotAvalaibleInStockException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ParametersException.class)
    public ResponseEntity<Map<String,String>> handleParametersException(ParametersException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<Map<String,String>> handleInvalidStatusException(InvalidStatusException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleUsernameNotFoundException(UsernameNotFoundException exception){
        return buildErrorResponse(exception.getMessage(),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException exception) {
        return buildErrorResponse(exception.getMessage(), HttpStatus.CONFLICT);
    }

    //handlers for spring security exceptions - authentication and authorization
    @ExceptionHandler({
            org.springframework.security.access.AccessDeniedException.class,
            org.springframework.security.authorization.AuthorizationDeniedException.class
    })
    public ResponseEntity<Map<String, String>> handleAccessDenied(Exception ex) {

        return buildErrorResponse("Access denied.", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(Exception exception) {

        return buildErrorResponse("Authentication error. " + exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    //general exception handler - fallback for unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception exception) {
        exception.printStackTrace();

        return buildErrorResponse("Error: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }




}
