package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratespubliclist;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CourtRoom {
    private String formattedCourtRoomName;
    private List<Sitting> sittings = new ArrayList<>();
}
