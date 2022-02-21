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
import uk.gov.hmcts.reform.pip.data.management.models.court.Court;
import uk.gov.hmcts.reform.pip.data.management.models.court.CourtCsv;
import uk.gov.hmcts.reform.pip.data.management.models.court.CourtReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private CourtService courtService;

    Court courtFirstExample;
    Court courtSecondExample;

    private static final String FAMILY_COURT =  "Family Court";
    private static final String MAGISTRATES_COURT = "Magistrates Court";

    @BeforeEach
    void setup() {
        CourtCsv courtCsvFirstExample = new CourtCsv();
        courtCsvFirstExample.setCourtName("Court Name First Example");
        courtFirstExample = new Court(courtCsvFirstExample);
        courtFirstExample.setCourtId(1);

        CourtCsv courtCsvSecondExample = new CourtCsv();
        courtCsvSecondExample.setCourtName("Court Name Second Example");
        courtSecondExample = new Court(courtCsvSecondExample);
    }

    @Test
    void testGetAllCourtsCallsTheCourtRepository() {
        when(courtRepository.findAll()).thenReturn(List.of(courtFirstExample, courtSecondExample));
        List<Court> returnedCourts = courtService.getAllCourts();

        assertTrue(returnedCourts.contains(courtFirstExample),
                     "First example court not contained in first array");

        assertTrue(returnedCourts.contains(courtSecondExample),
                   "First example court not contained in first array");
    }

    @Test
    void testHandleCourtIdSearchReturnsCourt() {
        when(courtRepository.getCourtByCourtId(courtFirstExample.getCourtId()))
            .thenReturn(Optional.of(courtFirstExample));

        Court court = courtService.getCourtById(courtFirstExample.getCourtId());

        assertEquals(court, courtFirstExample, "Unknown court has been returned");
    }

    @Test
    void testHandleSearchCourtIdThrowsCourtNotFoundException() {
        int unknownId = 1234;

        CourtNotFoundException courtNotFoundException = assertThrows(CourtNotFoundException.class, () ->
            courtService.getCourtById(unknownId), "Expected CourtNotFoundException to be thrown"
        );

        assertTrue(courtNotFoundException.getMessage().contains(String.valueOf(unknownId)),
                   "Court not found exception does not contain the expected uuid");
    }

    @Test
    void testHandleCourtNameSearchReturnsCourt() {
        when(courtRepository.getCourtByName(courtFirstExample.getName()))
            .thenReturn(Optional.of(courtFirstExample));

        Court court = courtService.getCourtByName(courtFirstExample.getName());

        assertEquals(court, courtFirstExample, "Unknown court has been returned");
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
        List<String> jurisdictions = List.of(MAGISTRATES_COURT, FAMILY_COURT);

        String expectedJurisdictions = MAGISTRATES_COURT + "," + FAMILY_COURT;
        String expectedRegions = "North West,South West";

        when(courtRepository.findByRegionAndJurisdictionOrderByName(expectedRegions, expectedJurisdictions))
            .thenReturn(List.of(courtFirstExample, courtSecondExample));

        List<Court> returnedCourts = courtService.searchByRegionAndJurisdiction(regions, jurisdictions);

        assertTrue(returnedCourts.contains(courtFirstExample),
                     "First court has not been found");

        assertTrue(returnedCourts.contains(courtSecondExample),
                   "Second court has not been found");
    }

    @Test
    void testHandleCourtSearchOnlyRegion() {
        List<String> regions = List.of("North West", "South West");

        String expectedRegions = "North West,South West";

        when(courtRepository.findByRegionAndJurisdictionOrderByName(expectedRegions, ""))
            .thenReturn(List.of(courtFirstExample));

        List<Court> returnedCourts = courtService.searchByRegionAndJurisdiction(regions, null);

        assertTrue(returnedCourts.contains(courtFirstExample),
                   "First court has not been found");
    }

    @Test
    void testHandleCourtSearchOnlyJurisdiction() {
        List<String> jurisdictions = List.of(MAGISTRATES_COURT, FAMILY_COURT);

        String expectedJurisdictions = MAGISTRATES_COURT + "," + FAMILY_COURT;

        when(courtRepository.findByRegionAndJurisdictionOrderByName("", expectedJurisdictions))
            .thenReturn(List.of(courtSecondExample));

        List<Court> returnedCourts = courtService.searchByRegionAndJurisdiction(null, jurisdictions);

        assertTrue(returnedCourts.contains(courtSecondExample),
                   "Second court has not been found");
    }

    @Test
    void testHandleCourtSearchNoRegionOrJurisdiction() {
        when(courtRepository.findByRegionAndJurisdictionOrderByName("", ""))
            .thenReturn(List.of(courtFirstExample, courtSecondExample));

        List<Court> returnedCourts = courtService.searchByRegionAndJurisdiction(null, null);

        assertTrue(returnedCourts.contains(courtFirstExample),
                   "First court has not been found");

        assertTrue(returnedCourts.contains(courtSecondExample),
                   "Second court has not been found");
    }

    @Test
    void testHandleUploadCourtsOk() throws IOException {
        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsv.csv")) {

            MultipartFile multipartFile = new MockMultipartFile("file",
                                                                "TestFileName",
                                                                "text/plain",
                                                                IOUtils.toByteArray(inputStream)
            );

            List<Court> courts = new ArrayList<>(courtService.uploadCourts(multipartFile));

            assertEquals(2, courts.size(), "Unknown number of courts returned from parser");

            Court firstCourt = courts.get(0);
            assertEquals("Test Court", firstCourt.getName(), "Court name does not match in first court");
            assertEquals("North West", firstCourt.getRegion(), "Court region does not match in first court");
            List<String> firstCourtJurisdiction = firstCourt.getJurisdiction();
            assertEquals(2, firstCourtJurisdiction.size(), "Unexpected number of jurisdictions");
            assertTrue(firstCourtJurisdiction.contains(MAGISTRATES_COURT), "Jurisdiction does not have expected value");
            assertTrue(firstCourtJurisdiction.contains(FAMILY_COURT), "Jurisdiction does not have expected value");

            Court secondCourt = courts.get(1);
            assertEquals("Test Court Other", secondCourt.getName(), "Court name does not match in second court");
            assertEquals("South West", secondCourt.getRegion(), "Court region does not match in second court");
            List<String> secondCourtJurisdiction = secondCourt.getJurisdiction();
            assertEquals(1, secondCourtJurisdiction.size(), "Unexpected number of jurisdictions");
            assertTrue(firstCourtJurisdiction.contains(FAMILY_COURT), "Jurisdiction does not have expected value");
        }
    }

    @Test
    void testHandleUploadReferencesOk() throws IOException {

        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsv.csv")) {


            MultipartFile multipartFile = new MockMultipartFile("file", "TestFileName",
                                                                "text/plain", IOUtils.toByteArray(inputStream)
            );

            List<Court> courts = new ArrayList<>(courtService.uploadCourts(multipartFile));

            assertEquals(2, courts.size(), "Unknown number of courts returned from parser");

            Court firstCourt = courts.get(0);
            List<CourtReference> firstCourtReferences = firstCourt.getCourtReferenceList();
            assertEquals(2, firstCourtReferences.size(), "Unknown number of references for first court");
            CourtReference firstCourtReferenceOne = firstCourtReferences.get(0);
            assertEquals("TestProvenance", firstCourtReferenceOne.getProvenance(), "Provenance is not as expected");
            assertEquals("1", firstCourtReferenceOne.getProvenanceId(), "Provenance ID is not as expected");
            CourtReference firstCourtReferenceTwo = firstCourtReferences.get(1);
            assertEquals(
                "TestProvenanceOther",
                firstCourtReferenceTwo.getProvenance(),
                "Provenance is not as expected"
            );
            assertEquals("2", firstCourtReferenceTwo.getProvenanceId(), "Provenance ID is not as expected");


            Court secondCourt = courts.get(1);
            List<CourtReference> secondCourtReferences = secondCourt.getCourtReferenceList();
            assertEquals(1, secondCourtReferences.size(), "Unknown number of references for second court");
            CourtReference secondCourtReferenceOne = secondCourtReferences.get(0);
            assertEquals("TestProvenance", secondCourtReferenceOne.getProvenance(), "Provenance is not as expected");
            assertEquals("1", secondCourtReferenceOne.getProvenanceId(), "Provenance ID is not as expected");
        }

    }

    @Test
    void testHandleUploadInvalidCsv() throws IOException {

        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/InvalidCsv.txt")) {


            MultipartFile multipartFile = new MockMultipartFile("file", "TestFileName",
                                                                "text/plain", IOUtils.toByteArray(inputStream)
            );


            assertThrows(CsvParseException.class, () -> courtService.uploadCourts(multipartFile));
        }
    }

}

