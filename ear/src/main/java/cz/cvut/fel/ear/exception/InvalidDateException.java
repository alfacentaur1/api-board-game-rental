package cz.cvut.fel.ear.exception;

public class InvalidDateException extends RuntimeException {
    /**
     * Called when a provided date is invalid or does not meet the required criteria
     * @param message more detailed message about the exception
     */
    public InvalidDateException(String message) {
        super(message);
    }
}
