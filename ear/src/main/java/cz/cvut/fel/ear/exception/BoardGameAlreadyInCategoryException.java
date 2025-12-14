package cz.cvut.fel.ear.exception;

public class BoardGameAlreadyInCategoryException extends RuntimeException implements ItemAlreadyInSourceI{
    public BoardGameAlreadyInCategoryException() {
        super("BoardGame already exists in category");
    }

    @Override
    public String getItem() {
        return "BoardGame";
    }

    @Override
    public String getSource() {
        return "Category";
    }
}
