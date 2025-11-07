package cz.cvut.fel.ear.model;

/**
 * Enum representing the status of a loan in the lending system.
 */
public enum LoanStatus {
    RETURNED_IN_TIME,
    RETURNED_LATE,
    REJECTED,
    APPROVED,
    PENDING
}
