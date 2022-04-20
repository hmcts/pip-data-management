package uk.gov.hmcts.reform.pip.data.management.config.flyway;

/**
 * Exception class which captures Flyway exceptions.
 */
public class PendingMigrationScriptException extends RuntimeException {

    private static final long serialVersionUID = -7333237701605622780L;

    public PendingMigrationScriptException(String script) {
        super("Found migration not yet applied: " + script);
    }

}
