package uk.gov.hmcts.reform.pip.data.management.models.publication;

import lombok.Getter;

/**
 * Enum that represents the different list types.
 */
@Getter
public enum ListType {
    SJP_PUBLIC_LIST(true),
    SJP_PRESS_LIST(true),
    CROWN_DAILY_LIST,
    CROWN_FIRM_LIST,
    CROWN_WARNED_LIST,
    MAGS_PUBLIC_LIST,
    MAGS_STANDARD_LIST,
    CIVIL_DAILY_CAUSE_LIST,
    FAMILY_DAILY_CAUSE_LIST;

    /**
     * Flag that represents whether the list type is SJP.
     */
    private final boolean isSjp;

    ListType(boolean isSjp) {
        this.isSjp = isSjp;
    }

    ListType() {
        this.isSjp = false;
    }

}
