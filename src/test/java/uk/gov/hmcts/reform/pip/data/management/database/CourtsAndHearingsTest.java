package uk.gov.hmcts.reform.pip.data.management.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CourtsAndHearingsTest {

    private CourtsAndHearings courtsAndHearings;

    @BeforeEach
    void setup() {
        courtsAndHearings = new CourtsAndHearings();
    }


    @Test
    void testGetHearingsByCourtId() {
        assertEquals(3, courtsAndHearings.getListHearings(1).size(),
                     "Number of hearings should match");
    }

    @Test
    void testGetListCourtEventGlossary() {
        assertEquals(49, courtsAndHearings.getListCaseEventGlossary().size(),
                     "Case list event glossary size should match");
    }
}
