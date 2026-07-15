package uk.gov.hmcts.reform.pip.data.management.models.templatemodels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.Offence;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MagistratesStandardList {
    private String courtHouseName;
    private String lja;
    private String courtRoomName;
    private String sittingHeading;
    private String name;
    private String applicationParticulars;
    private String dob;
    private String age;
    private String address;
    private String prosecutingAuthority;
    private String attendanceMethod;
    private String reference;
    private String applicationType;
    private String asn;
    private String hearingType;
    private String panel;
    private String reportingRestrictionDetails;
    private List<Offence> offences;
}
