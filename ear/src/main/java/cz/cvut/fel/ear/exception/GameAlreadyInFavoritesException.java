package cz.cvut.fel.ear.exception;

public class GameAlreadyInFavoritesException extends RuntimeException {
    /**
     * Called when user tries to add a game to favorites that is already in favorites
     * @param message more detailed message about the exception
     */
    public GameAlreadyInFavoritesException(String message) {
        super(message);
    }
}
