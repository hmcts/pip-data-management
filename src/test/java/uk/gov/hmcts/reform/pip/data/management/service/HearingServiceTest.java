package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HearingNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class HearingServiceTest {

    @Autowired
    private HearingService hearingService;

    @Test
    void testGetHearingsReturnsExpected() {
        assertEquals(3, hearingService.getHearings(1).size(), "Number of hearings should match");
    }

    @Test
    void testNoHearingsThrowsNotFoundException() {
        Exception ex = assertThrows(HearingNotFoundException.class, () ->
                                        hearingService.getHearings(5), "Expect HearingNotFound to be thrown"
        );
        assertEquals("No hearings found for court id: 5", ex.getMessage(), "Messages should match");
    }
}
