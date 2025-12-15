package cz.cvut.fel.ear.exception;

public class NotAvalaibleInStockException extends RuntimeException {
    private String boardGameName;
    public NotAvalaibleInStockException(String boardGameName) {
        super("Board game %s has no available items to borrow".formatted(boardGameName));
        this.boardGameName = boardGameName;
    }

    public String getBoardGameName() {
        return boardGameName;
    }
}
