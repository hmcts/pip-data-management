package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LiveCaseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class LiveCaseStatusServiceTest {

    @Autowired
    private LiveCaseStatusService liveCaseStatusService;

    @Test
    void testHandleLiveCaseRequest() {
        assertEquals("Mutsu Court", liveCaseStatusService.handleLiveCaseRequest(1).get(0)
            .getCourtName(), "Court names should match");
    }

    @Test
    void testLiveCaseStatusExceptionThrown() {
        LiveCaseStatusException ex = assertThrows(LiveCaseStatusException.class, () ->
            liveCaseStatusService.handleLiveCaseRequest(2), "Expected LiveCaseStatusException to be thrown");
        assertEquals("No live cases found for court id: 2", ex.getMessage(), "Messages should match");
    }
}
