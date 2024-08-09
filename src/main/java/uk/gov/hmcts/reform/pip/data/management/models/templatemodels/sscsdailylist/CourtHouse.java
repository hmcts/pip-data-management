package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourtHouse {
    String name;
    String phone;
    String email;
    List<CourtRoom> listOfCourtRooms;
}
