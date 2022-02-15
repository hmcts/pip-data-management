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
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTest;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CourtNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.CourtMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.CourtHelper.returnFilteredCourtsWhereResultsShouldBe1;
import static uk.gov.hmcts.reform.pip.data.management.helpers.CourtHelper.returnFilteredCourtsWhereResultsShouldBe2;
import static uk.gov.hmcts.reform.pip.data.management.helpers.TestConstants.INVALID;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTest.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class CourtServiceTest {

    private static final String ABERGAVENNY_MAGISTRATES_COURT = "Abergavenny Magistrates' Court";
    private static final String ACCRINGTON_MAGISTRATES_COURT = "Accrington Magistrates' Court";
    private static final String LESLEY_COUNTY_COURT = "Lesley County Court";
    private static final String MANCHESTER_FAMILY_COURT = "Manchester Family Court";
    private static final String MANCHESTER = "Manchester";
    private static final String SORTED_MESSAGE = "Courts should be sorted";

    private List<Court> courts;
    private List<Court> foundCourt;
    private List<String> filters;
    private List<String> values;

    @MockBean
    private FilterService filterService;

    @Autowired
    private CourtService courtService;

    @BeforeEach
    void setup() {
        filters = new ArrayList<>();
        values = new ArrayList<>();

        courts = courtService.getAllCourts();
        foundCourt = courts.stream().filter(court -> court.getCourtId().equals(2)).collect(Collectors.toList());

        when(filterService.filterCourts("1", CourtMethods.COURT_ID.methodName)).thenReturn(foundCourt);
        when(filterService.filterCourts("7", CourtMethods.COURT_ID.methodName))
            .thenThrow(CourtNotFoundException.class);
        when(filterService.filterCourts(LESLEY_COUNTY_COURT, CourtMethods.NAME.methodName)).thenReturn(foundCourt);
        when(filterService.filterCourts(INVALID, CourtMethods.NAME.methodName)).thenThrow(CourtNotFoundException.class);
        when(filterService.filterCourts(MANCHESTER, CourtMethods.LOCATION.methodName, Optional.of(courts)))
            .thenReturn(returnFilteredCourtsWhereResultsShouldBe2(1));
        when(filterService.filterCourts("magistrates court", CourtMethods.LOCATION.methodName, Optional.of(courts)))
            .thenReturn(new ArrayList<>());
        when(filterService.filterCourts(MANCHESTER, CourtMethods.JURISDICTION.methodName,
                                        Optional.of(returnFilteredCourtsWhereResultsShouldBe2(1))))
            .thenReturn(new ArrayList<>());
        when(filterService.filterCourts("magistrates court", CourtMethods.JURISDICTION.methodName,
                                        Optional.of(returnFilteredCourtsWhereResultsShouldBe2(1))))
            .thenReturn(returnFilteredCourtsWhereResultsShouldBe1());
        when(filterService.filterCourts("hull", CourtMethods.LOCATION.methodName, Optional.of(courts)))
            .thenReturn(returnFilteredCourtsWhereResultsShouldBe2(3));
    }

    @Test
    void testGetAllCourtsReturnsAlphabetised() {
        assertEquals(ABERGAVENNY_MAGISTRATES_COURT, courts.get(0).getName(), SORTED_MESSAGE);
        assertEquals(ACCRINGTON_MAGISTRATES_COURT, courts.get(1).getName(), SORTED_MESSAGE);
        assertEquals(LESLEY_COUNTY_COURT, courts.get(2).getName(), SORTED_MESSAGE);
        assertEquals(MANCHESTER_FAMILY_COURT, courts.get(3).getName(), SORTED_MESSAGE);
    }

    @Test
    void testHandleCourtIdSearchReturnsCourt() {
        assertEquals(foundCourt.get(0), courtService.handleSearchCourt(1),
                     "Found court should match");
    }

    @Test
    void testHandleSearchCourtIdThrowsCourtNotFoundException() {
        assertThrows(CourtNotFoundException.class, () ->
            courtService.handleSearchCourt(INVALID), "Expected CourtNotFoundException to be thrown"
        );
    }

    @Test
    void testHandleCourtSearchReturnsCourt() {
        assertEquals(foundCourt.get(0), courtService.handleSearchCourt(LESLEY_COUNTY_COURT),
                     "Found court should match");
    }

    @Test
    void testHandleSearchCourtThrowsCourtNotFoundException() {
        assertThrows(CourtNotFoundException.class, () ->
            courtService.handleSearchCourt(INVALID), "Expected CourtNotFoundException to be thrown"
        );
    }

    @Test
    void testHandleFilterRequestWith2FiltersAndValues() {
        filters.add("location");
        filters.add("jurisdiction");
        values.add(MANCHESTER);
        values.add("magistrates court");

        assertEquals(1, courtService.handleFilterRequest(filters, values).size(),
                     "Court list should have been filtered to 1 court");
    }

    @Test
    void testHandleFilterRequestWith1Filter2Values() {
        filters.add("location");
        values.add(MANCHESTER);
        values.add("hull");

        assertEquals(4, courtService.handleFilterRequest(filters, values).size(),
                     "Court list should have been filtered to 4 courts");
    }
}

