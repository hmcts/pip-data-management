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
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.HEARINGS_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.CourtHelper.createHearing;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class FilterServiceTest {

    private static final String CASE_NAME = "test name";

    private List<Hearing> hearings;

    @Autowired
    private FilterService filterService;

    @BeforeEach
    void setup() {
        hearings = createHearing();
        hearings.get(0).setCaseName(CASE_NAME);
        hearings.get(0).setCaseNumber("9999");
        hearings.get(0).setUrn("123");
        hearings.get(1).setCaseName("invalid");
        hearings.get(1).setCaseNumber("Av123");
        hearings.get(1).setUrn("Av123");
    }

    @Test
    void testFilterHearingsByNameFullMatch() {
        assertEquals(1, filterService.filterHearingsByName(CASE_NAME, hearings).size(),
                     "Hearings size should match");
        assertEquals(CASE_NAME, filterService.filterHearingsByName(CASE_NAME, hearings).get(0).getCaseName(),
                     "Case name should match");
    }

    @Test
    void testFilterHearingsByNamePartialMatchCase() {
        assertEquals(1, filterService.filterHearingsByName("TEsT", hearings).size(),
                     "Hearings size should match");
        assertEquals(CASE_NAME, filterService.filterHearingsByName("TEsT", hearings).get(0).getCaseName(),
                     "Case name should match");
    }

    @Test
    void testFilterHearingsByNamePartialMatch1Char() {
        assertEquals(1, filterService.filterHearingsByName("t", hearings).size(),
                     "Hearings size should match");
        assertEquals(CASE_NAME, filterService.filterHearingsByName("t", hearings).get(0).getCaseName(),
                     "Case name should match");
    }

    @Test
    void testFindHearingByCaseNumber() {
        assertEquals(hearings.get(0), filterService.findHearingByCaseNumber("9999", hearings),
                     HEARINGS_MATCH);
    }

    @Test
    void testFindHearingByCaseNumberNoMatch() {
        assertNull(filterService.findHearingByCaseNumber("99999", hearings),
                     "Should return null for no match");
    }

    @Test
    void testFindHearingByCaseNumberCaseInsensitive() {
        assertEquals(hearings.get(1), filterService.findHearingByCaseNumber("av123", hearings),
                     HEARINGS_MATCH);
    }

    @Test
    void testFindHearingByUrn() {
        assertEquals(hearings.get(0), filterService.findHearingByUrn("123", hearings),
                     HEARINGS_MATCH);
    }

    @Test
    void testFindHearingByUrnNoMatch() {
        assertNull(filterService.findHearingByUrn("000",hearings),
                   "Should return null for no match");
    }

    @Test
    void testFindHearingByUrnCaseInsensitive() {
        assertEquals(hearings.get(1), filterService.findHearingByUrn("av123", hearings),
                     HEARINGS_MATCH);
    }

}
