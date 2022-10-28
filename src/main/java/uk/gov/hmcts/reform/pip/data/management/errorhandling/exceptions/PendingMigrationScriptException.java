package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * Flyway exception class for migrations when they've not bee applied
 */
public class PendingMigrationScriptException extends RuntimeException {

    public PendingMigrationScriptException(String script) {
        super("Found migration not yet applied: " + script);
    }
}
