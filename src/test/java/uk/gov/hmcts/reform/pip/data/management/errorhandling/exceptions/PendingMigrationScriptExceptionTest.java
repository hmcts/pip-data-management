package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PendingMigrationScriptExceptionTest {

    private static final String SCRIPT_MESSAGE = "V1_Script";

    @Test
    void testPendingMigrationScriptException() {
        PendingMigrationScriptException pendingMigrationScriptException =
            new PendingMigrationScriptException(SCRIPT_MESSAGE);

        assertEquals("Found migration not yet applied: " + SCRIPT_MESSAGE,
                     pendingMigrationScriptException.getMessage(),
                     "Exception message does not match expected message");
    }
}
