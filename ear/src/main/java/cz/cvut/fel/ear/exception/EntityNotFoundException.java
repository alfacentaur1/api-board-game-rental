package cz.cvut.fel.ear.exception;

public class EntityNotFoundException extends RuntimeException {
    /**
     * Called when an entity is not found in the database
     * @param message more detailed message about the exception
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
