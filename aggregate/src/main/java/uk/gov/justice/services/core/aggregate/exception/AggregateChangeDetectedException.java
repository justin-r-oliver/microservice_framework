package uk.gov.justice.services.core.aggregate.exception;

public class AggregateChangeDetectedException extends Exception {
    private static final long serialVersionUID = 5934757852541650746L;

    public AggregateChangeDetectedException(final String message) {
        super(message);
    }
}
