package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownfirmpddalist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrownFirmPddaList {
    private String sittingDate;
    private String courtName;
    private String courtAddress;
    private String courtPhone;
    private List<SittingInfo> sittings;
}
