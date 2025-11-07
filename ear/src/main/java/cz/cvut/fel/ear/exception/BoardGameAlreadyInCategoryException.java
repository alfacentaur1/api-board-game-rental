package cz.cvut.fel.ear.exception;

public class BoardGameAlreadyInCategoryException extends RuntimeException {
    /**
     * Called when a board game is already in the category
     * @param message more detailed message about the exception
     */
    public BoardGameAlreadyInCategoryException(String message) {
        super(message);
    }
}
