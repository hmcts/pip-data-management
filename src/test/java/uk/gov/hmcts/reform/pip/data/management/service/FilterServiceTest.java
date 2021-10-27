package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ReflectionException;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.CourtMethods;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.CourtHelper.createHearing;
import static uk.gov.hmcts.reform.pip.data.management.helpers.CourtHelper.createMockCourtList;
import static uk.gov.hmcts.reform.pip.data.management.helpers.TestConstants.HEARINGS_MATCH;

@SpringBootTest
class FilterServiceTest {

    private static final String CASE_NAME = "test name";

    private List<Hearing> hearings;

    @Autowired
    private FilterService filterService;

    @MockBean
    private CourtService courtService;

    @BeforeEach
    void setup() {
        hearings = createHearing();
        hearings.get(0).setCaseName(CASE_NAME);
        hearings.get(0).setCaseNumber("9999");
        hearings.get(0).setUrn("123");
        hearings.get(1).setCaseName("invalid");
        hearings.get(1).setCaseNumber("Av123");
        hearings.get(1).setUrn("Av123");

        List<Court> allCourts = createMockCourtList();
        when(courtService.getAllCourts()).thenReturn(allCourts);
    }

    @Test
    void testFilterCourtsJurisdictionCrown() {
        List<Court> filteredCourts = filterService.filterCourts("Crown Court",
                                                                CourtMethods.JURISDICTION.methodName);

        assertEquals(4, filteredCourts.size(), "Filtered courts size should be 4");
        assertEquals("mock court 7", filteredCourts.get(0).getName(),
                     "Filtered court name should match");
    }

    @Test
    void testFilterCourtsJurisdictionMag() {
        List<Court> filteredCourts = filterService.filterCourts("Magistrates Court",
                                                                CourtMethods.JURISDICTION.methodName);

        assertEquals(6, filteredCourts.size(),
                     "Filtered courts size should be 6 when searching only jurisdiction for Crown Court"
        );
        assertEquals("mock court 1", filteredCourts.get(0).getName(),
                     "Filtered court name should match"
        );
    }

    @Test
    void testFilterCourtsLocation() {
        List<Court> filteredCourts = filterService.filterCourts("London", CourtMethods.LOCATION.methodName);

        assertEquals(2, filteredCourts.size(), "Filtered court size should be 2");
        assertEquals("mock court 11", filteredCourts.get(0).getName(),
                     "Filtered court name should match"
        );

    }

    @Test
    void testFilterCourtsAnyCase() {
        List<Court> filteredCourts = filterService.filterCourts("LoNdOn", CourtMethods.LOCATION.methodName);

        assertEquals(2, filteredCourts.size(), "Filtered court size should be 2");
    }

    @Test
    void testFilterCourtsReturnsNoMatches() {
        List<Court> filteredCourts = filterService.filterCourts("moon", CourtMethods.LOCATION.methodName);
        assertEquals(0, filteredCourts.size(), "Should return empty list if no courts filtered");
    }

    @Test
    void testFilterCourtsReturnsListOverloaded() {
        List<Court> preFilteredCourts = filterService.filterCourts("Crown Court",
                                                                   CourtMethods.JURISDICTION.methodName);
        assertEquals(4, preFilteredCourts.size(), "pre-filtered courts size should be 4");

        List<Court> filteredCourts = filterService.filterCourts("manchester", CourtMethods.LOCATION.methodName,
                                                                Optional.of(preFilteredCourts));
        assertEquals(2, filteredCourts.size(), "Courts should be filtered down from filtered list");
    }

    @Test
    void testEmptyOptionalUsesAllCourts() {
        List<Court> filteredCourts = filterService.filterCourts("manchester", CourtMethods.LOCATION.methodName,
                                                                Optional.empty());
        assertEquals(8, filteredCourts.size(), "Courts should be filtered down from full list");
    }

    @Test
    void testInvalidMethodNameThrowsReflectionException() {
        assertThrows(ReflectionException.class, () ->
                         filterService.filterCourts("london", "InvalidMethod"),
                     "Expected filterCourts() to throw ReflectionException"
        );
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
