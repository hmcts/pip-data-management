package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Matter {
    private String sittingStartTime;
    private PartyInfo partyInfo;
    private MatterMetadata matterMetadata;
    private List<Offence> offences;
}
