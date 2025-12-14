package cz.cvut.fel.ear.exception;

public class InvalidLoanStateChangeException extends RuntimeException {
    private long loanId;
    private String currentState;
    private String newState;

    public InvalidLoanStateChangeException(long loanId, String currentState, String newState) {
        super("Loan with id: %d cannot change state from %s to %s".formatted(loanId, currentState, newState));
        this.loanId = loanId;
        this.currentState = currentState;
        this.newState = newState;
    }

    public long getLoanId() {
        return loanId;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getNewState() {
        return newState;
    }
}
