package uk.gov.hmcts.reform.pip.data.management.helpers;

import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;

import java.util.ArrayList;
import java.util.List;

public final class CourtHelper {

    private CourtHelper() {
    }

    public static Court createMockCourt(String courtName) {
        Court court = new Court();
        court.setName(courtName);
        court.setHearingList(createHearing());
        return court;
    }

    private static List<Hearing> createHearing() {
        Hearing hearing1 = new Hearing();
        hearing1.setCourtId(2);

        Hearing hearing2 = new Hearing();
        hearing1.setCourtId(4);

        List<Hearing> hearingList = new ArrayList<>();
        hearingList.add(hearing1);
        hearingList.add(hearing2);
        return hearingList;
    }

    public static List<Court> createMockCourtList() {
        List<Court> courts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Court court = createMockCourt(String.format("mock court %o", i + 1));
            court.setCourtId(i);
            court.setJurisdiction(i > 5 ? "Crown Court" : "Magistrates Court");
            court.setLocation(i > 7 ? "london" : "manchester");
            courts.add(court);
        }
        return courts;
    }

    public static List<Court> returnFilteredCourtsWhereResultsShouldBe2(int item) {
        List<Court> mockFilteredCourts = new ArrayList<>();
        mockFilteredCourts.add(createMockCourt(String.format("mock %o", item)));
        mockFilteredCourts.add(createMockCourt(String.format("mock %o", item + 1)));

        return mockFilteredCourts;
    }

    public static List<Court> returnFilteredCourtsWhereResultsShouldBe1() {
        List<Court> mockFilteredCourts = new ArrayList<>();
        mockFilteredCourts.add(createMockCourt("mock 1"));

        return mockFilteredCourts;
    }
}
