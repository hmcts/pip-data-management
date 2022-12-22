package uk.gov.hmcts.reform.pip.data.management.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.CaseEventGlossary;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class CourtEventGlossaryServiceTest {

    private static final String ADJOURNED = "Adjourned";
    private static final String APPEAL_INTERPRETER_SWORN = "Appeal Interpreter Sworn";
    private static final String APPEAL_WITNESS_CONTINUES = "Appeal Witness continues";
    private static final String SUCCESS_MESSAGE = "Events should be returned";

    private List<CaseEventGlossary> courtEventGlossaries;

    @Autowired
    private CaseEventGlossaryService courtEventGlossaryService;

    @BeforeEach
    void setup() {

        courtEventGlossaries = courtEventGlossaryService.getAllCaseEventGlossary();
    }

    @Test
    void testGetAllCaseEventGlossariesReturnsAlphabetised() {
        assertEquals(ADJOURNED, courtEventGlossaries.get(0).getName(), SUCCESS_MESSAGE);
        assertEquals(APPEAL_INTERPRETER_SWORN, courtEventGlossaries.get(1).getName(), SUCCESS_MESSAGE);
        assertEquals(APPEAL_WITNESS_CONTINUES, courtEventGlossaries.get(2).getName(), SUCCESS_MESSAGE);
    }
}
