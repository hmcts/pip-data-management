package uk.gov.hmcts.reform.pip.data.management.models.request;

import lombok.Data;

import java.util.UUID;

/**
 * Model that represents a PI User.
 */
@Data
public class PiUser {

    /**
     * The ID of the user as they exist in P&I.
     */
    private UUID userId;

    /**
     * The Sign in entry system the user was added with. (CFT IDAM, Crime IDAM, P&I AAD).
     */
    private UserProvenances userProvenance;

    /**
     * The user id of the user as per their provenance system.
     */
    private String provenanceUserId;

    /**
     * Email of the user.
     */
    private String email;

    /**
     * Role of the user, Verified, Internal or Technical.
     */
    private Roles roles;
}
