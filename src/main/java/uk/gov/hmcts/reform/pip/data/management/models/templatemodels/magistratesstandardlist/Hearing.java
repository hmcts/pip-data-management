package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hearing {
    private String sittingStartTime;
    private PartyInfo partyInfo;
    private HearingMetadata hearingMetadata;
    private List<Offence> offences;
}
