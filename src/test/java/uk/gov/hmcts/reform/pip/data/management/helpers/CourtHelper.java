package uk.gov.hmcts.reform.pip.data.management.helpers;

import uk.gov.hmcts.reform.pip.data.management.models.Hearing;

import java.util.ArrayList;
import java.util.List;

public final class CourtHelper {

    private CourtHelper() {
    }

    public static List<Hearing> createHearing() {
        Hearing hearing1 = new Hearing();
        hearing1.setCourtId(2);

        Hearing hearing2 = new Hearing();
        hearing1.setCourtId(4);

        List<Hearing> hearingList = new ArrayList<>();
        hearingList.add(hearing1);
        hearingList.add(hearing2);
        return hearingList;
    }

}
