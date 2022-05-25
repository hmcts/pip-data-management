package uk.gov.hmcts.reform.pip.data.management.models.publication;

import lombok.Getter;
import uk.gov.hmcts.reform.pip.data.management.models.request.UserProvenances;

/**
 * Enum that represents the different list types.
 */
@Getter
public enum ListType {
    SJP_PUBLIC_LIST(true, UserProvenances.PI_AAD),
    SJP_PRESS_LIST(true, UserProvenances.PI_AAD),
    CROWN_DAILY_LIST(UserProvenances.CRIME_IDAM),
    CROWN_FIRM_LIST(UserProvenances.CRIME_IDAM),
    CROWN_WARNED_LIST(UserProvenances.CRIME_IDAM),
    MAGS_PUBLIC_LIST(UserProvenances.CRIME_IDAM),
    MAGS_STANDARD_LIST(UserProvenances.CRIME_IDAM),
    CIVIL_DAILY_CAUSE_LIST(UserProvenances.CFT_IDAM),
    FAMILY_DAILY_CAUSE_LIST(UserProvenances.CFT_IDAM);

    /**
     * Flag that represents whether the list type is SJP.
     */
    private final boolean isSjp;

    private final UserProvenances provenance;

    ListType(boolean isSjp, UserProvenances provenance) {
        this.isSjp = isSjp;
        this.provenance = provenance;
    }

    ListType(UserProvenances provenance) {
        this.isSjp = false;
        this.provenance = provenance;
    }

}
