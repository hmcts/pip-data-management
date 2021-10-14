package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
        hearingList.add(hearing);

        when(hearingService.getHearings(1)).thenReturn(hearingList);
    }

    @Test
    void testGetHearings() {
        assertEquals(hearingList.get(0), hearingController.getHearing(1).getBody().get(0),
                     "Hearings should match"
        );
    }
}
