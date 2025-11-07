package cz.cvut.fel.ear.exception;

public class EntityAlreadyExistsException extends RuntimeException {
    /**
     * Called when an entity with the same identifier already exists
     * @param message more detailed message about the exception
     */
    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}
