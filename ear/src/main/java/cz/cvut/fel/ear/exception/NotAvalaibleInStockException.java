package cz.cvut.fel.ear.exception;

public class NotAvalaibleInStockException extends RuntimeException {
    /**
     * Called when requested board game in not available in stock
     * @param message more detailed message about the exception
     */
    public NotAvalaibleInStockException(String message) {
        super(message);
    }
}
