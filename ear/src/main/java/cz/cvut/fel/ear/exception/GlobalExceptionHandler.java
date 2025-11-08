package cz.cvut.fel.ear.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


}
