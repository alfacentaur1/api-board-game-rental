package cz.cvut.fel.ear.exception;

public class CategoryAlreadyExistsException extends EntityAlreadyExistsException{
    public CategoryAlreadyExistsException(String categoryName) {
        super("Category", categoryName);
    }
}
