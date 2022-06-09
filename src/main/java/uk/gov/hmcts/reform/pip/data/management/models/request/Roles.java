package uk.gov.hmcts.reform.pip.data.management.models.request;

/**
 * Enum of the roles allowed for users of P&I, verified are media members, Internal are different levels of admin,
 * and Technical are consumers of our service such as Courtel.
 */
public enum Roles {
    VERIFIED,
    INTERNAL_SUPER_ADMIN_CTSC,
    INTERNAL_SUPER_ADMIN_LOCAL,
    INTERNAL_ADMIN_CTSC,
    INTERNAL_ADMIN_LOCAL,
    TECHNICAL
}
