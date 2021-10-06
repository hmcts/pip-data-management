package uk.gov.hmcts.reform.pip.data.management.helpers;

import uk.gov.hmcts.reform.pip.data.management.models.lcsu.LiveCaseStatus;

import java.util.ArrayList;
import java.util.List;

public final class LiveCaseHelper {

    private LiveCaseHelper() {

    }

    public static List<LiveCaseStatus> createMockLiveCaseList() {
        LiveCaseStatus lcsu = new LiveCaseStatus();
        lcsu.setCourtId(1);
        List<LiveCaseStatus> returnedList = new ArrayList<>();
        returnedList.add(lcsu);
        return returnedList;
    }
}
