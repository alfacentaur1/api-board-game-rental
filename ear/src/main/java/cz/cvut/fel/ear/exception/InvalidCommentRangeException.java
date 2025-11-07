package cz.cvut.fel.ear.exception;

public class InvalidCommentRangeException extends RuntimeException {
    /**
     * Called when a comment's rating is out of the valid range
     * @param message more detailed message about the exception
     */
    public InvalidCommentRangeException(String message) {
        super(message);
    }
}
