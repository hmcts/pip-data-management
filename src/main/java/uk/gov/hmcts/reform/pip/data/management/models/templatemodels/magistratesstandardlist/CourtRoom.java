package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourtRoom {

    private String courtRoomName;
    private String courtHouseName;
    private String lja;
    private List<GroupedPartyMatters> groupedPartyMatters = new ArrayList<>();

}
