package cz.cvut.fel.ear.exception;

public class BoardGameAlreadyInCategoryException extends RuntimeException {
    public BoardGameAlreadyInCategoryException(String message) {
        super(message);
    }
}
