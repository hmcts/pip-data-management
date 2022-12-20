package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.CaseEventGlossary;
import uk.gov.hmcts.reform.pip.data.management.service.CaseEventGlossaryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.EventGlossaryHelper.createMockEventList;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class CaseEventGlossaryControllerTest {

    private List<CaseEventGlossary> allEvents;

    @Mock
    private CaseEventGlossaryService caseEventGlossaryService;

    @InjectMocks
    private CaseEventGlossaryController caseEventGlossaryController;

    @BeforeEach
    void setup() {
        allEvents = createMockEventList();

        when(caseEventGlossaryService.getAllCaseEventGlossary()).thenReturn(allEvents);
    }

    @Test
    void testGetCaseEventGlossaryListReturnsAllGlossaries() {
        assertEquals(allEvents, caseEventGlossaryController.getCaseEventGlossaryList().getBody(),
                     "Should contain all case event glossaries");
    }

    @Test
    void testGGetCaseEventGlossaryListReturnsOk() {
        assertEquals(HttpStatus.OK, caseEventGlossaryController.getCaseEventGlossaryList().getStatusCode(),
                     "Event code should match");
    }
}
