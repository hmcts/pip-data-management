package uk.gov.hmcts.reform.pip.data.management.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.INVALID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.CourtHelper.createHearing;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class HearingServiceTest {

    private static final String CASE_NAME = "test";
    private static final String NUMBER = "123456";
    private static final String EXCEPTION_THROWN = "Expect HearingNotFound to be thrown";

    private List<Hearing> mockHearings;

    @MockBean
    private FilterService filterService;

    @Autowired
    private HearingService hearingService;

    @BeforeEach
    void setup() {
        mockHearings = createHearing();
        when(filterService.filterHearingsByName(eq(CASE_NAME), any())).thenReturn(mockHearings);
        when(filterService.filterHearingsByName(eq(INVALID), any())).thenReturn(new ArrayList<>());
        when(filterService.findHearingByCaseNumber(eq(NUMBER), any())).thenReturn(mockHearings.get(0));
        when(filterService.findHearingByCaseNumber(eq(INVALID), any())).thenReturn(null);
        when(filterService.findHearingByUrn(eq(NUMBER), any())).thenReturn(mockHearings.get(0));
        when(filterService.findHearingByUrn(eq(INVALID), any())).thenReturn(null);
    }

    @Test
    void testGetHearingsReturnsExpected() {
        assertEquals(3, hearingService.getHearings(1).size(), "Number of hearings should match");
    }

    @Test
    void testNoHearingsThrowsNotFoundException() {
        Exception ex = assertThrows(HearingNotFoundException.class, () ->
            hearingService.getHearings(5), EXCEPTION_THROWN
        );
        assertEquals("No hearings found for court id: 5", ex.getMessage(), MESSAGES_MATCH);
    }

    @Test
    void testGetHearingByName() {
        assertEquals(2, hearingService.getHearingByName(CASE_NAME).size(), "Number of hearings should match");
    }

    @Test
    void testGetHearingByNameThrows() {
        Exception ex = assertThrows(HearingNotFoundException.class, () ->
            hearingService.getHearingByName(INVALID), EXCEPTION_THROWN);
        assertEquals("No hearings found containing the case name: invalid", ex.getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testGetHearingByCaseNumber() {
        assertEquals(mockHearings.get(0), hearingService.getHearingByCaseNumber(NUMBER),
                     "Expect hearing object to match");
    }

    @Test
    void testGetHearingByCaseNumberThrows() {
        Exception ex = assertThrows(HearingNotFoundException.class, () ->
            hearingService.getHearingByCaseNumber(INVALID), EXCEPTION_THROWN);
        assertEquals("No hearing found for case number: invalid", ex.getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testGetHearingByUrn() {
        assertEquals(mockHearings.get(0), hearingService.getHearingByUrn(NUMBER),
                     "Expect hearing object to match");
    }

    @Test
    void testGetHearingThrows() {
        Exception ex = assertThrows(HearingNotFoundException.class, () ->
            hearingService.getHearingByUrn(INVALID), EXCEPTION_THROWN);
        assertEquals("No hearing found for urn number: invalid", ex.getMessage(),
                     MESSAGES_MATCH);
    }
}
