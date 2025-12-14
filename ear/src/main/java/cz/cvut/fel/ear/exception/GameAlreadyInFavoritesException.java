package cz.cvut.fel.ear.exception;

public class GameAlreadyInFavoritesException extends RuntimeException implements ItemAlreadyInSourceI{
    public GameAlreadyInFavoritesException() {
        super("BoardGame already exist in user favourites");
    }

    @Override
    public String getItem() {
        return "BoardGame";
    }

    @Override
    public String getSource() {
        return "Favourites";
    }
}
