package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.Event;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CourtEventGlossaryServiceTest {

    private static final String ADJOURNED = "Adjourned";
    private static final String APPEAL_INTERPRETER_SWORN = "Appeal Interpreter Sworn";
    private static final String APPEAL_WITNESS_CONTINUES = "Appeal Witness continues";
    private static final String SUCCESS_MESSAGE = "Events should be returned";

    private List<Event> courtEvents;

    @Autowired
    private CourtEventGlossaryService courtEventGlossaryService;

    @BeforeEach
    void setup() {

        courtEvents = courtEventGlossaryService.getAllCourtEventGlossary();
    }

    @Test
    void testGetAllCourtsReturnsAlphabetised() {
        assertEquals(ADJOURNED, courtEvents.get(0).getEventName(), SUCCESS_MESSAGE);
        assertEquals(APPEAL_INTERPRETER_SWORN, courtEvents.get(1).getEventName(), SUCCESS_MESSAGE);
        assertEquals(APPEAL_WITNESS_CONTINUES, courtEvents.get(2).getEventName(), SUCCESS_MESSAGE);
    }
}
