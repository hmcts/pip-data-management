package uk.gov.hmcts.reform.pip.data.management.models.templatemodels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MagistratesPublicList {
    private String courtHouse;
    private String courtRoom;
    private String sittingAt;
    private String urn;
    private String name;
    private String hearingType;
    private String prosecutingAuthority;
    private String offence;
    private String reportingRestriction;
}
