package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist;

import lombok.Data;

import java.util.List;

@Data
public class CourtRoom {
    String name;
    List<Sitting> listOfSittings;
}
