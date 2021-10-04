package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ReflectionException;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.CourtMethods;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.CourtHelper.createMockCourtList;

@SpringBootTest
public class FilterServiceTest {

    @Autowired
    private FilterService filterService;

    @MockBean
    private CourtService courtService;

    @BeforeEach
    public void setup() {
        List<Court> allCourts = createMockCourtList();

        when(courtService.getAllCourts()).thenReturn(allCourts);
    }

    @Test
    public void testFilterCourtsJurisdictionCrown() {
        List<Court> filteredCourts = filterService.filterCourts("Crown Court",
                                                                CourtMethods.JURISDICTION.methodName);

        assertEquals(4, filteredCourts.size(), "Filtered courts size should be 4");
        assertEquals("mock court 7", filteredCourts.get(0).getName(),
                     "Filtered court name should match");
    }

    @Test
    public void testFilterCourtsJurisdictionMag() {
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
    public void testFilterCourtsLocation() {
        List<Court> filteredCourts = filterService.filterCourts("London", CourtMethods.LOCATION.methodName);

        assertEquals(2, filteredCourts.size(), "Filtered court size should be 2");
        assertEquals("mock court 11", filteredCourts.get(0).getName(),
                     "Filtered court name should match"
        );

    }

    @Test
    public void testFilterCourtsAnyCase() {
        List<Court> filteredCourts = filterService.filterCourts("LoNdOn", CourtMethods.LOCATION.methodName);

        assertEquals(2, filteredCourts.size(), "Filtered court size should be 2");
    }

    @Test
    public void testFilterCourtsReturnsNoMatches() {
        List<Court> filteredCourts = filterService.filterCourts("moon", CourtMethods.LOCATION.methodName);
        assertEquals(0, filteredCourts.size(), "Should return empty list if no courts filtered");
    }

    @Test
    public void testFilterCourtsReturnsListOverloaded() {
        List<Court> preFilteredCourts = filterService.filterCourts("Crown Court",
                                                                   CourtMethods.JURISDICTION.methodName);
        assertEquals(4, preFilteredCourts.size(), "pre-filtered courts size should be 4");

        List<Court> filteredCourts = filterService.filterCourts("manchester", CourtMethods.LOCATION.methodName,
                                                                Optional.of(preFilteredCourts));
        assertEquals(2, filteredCourts.size(), "Courts should be filtered down from filtered list");
    }

    @Test
    public void testEmptyOptionalUsesAllCourts() {
        List<Court> filteredCourts = filterService.filterCourts("manchester", CourtMethods.LOCATION.methodName,
                                                                Optional.empty());
        assertEquals(8, filteredCourts.size(), "Courts should be filtered down from full list");
    }

    @Test
    public void testInvalidMethodNameThrowsReflectionException() {
        assertThrows(ReflectionException.class, () ->
                         filterService.filterCourts("london", "InvalidMethod"),
                     "Expected filterCourts() to throw ReflectionException"
        );
    }
}
