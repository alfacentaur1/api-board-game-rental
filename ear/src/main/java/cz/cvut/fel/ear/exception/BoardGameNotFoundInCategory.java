package cz.cvut.fel.ear.exception;

public class BoardGameNotFoundInCategory extends RuntimeException {
    /**
     * Called when a board game is not found in specified category
     * @param message more detailed message about the exception
     */
    public BoardGameNotFoundInCategory(String message) {
        super(message);
    }
}
