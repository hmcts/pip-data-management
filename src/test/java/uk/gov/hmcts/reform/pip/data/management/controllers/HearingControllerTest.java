package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;
import uk.gov.hmcts.reform.pip.data.management.service.HearingService;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.HEARINGS_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.STATUS_CODE_MATCH;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class HearingControllerTest {

    private static final String CASE_NAME = "test case name";
    private static final String NUMBER = "123456";

    private final List<Hearing> hearingList = new ArrayList<>();

    @Mock
    private HearingService hearingService;

    @InjectMocks
    private HearingController hearingController;

    @BeforeEach
    void setup() {
        Hearing hearing = new Hearing();
        hearing.setCourtId(1);
        hearing.setHearingId(2);
        hearing.setCaseName(CASE_NAME);
        hearing.setDate(new GregorianCalendar(2021, 8, 27, 21, 57, 01).getTime());
        hearingList.add(hearing);

        when(hearingService.getHearings(1)).thenReturn(hearingList);
        when(hearingService.getHearingByName(CASE_NAME)).thenReturn(hearingList);
        when(hearingService.getHearingByCaseNumber(NUMBER)).thenReturn(hearingList.get(0));
        when(hearingService.getHearingByUrn(NUMBER)).thenReturn(hearingList.get(0));
    }

    @Test
    void testGetHearings() {
        assertEquals(hearingList.get(0), hearingController.getHearing(1).getBody().get(0),
                     HEARINGS_MATCH
        );
    }

    @Test
    void testGetHearingsReturnsOk() {
        assertEquals(HttpStatus.OK, hearingController.getHearing(1).getStatusCode(),
                     STATUS_CODE_MATCH
        );
    }

    @Test
    void testGetHearingsByName() {
        assertEquals(hearingList, hearingController.getHearingsByName(CASE_NAME).getBody(),
                     HEARINGS_MATCH);
    }

    @Test
    void testGetHearingsByNameReturnsOk() {
        assertEquals(HttpStatus.OK, hearingController.getHearingsByName(CASE_NAME).getStatusCode(),
                     STATUS_CODE_MATCH);
    }

    @Test
    void testGetHearingByCaseNumber() {
        assertEquals(hearingList.get(0), hearingController.getHearingsByCaseNumber(NUMBER).getBody(),
                     HEARINGS_MATCH);
    }

    @Test
    void testGetHearingByCaseNumberReturnsOk() {
        assertEquals(HttpStatus.OK, hearingController.getHearingsByCaseNumber(NUMBER).getStatusCode(),
                     STATUS_CODE_MATCH);
    }

    @Test
    void testGetHearingByUrn() {
        assertEquals(hearingList.get(0), hearingController.getHearingByUrn(NUMBER).getBody(),
                     HEARINGS_MATCH);
    }

    @Test
    void testGetHearingByUrnReturnsOk() {
        assertEquals(HttpStatus.OK, hearingController.getHearingByUrn(NUMBER).getStatusCode(),
                     STATUS_CODE_MATCH);
    }
}
