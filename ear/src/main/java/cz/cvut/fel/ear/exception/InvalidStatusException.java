package cz.cvut.fel.ear.exception;

public class InvalidStatusException extends RuntimeException {
    /**
     * Called when an invalid loan status is encountered
     * @param message more detailed message about the exception
     */
    public InvalidStatusException(String message) {
        super(message);
    }
}
