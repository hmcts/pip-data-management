package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SittingInfo {
    private String courtRoomNumber;
    private String sittingAt;
    private String judgeName;
    private List<HearingInfo> hearings;
}
