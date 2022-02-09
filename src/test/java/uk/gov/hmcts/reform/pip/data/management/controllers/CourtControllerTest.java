package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTest;
import uk.gov.hmcts.reform.pip.data.management.models.court.Court;
import uk.gov.hmcts.reform.pip.data.management.models.court.CourtCsv;
import uk.gov.hmcts.reform.pip.data.management.service.CourtService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTest.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class CourtControllerTest {

    @Mock
    private CourtService courtService;

    @InjectMocks
    private CourtController courtController;

    Court firstCourt;
    Court secondCourt;

    @BeforeEach
    void setup() {

        CourtCsv firstCourtCsv = new CourtCsv();
        firstCourtCsv.setCourtName("Court Name A");
        firstCourtCsv.setProvenance("Court Provenance A");
        firstCourtCsv.setRegion("Court Region A");
        firstCourt = new Court(firstCourtCsv);

        CourtCsv secondCourtCsv = new CourtCsv();
        secondCourtCsv.setCourtName("Court Name B");
        secondCourtCsv.setProvenance("Court Provenance B");
        secondCourtCsv.setRegion("Court Region B");
        secondCourt = new Court(secondCourtCsv);
    }

    @Test
    void testGetCourtListReturnsAllCourts() {

        when(courtService.getAllCourts()).thenReturn(List.of(firstCourt, secondCourt));

        List<Court> returnedCourts = courtController.getCourtList().getBody();

        assertEquals(2, returnedCourts.size(), "Unexpected number of courts returned");
        assertTrue(returnedCourts.contains(firstCourt), "First court not contained within list");
        assertTrue(returnedCourts.contains(secondCourt), "Second court not contained within list");
    }

    @Test
    void testGetCourtListReturnsOk() {
        when(courtService.getAllCourts()).thenReturn(List.of(firstCourt, secondCourt));
        assertEquals(HttpStatus.OK, courtController.getCourtList().getStatusCode(),
                     "Court list has not returned OK");
    }

    @Test
    void testGetCourtIdReturnsCourt() {
        int id = 1;
        when(courtService.getCourtById(id)).thenReturn(firstCourt);

        assertEquals(firstCourt, courtController.getCourtById(id).getBody(),
                     "Returned court does not match expected court"
        );
    }

    @Test
    void testGetCourtIdReturnsOK() {
        int id = 1;
        when(courtService.getCourtById(id)).thenReturn(firstCourt);

        assertEquals(HttpStatus.OK, courtController.getCourtById(id).getStatusCode(),
                     "Court ID search has not returned OK"
        );
    }

    @Test
    void testGetCourtByNameReturnsCourt() {
        when(courtService.getCourtByName(secondCourt.getName())).thenReturn(secondCourt);

        assertEquals(secondCourt, courtController.getCourtByName(secondCourt.getName()).getBody(),
                     "Returned court does not match expected court"
        );
    }

    @Test
    void testGetCourtByNameReturnsOk() {
        when(courtService.getCourtByName(secondCourt.getName())).thenReturn(secondCourt);

        assertEquals(HttpStatus.OK, courtController.getCourtByName(secondCourt.getName()).getStatusCode(),
                     "Court Name search has not returned OK"
        );
    }

    @Test
    void testGetFilterCourtsReturnsCourts() {
        List<String> regions = List.of("Region A", "Region B");
        List<String> jurisdictions = List.of("Jurisdiction A", "Jurisdiction B");

        when(courtService.searchByRegionAndJurisdiction(regions, jurisdictions))
            .thenReturn(List.of(firstCourt, secondCourt));

        List<Court> courts = courtController.searchByRegionAndJurisdiction(regions, jurisdictions).getBody();


        assertEquals(2, courts.size(), "Unexpected number of courts have been returned");
        assertTrue(courts.contains(firstCourt), "First court not contained within list");
        assertTrue(courts.contains(secondCourt), "Second court not contained within list");
    }

    @Test
    void testGetByRegionAndJurisdictionReturnsOk() {
        List<String> regions = List.of("Region A", "Region B");
        List<String> jurisdictions = List.of("Jurisdiction A", "Jurisdiction B");

        when(courtService.searchByRegionAndJurisdiction(regions, jurisdictions))
            .thenReturn(List.of(firstCourt, secondCourt));

        assertEquals(HttpStatus.OK,
                     courtController.searchByRegionAndJurisdiction(regions, jurisdictions).getStatusCode(),
                     "Court region and jurisdiction search has not returned OK");
    }

    @Test
    void testUploadCourtsReturnsNewCourts() throws IOException {

        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsv.csv")) {

            MultipartFile multipartFile = new MockMultipartFile("file",
                                                                "TestFileName", "text/plain",
                                                                IOUtils.toByteArray(inputStream));

            when(courtService.uploadCourts(multipartFile)).thenReturn(List.of(firstCourt, secondCourt));

            List<Court> returnedCourts = new ArrayList<>(courtController.uploadCourts(multipartFile).getBody());

            assertEquals(2, returnedCourts.size(), "Unexpected number of courts have been returned");
            assertTrue(returnedCourts.contains(firstCourt), "First court not contained within list");
            assertTrue(returnedCourts.contains(secondCourt), "Second court not contained within list");
        }

    }

    @Test
    void testUploadCourtsReturnsOk() throws IOException {

        try (InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("csv/ValidCsv.csv")) {

            MultipartFile multipartFile = new MockMultipartFile("file",
                                                                "TestFileName", "text/plain",
                                                                IOUtils.toByteArray(inputStream));

            when(courtService.uploadCourts(multipartFile)).thenReturn(List.of(firstCourt, secondCourt));

            assertEquals(HttpStatus.OK, courtController.uploadCourts(multipartFile).getStatusCode(),
                         "Upload courts endpoint has not returned OK");
        }

    }
}
