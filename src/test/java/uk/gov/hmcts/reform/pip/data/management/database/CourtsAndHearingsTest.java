package uk.gov.hmcts.reform.pip.data.management.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CourtsAndHearingsTest {

    private CourtsAndHearings courtsAndHearings;

    @BeforeEach
    public void setup() throws IOException {
        courtsAndHearings = new CourtsAndHearings();
    }

    @Test
    public void testGetListCourt() {
        assertEquals(4, courtsAndHearings.getListCourts().size(), "Court list size should match");
    }

    @Test
    public void testBuildCourts() {
        assertEquals(3, courtsAndHearings.getListCourts().get(0).getHearings(),
                     "Number of hearings in court should match");
    }

    @Test
    public void testGetHearingsByCourtId() {
        assertEquals(3, courtsAndHearings.getListHearings(1).size(),
                     "Number of hearings should match");
    }
}
