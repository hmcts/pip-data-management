package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;
import uk.gov.hmcts.reform.pip.data.management.service.HearingService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class HearingControllerTest {

    private static final String CASE_NAME = "test case name";
    private static final String NUMBER = "123456";
    private static final String STATUS_CODE_MATCH = "Status code should match";

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
        hearingList.add(hearing);

        when(hearingService.getHearings(1)).thenReturn(hearingList);
        when(hearingService.getHearingByName(CASE_NAME)).thenReturn(hearingList);
        when(hearingService.getHearingByCaseNumber(NUMBER)).thenReturn(hearingList.get(0));
        when(hearingService.getHearingByUrn(NUMBER)).thenReturn(hearingList.get(0));
    }

    @Test
    void testGetHearings() {
        assertEquals(hearingList.get(0), hearingController.getHearing(1).getBody().get(0),
                     "Hearings should match"
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
                     "Hearings should match");
    }

    @Test
    void testGetHearingsByNameReturnsOk() {
        assertEquals(HttpStatus.OK, hearingController.getHearingsByName(CASE_NAME).getStatusCode(),
                     "Status code should match");
    }

    @Test
    void testGetHearingByCaseNumber() {
        assertEquals(hearingList.get(0), hearingController.getHearingsByCaseNumber(NUMBER).getBody(),
                     "Hearing should match");
    }

    @Test
    void testGetHearingByCaseNumberReturnsOk() {
        assertEquals(HttpStatus.OK, hearingController.getHearingsByCaseNumber(NUMBER).getStatusCode(),
                     STATUS_CODE_MATCH);
    }

    @Test
    void testGetHearingByUrn() {
        assertEquals(hearingList.get(0), hearingController.getHearingByUrn(NUMBER).getBody(),
                     "Hearing should match");
    }

    @Test
    void testGetHearingByUrnReturnsOk() {
        assertEquals(HttpStatus.OK, hearingController.getHearingByUrn(NUMBER).getStatusCode(),
                     STATUS_CODE_MATCH);
    }
}
