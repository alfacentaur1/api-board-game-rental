package cz.cvut.fel.ear.exception;

public class GameAlreadyInFavoritesException extends RuntimeException {
    public GameAlreadyInFavoritesException(String message) {
        super(message);
    }
}
