package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CourtNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.request.FilterRequest;
import uk.gov.hmcts.reform.pip.data.management.service.CourtService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.CourtHelper.createMockCourtList;

@SpringBootTest(classes = {Application.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CourtControllerTest {

    private List<Court> allCourts;
    private final List<String> filters = new ArrayList<>();
    private final List<String> values = new ArrayList<>();

    @Mock
    private CourtService courtService;

    @InjectMocks
    private CourtController courtController;


    @BeforeEach
    void setup() {
        allCourts = createMockCourtList();

        filters.add("location");
        filters.add("jurisdiction");

        values.add("london");
        values.add("manchester");

        when(courtService.getAllCourts()).thenReturn(allCourts);
        when(courtService.handleSearchCourt("mock court 1")).thenReturn(allCourts.get(0));
        when(courtService.handleSearchCourt(1)).thenReturn(allCourts.get(0));
        when(courtService.handleFilterRequest(filters, values)).thenReturn(allCourts);
        when(courtService.handleSearchCourt("Invalid")).thenThrow(CourtNotFoundException.class);
        when(courtService.handleSearchCourt(7)).thenThrow(CourtNotFoundException.class);
    }

    @Test
    void testGetCourtListReturnsAllCourts() {
        assertEquals(allCourts, courtController.getCourtList().getBody(), "Should contain all courts");
    }

    @Test
    void testGetCourtListReturnsOk() {
        assertEquals(HttpStatus.OK, courtController.getCourtList().getStatusCode(), "Status code should match");
    }

    @Test
    void testGetCourtIdReturnsCourt() {
        assertEquals(allCourts.get(0), courtController.getCourtById(1).getBody(),
                     "Returned Courts should match"
        );
    }

    @Test
    void testGetCourtIdReturnsOK() {
        assertEquals(HttpStatus.OK, courtController.getCourtById(1).getStatusCode(),
                     "Status code should match"
        );
    }

    @Test
    void testGetCourtIdNoResultsThrows() {
        assertThrows(CourtNotFoundException.class, () ->
            courtController.getCourtById(7));
    }

    @Test
    void testGetCourtReturnsCourt() {
        assertEquals(allCourts.get(0), courtController.getCourtByName("mock court 1").getBody(),
                     "Courts should match"
        );
    }

    @Test
    void testGetCourtReturnsOk() {
        assertEquals(HttpStatus.OK, courtController.getCourtByName("mock court 1").getStatusCode(),
                     "Status code should match"
        );
    }

    @Test
    void testGetCourtNoResultsThrows() {
        assertThrows(CourtNotFoundException.class, () ->
            courtController.getCourtByName("Invalid"));
    }

    @Test
    void testGetFilterCourtsReturnsCourts() {
        FilterRequest filterRequest = new FilterRequest(filters, values);
        assertEquals(allCourts, courtController.filterCourts(filterRequest).getBody(),
                     "Courts should match"
        );
    }
}
