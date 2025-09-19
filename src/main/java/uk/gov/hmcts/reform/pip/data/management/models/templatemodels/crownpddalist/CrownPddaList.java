package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrownPddaList {
    private String sittingDate;
    private String courtName;
    private List<String> courtAddress;
    private String courtPhone;
    private List<SittingInfo> sittings;
}
