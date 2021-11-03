package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.EventGlossary;
import uk.gov.hmcts.reform.pip.data.management.service.CourtEventGlossaryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.EventGlossaryHelper.createMockEventList;

@SpringBootTest
class CourtEventGlossaryControllerTest {

    private List<EventGlossary> allEvents;

    @Mock
    private CourtEventGlossaryService courtEventGlossaryService;

    @InjectMocks
    private CourtEventGlossaryController courtEventGlossaryController;

    @BeforeEach
    void setup() {
        allEvents = createMockEventList();

        when(courtEventGlossaryService.getAllCourtEventGlossary()).thenReturn(allEvents);
    }

    @Test
    void testGetCourtEventStatusListReturnsAllEvents() {
        assertEquals(allEvents, courtEventGlossaryController.getCourtEventStatusList().getBody(),
                     "Should contain all court event statuses");
    }

    @Test
    void testGetCourtEventStatusListReturnsOk() {
        assertEquals(HttpStatus.OK, courtEventGlossaryController.getCourtEventStatusList().getStatusCode(),
                     "Event code should match");
    }
}
