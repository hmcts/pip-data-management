package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.pip.data.management.database.CourtRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CourtNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.models.court.CourtCsv;
import uk.gov.hmcts.reform.pip.data.management.models.court.CourtReference;
import uk.gov.hmcts.reform.pip.data.management.models.court.NewCourt;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private CourtService courtService;

    NewCourt newCourtFirstExample;
    NewCourt newCourtSecondExample;

    @BeforeEach
    void setup() {
        CourtCsv courtCsvFirstExample = new CourtCsv();
        courtCsvFirstExample.setCourtName("Court Name First Example");
        newCourtFirstExample = new NewCourt(courtCsvFirstExample);
        newCourtFirstExample.setCourtId(UUID.randomUUID());

        CourtCsv courtCsvSecondExample = new CourtCsv();
        courtCsvSecondExample.setCourtName("Court Name Second Example");
        newCourtSecondExample = new NewCourt(courtCsvSecondExample);
    }

    @Test
    void testGetAllCourtsCallsTheCourtRepository() {
        when(courtRepository.findAll()).thenReturn(List.of(newCourtFirstExample, newCourtSecondExample));
        List<NewCourt> returnedCourts = courtService.getAllCourts();

        assertTrue(returnedCourts.contains(newCourtFirstExample),
                     "First example court not contained in first array");

        assertTrue(returnedCourts.contains(newCourtSecondExample),
                   "First example court not contained in first array");
    }

    @Test
    void testHandleCourtIdSearchReturnsCourt() {
        when(courtRepository.getNewCourtByCourtId(newCourtFirstExample.getCourtId()))
            .thenReturn(Optional.of(newCourtFirstExample));

        NewCourt newCourt = courtService.getCourtById(newCourtFirstExample.getCourtId());

        assertEquals(newCourt, newCourtFirstExample, "Unknown court has been returned");
    }

    @Test
    void testHandleSearchCourtIdThrowsCourtNotFoundException() {
        UUID unknownId = UUID.randomUUID();

        CourtNotFoundException courtNotFoundException = assertThrows(CourtNotFoundException.class, () ->
            courtService.getCourtById(unknownId), "Expected CourtNotFoundException to be thrown"
        );

        assertTrue(courtNotFoundException.getMessage().contains(unknownId.toString()),
                   "Court not found exception does not contain the expected uuid");
    }

    @Test
    void testHandleCourtNameSearchReturnsCourt() {
        when(courtRepository.getNewCourtByCourtName(newCourtFirstExample.getCourtName()))
            .thenReturn(Optional.of(newCourtFirstExample));

        NewCourt newCourt = courtService.getCourtByName(newCourtFirstExample.getCourtName());

        assertEquals(newCourt, newCourtFirstExample, "Unknown court has been returned");
    }

    @Test
    void testHandleSearchCourtNameThrowsCourtNotFoundException() {
        String unknownName = "UnknownName";

        CourtNotFoundException courtNotFoundException = assertThrows(CourtNotFoundException.class, () ->
            courtService.getCourtByName(unknownName), "Expected CourtNotFoundException to be thrown"
        );

        assertTrue(courtNotFoundException.getMessage().contains(unknownName),
                   "Court not found exception does not contain the expected name");
    }

    @Test
    void testHandleCourtSearchByRegionAndJurisdiction() {

        List<String> regions = List.of("North West", "South West");
        List<String> jurisdictions = List.of("Magistrates Court", "Family Court");

        String expectedString = "Magistrates Court,Family Court";

        when(courtRepository.findByRegionAndJurisdictionOrderByName(regions, expectedString))
            .thenReturn(List.of(newCourtFirstExample, newCourtSecondExample));

        List<NewCourt> returnedCourts = courtService.searchByRegionAndJurisdiction(regions, jurisdictions);

        assertTrue(returnedCourts.contains(newCourtFirstExample),
                     "First court has not been found");

        assertTrue(returnedCourts.contains(newCourtSecondExample),
                   "Second court has not been found");
    }

    @Test
    void testHandleUploadCourtsOk() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsv.csv");

        MultipartFile multipartFile = new MockMultipartFile("file",
                              "TestFileName", "text/plain", IOUtils.toByteArray(inputStream));

        List<NewCourt> newCourts = new ArrayList<>(courtService.uploadCourts(multipartFile));

        assertEquals(2, newCourts.size(), "Unknown number of courts returned from parser");

        NewCourt firstCourt = newCourts.get(0);
        assertEquals("Test Court", firstCourt.getCourtName(), "Court name does not match in first court");
        assertEquals("North West", firstCourt.getRegion(), "Court region does not match in first court");
        List<String> firstCourtJurisdiction = firstCourt.getJurisdiction();
        assertEquals(2, firstCourtJurisdiction.size(), "Unexpected number of jurisdictions");
        assertTrue(firstCourtJurisdiction.contains("Magistrates Court"), "Jurisdiction does not have expected value");
        assertTrue(firstCourtJurisdiction.contains("Family Court"), "Jurisdiction does not have expected value");

        NewCourt secondCourt = newCourts.get(1);
        assertEquals("Test Court Other", secondCourt.getCourtName(), "Court name does not match in second court");
        assertEquals("South West", secondCourt.getRegion(), "Court region does not match in second court");
        List<String> secondCourtJurisdiction = secondCourt.getJurisdiction();
        assertEquals(1, secondCourtJurisdiction.size(), "Unexpected number of jurisdictions");
        assertTrue(firstCourtJurisdiction.contains("Family Court"), "Jurisdiction does not have expected value");
    }

    @Test
    void testHandleUploadReferencesOk() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsv.csv");

        MultipartFile multipartFile = new MockMultipartFile("file",
                                                            "TestFileName", "text/plain", IOUtils.toByteArray(inputStream));

        List<NewCourt> newCourts = new ArrayList<>(courtService.uploadCourts(multipartFile));

        assertEquals(2, newCourts.size(), "Unknown number of courts returned from parser");

        NewCourt firstCourt = newCourts.get(0);
        List<CourtReference> firstCourtReferences = firstCourt.getCourtReferenceList();
        assertEquals(2, firstCourtReferences.size(), "Unknown number of references for first court");
        CourtReference firstCourtReferenceOne = firstCourtReferences.get(0);
        assertEquals("TestProvenance", firstCourtReferenceOne.getProvenance(), "Provenance is not as expected");
        assertEquals("1", firstCourtReferenceOne.getProvenanceId(), "Provenance ID is not as expected");
        CourtReference firstCourtReferenceTwo = firstCourtReferences.get(1);
        assertEquals("TestProvenanceOther", firstCourtReferenceTwo.getProvenance(), "Provenance is not as expected");
        assertEquals("2", firstCourtReferenceTwo.getProvenanceId(), "Provenance ID is not as expected");


        NewCourt secondCourt = newCourts.get(1);
        List<CourtReference> secondCourtReferences = secondCourt.getCourtReferenceList();
        assertEquals(1, secondCourtReferences.size(), "Unknown number of references for second court");
        CourtReference secondCourtReferenceOne = secondCourtReferences.get(0);
        assertEquals("TestProvenance", secondCourtReferenceOne.getProvenance(), "Provenance is not as expected");
        assertEquals("1", secondCourtReferenceOne.getProvenanceId(), "Provenance ID is not as expected");

    }

    @Test
    void testHandleUploadInvalidCsv() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/InvalidCsv.txt");

        MultipartFile multipartFile = new MockMultipartFile("file",
                                                            "TestFileName", "text/plain", IOUtils.toByteArray(inputStream));


        assertThrows(CsvParseException.class, () -> courtService.uploadCourts(multipartFile));
    }

}

