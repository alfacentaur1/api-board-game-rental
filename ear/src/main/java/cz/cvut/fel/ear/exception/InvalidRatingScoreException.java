package cz.cvut.fel.ear.exception;

public class InvalidRatingScoreException extends RuntimeException {
    /**
     * Called when a rating score is outside the valid range
     * @param message more detailed message about the exception
     */
    public InvalidRatingScoreException(String message) {
        super(message);
    }
}
