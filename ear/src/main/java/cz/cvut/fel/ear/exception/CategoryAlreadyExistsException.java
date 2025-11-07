package cz.cvut.fel.ear.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
    /**
     * Called when a category with the same name already exists
     * @param message more detailed message about the exception
     */
    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
}
