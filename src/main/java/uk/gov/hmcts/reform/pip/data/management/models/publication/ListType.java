package uk.gov.hmcts.reform.pip.data.management.models.publication;

import lombok.Getter;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;

import java.util.List;

/**
 * Enum that represents the different list types.
 */
@Getter
public enum ListType {
    SJP_PUBLIC_LIST(LocationType.NATIONAL, List.of(Provenance.CRIME)),
    SJP_PRESS_LIST(LocationType.NATIONAL, List.of(Provenance.CRIME)),
    CROWN_DAILY_LIST(LocationType.VENUE, List.of(Provenance.CRIME)),
    CROWN_FIRM_LIST(LocationType.VENUE, List.of(Provenance.CRIME)),
    CROWN_WARNED_LIST(LocationType.VENUE, List.of(Provenance.CRIME)),
    MAGS_PUBLIC_LIST(LocationType.VENUE, List.of(Provenance.CRIME)),
    MAGS_STANDARD_LIST(LocationType.VENUE, List.of(Provenance.CRIME)),
    CIVIL_DAILY_CAUSE_LIST(LocationType.VENUE, List.of(Provenance.CIVIL)),
    FAMILY_DAILY_CAUSE_LIST(LocationType.VENUE, List.of(Provenance.FAMILY)),
    CIVIL_AND_FAMILY_DAILY_CAUSE_LIST(LocationType.VENUE, List.of(Provenance.CIVIL, Provenance.FAMILY)),
    COP_DAILY_CAUSE_LIST(LocationType.VENUE, List.of(Provenance.CIVIL, Provenance.FAMILY)),
    SSCS_DAILY_LIST(LocationType.REGION, List.of(Provenance.CIVIL, Provenance.FAMILY));

    /**
     * Flag that represents the Location Type level the list displays at.
     */
    private final LocationType listLocationLevel;
    private final List<Provenance> provenances;

    ListType(LocationType listLocationLevel, List<Provenance> provenances) {
        this.listLocationLevel = listLocationLevel;
        this.provenances = provenances;
    }

}
