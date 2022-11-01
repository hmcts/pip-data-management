package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Flyway exception class for migrations when they've not been applied.
 */
public class PendingMigrationScriptException extends RuntimeException {

    private static final long serialVersionUID = 5681614138563249144L;

    public PendingMigrationScriptException(String script) {
        super("Found migration not yet applied: " + script);
    }
}
