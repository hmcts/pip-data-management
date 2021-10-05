package uk.gov.hmcts.reform.pip.data.management.models.lcsu;

import lombok.Data;

@Data
public class Updates {

    private int courtRoom;
    private String caseNumber;
    private String caseName;
    private Event event;
}
