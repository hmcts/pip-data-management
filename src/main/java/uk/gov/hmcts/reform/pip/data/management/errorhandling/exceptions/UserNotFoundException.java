package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

import java.util.UUID;

/**
 * This exception handles the scenario where a user has been passed in that does not exist.
 */
public class UserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -2286225865951086823L;

    public UserNotFoundException(UUID uuid) {
        super("User has not been found with ID: " + uuid);
    }

}
