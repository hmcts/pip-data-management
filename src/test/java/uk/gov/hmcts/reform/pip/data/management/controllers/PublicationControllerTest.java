package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.service.ExcelConversionService;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactDeleteService;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactSearchService;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactService;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactTriggerService;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationCreationRunner;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationService;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.STATUS_CODE_MATCH;

@SuppressWarnings({"PMD.UseConcurrentHashMap", "PMD.ExcessiveImports",
    "PMD.TooManyMethods", "PMD.CouplingBetweenObjects"})
@ExtendWith(MockitoExtension.class)
class PublicationControllerTest {

    @Mock
    private PublicationService publicationService;

    @Mock
    private PublicationCreationRunner publicationCreationRunner;

    @Mock
    private ArtefactSearchService artefactSearchService;

    @Mock
    private ArtefactService artefactService;

    @Mock
    private ArtefactDeleteService artefactDeleteService;

    @Mock
    private ArtefactTriggerService artefactTriggerService;

    @Mock
    private ValidationService validationService;

    @Mock
    private ExcelConversionService excelConversionService;

    @InjectMocks
    private PublicationController publicationController;

    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String PROVENANCE = "provenance";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final String LOCATION_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String PAYLOAD = "payload";
    private static final Float PAYLOAD_SIZE = (float) PAYLOAD.getBytes().length / 1024;
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);
    private static final String PAYLOAD_URL = "This is a test payload";
    private static final CaseSearchTerm SEARCH_TERM = CaseSearchTerm.CASE_ID;
    private static final String EMPTY_FIELD = "";
    private static final String TEST_STRING = "test";
    private static final String VALIDATION_EXPECTED_MESSAGE =
        "The expected exception does not contain the correct message";
    private static final String ARTEFACT_MATCH_MESSAGE = "Artefact does not match";
    private static final String NOT_EQUAL_MESSAGE = "The expected strings are not the same";
    private static final String DELETED_MESSAGE = "Successfully deleted artefact: ";
    private static final String NO_MATCH = "NoMatch";
    private static final String FILE_NAME = "TestFileName";
    private static final String EXCEL_FILE_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final List<LocationArtefact> COURT_PER_LOCATION = new ArrayList<>();
    private Artefact artefact;
    private Artefact artefactWithId;
    private Artefact artefactWithNoMatchLocationId;
    private HeaderGroup headers;

    @BeforeEach
    void setup() {
        headers = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                  DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE
        );
        artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .displayFrom(DISPLAY_FROM)
            .displayTo(DISPLAY_TO)
            .language(LANGUAGE)
            .provenance(PROVENANCE)
            .sensitivity(SENSITIVITY)
            .type(ARTEFACT_TYPE)
            .listType(LIST_TYPE)
            .locationId(LOCATION_ID)
            .contentDate(CONTENT_DATE)
            .payloadSize(PAYLOAD_SIZE)
            .build();

        artefactWithId = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .displayFrom(DISPLAY_FROM)
            .displayTo(DISPLAY_TO)
            .language(LANGUAGE)
            .provenance(PROVENANCE)
            .sensitivity(SENSITIVITY)
            .type(ARTEFACT_TYPE)
            .payload(PAYLOAD_URL)
            .listType(LIST_TYPE)
            .locationId(LOCATION_ID)
            .contentDate(CONTENT_DATE)
            .search(new ConcurrentHashMap<>())
            .payloadSize(PAYLOAD_SIZE)
            .build();

        artefactWithNoMatchLocationId = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .displayFrom(DISPLAY_FROM)
            .displayTo(DISPLAY_TO)
            .language(LANGUAGE)
            .provenance(PROVENANCE)
            .sensitivity(SENSITIVITY)
            .type(ARTEFACT_TYPE)
            .payload(PAYLOAD_URL)
            .listType(LIST_TYPE)
            .locationId(NO_MATCH + LOCATION_ID)
            .contentDate(CONTENT_DATE)
            .search(new ConcurrentHashMap<>())
            .payloadSize(PAYLOAD_SIZE)
            .build();


    }

    @Test
    void testCreationOfPublication() {
        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationCreationRunner.run(artefact, PAYLOAD, true)).thenReturn(artefactWithId);

        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
            DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE, TEST_STRING, PAYLOAD
        );

        verify(publicationService).processCreatedPublication(any(Artefact.class), eq(PAYLOAD));

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithId, responseEntity.getBody(), ARTEFACT_MATCH_MESSAGE);
    }

    @Test
    void testCreationOfPublicationWithNonExistentLocationId() {
        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationCreationRunner.run(artefact, PAYLOAD, true)).thenReturn(artefactWithNoMatchLocationId);

        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
            DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE, TEST_STRING, PAYLOAD
        );

        verify(publicationService, never()).processCreatedPublication(any(Artefact.class), eq(PAYLOAD));

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithNoMatchLocationId, responseEntity.getBody(), ARTEFACT_MATCH_MESSAGE);
    }

    @Test
    void testGetMetadataEndpointReturnsOk() {
        assertEquals(HttpStatus.OK, publicationController.getArtefactMetadata(ARTEFACT_ID, USER_ID, false)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testGetPayloadEndpointReturnsOkWhenAdmin() {
        assertEquals(HttpStatus.OK, publicationController.getArtefactPayload(ARTEFACT_ID, USER_ID, true)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testGetPayloadEndpointReturnsOkWhenNotAdmin() {
        assertEquals(HttpStatus.OK, publicationController.getArtefactPayload(ARTEFACT_ID, USER_ID, false)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testSearchEndpointReturnsOkWithTrue() {
        assertEquals(HttpStatus.OK, publicationController.getAllRelevantArtefactsByLocationId(
                EMPTY_FIELD, USER_ID, true)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testSearchEndpointReturnsOkWithFalse() {
        assertEquals(HttpStatus.OK, publicationController.getAllRelevantArtefactsByLocationId(
                EMPTY_FIELD, USER_ID, false)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testGetArtefactsBySearchReturnsWhenTrue() {
        when(artefactSearchService.findAllBySearch(SEARCH_TERM, TEST_STRING, USER_ID))
            .thenReturn(List.of(artefactWithId));
        assertEquals(HttpStatus.OK, publicationController.getAllRelevantArtefactsBySearchValue(SEARCH_TERM, TEST_STRING,
                                                                                               USER_ID
                     ).getStatusCode(),
                     STATUS_CODE_MATCH
        );
    }

    @Test
    void testGetArtefactsBySearchReturnsWhenFalse() {
        when(artefactSearchService.findAllBySearch(SEARCH_TERM, TEST_STRING, USER_ID))
            .thenReturn(List.of(artefactWithId));
        assertEquals(HttpStatus.OK, publicationController
                         .getAllRelevantArtefactsBySearchValue(SEARCH_TERM, TEST_STRING, USER_ID)
                         .getStatusCode(),
                     STATUS_CODE_MATCH
        );
    }

    @Test
    void checkGetMetadataContentReturns() {
        when(artefactService.getMetadataByArtefactId(any(), any()))
            .thenReturn(artefactWithId);
        ResponseEntity<Artefact> unmappedBlob = publicationController
            .getArtefactMetadata(UUID.randomUUID(), USER_ID, false);

        assertEquals(HttpStatus.OK, unmappedBlob.getStatusCode(),
                     STATUS_CODE_MATCH
        );
        assertEquals(artefactWithId, unmappedBlob.getBody(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void checkGetMetadataContentReturnsAdmin() {
        when(artefactService.getMetadataByArtefactId(any()))
            .thenReturn(artefactWithId);
        ResponseEntity<Artefact> unmappedBlob = publicationController
            .getArtefactMetadata(UUID.randomUUID(), USER_ID, true);

        assertEquals(HttpStatus.OK, unmappedBlob.getStatusCode(),
                     STATUS_CODE_MATCH
        );
        assertEquals(artefactWithId, unmappedBlob.getBody(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void checkCountArtefactByLocationReturnsData() {
        COURT_PER_LOCATION.add(new LocationArtefact("1", 2));
        when(artefactService.countArtefactsByLocation()).thenReturn(COURT_PER_LOCATION);
        ResponseEntity<List<LocationArtefact>> result = publicationController.countByLocation("123-456");
        assertEquals(HttpStatus.OK, result.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(COURT_PER_LOCATION, result.getBody(), NOT_EQUAL_MESSAGE);
    }


    @Test
    void checkGetPayloadContentReturns() {
        when(artefactService.getPayloadByArtefactId(any(), any())).thenReturn(String.valueOf(artefactWithId));
        ResponseEntity<String> unmappedBlob =
            publicationController.getArtefactPayload(UUID.randomUUID(), USER_ID, false);
        assertEquals(HttpStatus.OK, unmappedBlob.getStatusCode(),
                     STATUS_CODE_MATCH
        );
        assertEquals(artefactWithId.toString(), unmappedBlob.getBody(), NOT_EQUAL_MESSAGE);
    }


    @Test
    void checkGetFileContentReturns() {
        String string = "Hello";
        byte[] testData = string.getBytes();
        when(artefactService.getFlatFileByArtefactID(any(), any())).thenReturn(new ByteArrayResource(testData));
        when(artefactService.getMetadataByArtefactId(any(), any())).thenReturn(artefactWithId);
        ResponseEntity<Resource> flatFileBlob = publicationController.getArtefactFile(
            UUID.randomUUID(),
            USER_ID,
            false
        );
        assertEquals(HttpStatus.OK, flatFileBlob.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(new ByteArrayResource(testData), flatFileBlob.getBody(), NOT_EQUAL_MESSAGE);
        String filename = flatFileBlob.getHeaders().get("Content-Disposition").toString();
        assertTrue(filename.contains(artefactWithId.getSourceArtefactId()), NOT_EQUAL_MESSAGE);
    }

    @Test
    void checkGetFileContentAdminReturns() {
        String string = "Hello";
        byte[] testData = string.getBytes();
        when(artefactService.getFlatFileByArtefactID(any())).thenReturn(new ByteArrayResource(testData));
        when(artefactService.getMetadataByArtefactId(any())).thenReturn(artefactWithId);
        ResponseEntity<Resource> flatFileBlob = publicationController.getArtefactFile(
            UUID.randomUUID(),
            USER_ID,
            true
        );
        assertEquals(HttpStatus.OK, flatFileBlob.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(new ByteArrayResource(testData), flatFileBlob.getBody(), NOT_EQUAL_MESSAGE);
        String filename = flatFileBlob.getHeaders().get("Content-Disposition").toString();
        assertTrue(filename.contains(artefactWithId.getSourceArtefactId()), NOT_EQUAL_MESSAGE);
    }

    @Test
    void checkGetArtefactsByCourtIdReturnsWhenTrue() {
        List<Artefact> artefactList = List.of(artefactWithId);

        when(artefactSearchService.findAllByLocationIdAdmin(EMPTY_FIELD, USER_ID, true)).thenReturn(artefactList);
        ResponseEntity<List<Artefact>> unmappedArtefact = publicationController
            .getAllRelevantArtefactsByLocationId(EMPTY_FIELD, USER_ID, true);

        assertEquals(artefactList, unmappedArtefact.getBody(), VALIDATION_EXPECTED_MESSAGE);
        assertEquals(HttpStatus.OK, unmappedArtefact.getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void checkGetArtefactsByCourtIdReturnsOkWhenFalse() {
        List<Artefact> artefactList = List.of(artefactWithId);

        when(artefactSearchService.findAllByLocationIdAdmin(EMPTY_FIELD, USER_ID, false)).thenReturn(artefactList);
        ResponseEntity<List<Artefact>> unmappedArtefact = publicationController
            .getAllRelevantArtefactsByLocationId(EMPTY_FIELD, USER_ID, false);

        assertEquals(artefactList, unmappedArtefact.getBody(), VALIDATION_EXPECTED_MESSAGE);
        assertEquals(HttpStatus.OK, unmappedArtefact.getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testCreatePublicationMultipartFile() {
        Map<String, List<Object>> search = new HashMap<>();
        search.put("location-id", List.of(LOCATION_ID));
        artefact.setSearch(search);
        artefact.setIsFlatFile(true);
        artefact.setPayloadSize(0f);
        artefactWithId.setIsFlatFile(true);
        artefactWithId.setSearch(search);
        artefactWithId.setPayloadSize(0f);

        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationCreationRunner.run(artefact, FILE)).thenReturn(artefactWithId);

        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE, TEST_STRING, FILE
        );

        verify(artefactTriggerService).checkAndTriggerPublicationSubscription(any(Artefact.class));

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithId, responseEntity.getBody(), ARTEFACT_MATCH_MESSAGE);
    }

    @Test
    void testCreatePublicationMultipartFileWithNonExistentLocationId() {
        Map<String, List<Object>> search = new HashMap<>();
        search.put("location-id", List.of(LOCATION_ID));
        artefact.setSearch(search);
        artefact.setIsFlatFile(true);
        artefact.setPayloadSize(0f);
        artefactWithNoMatchLocationId.setIsFlatFile(true);
        artefactWithNoMatchLocationId.setSearch(search);
        artefactWithNoMatchLocationId.setPayloadSize(0f);

        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationCreationRunner.run(artefact, FILE)).thenReturn(artefactWithNoMatchLocationId);

        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE, TEST_STRING, FILE
        );

        verify(artefactTriggerService, never()).checkAndTriggerPublicationSubscription(any(Artefact.class));

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithNoMatchLocationId, responseEntity.getBody(), ARTEFACT_MATCH_MESSAGE);
    }

    @Test
    void testNonStrategicCreatePublication() {
        MultipartFile file = new MockMultipartFile(FILE_NAME, FILE_NAME, EXCEL_FILE_TYPE,
                                                   TEST_STRING.getBytes(StandardCharsets.UTF_8));

        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(excelConversionService.convert(file)).thenReturn(PAYLOAD);
        when(publicationCreationRunner.run(artefact, PAYLOAD, false)).thenReturn(artefactWithId);

        ResponseEntity<Artefact> responseEntity = publicationController.nonStrategicUploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO,
            LIST_TYPE, LOCATION_ID, CONTENT_DATE, TEST_STRING, file
        );

        verify(publicationService).processCreatedPublication(artefactWithId, PAYLOAD);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithId, responseEntity.getBody(), ARTEFACT_MATCH_MESSAGE);
    }

    @Test
    void testNonStrategicCreatePublicationWithNonExistentLocationId() {
        MultipartFile file = new MockMultipartFile(FILE_NAME, FILE_NAME, EXCEL_FILE_TYPE,
                                                   TEST_STRING.getBytes(StandardCharsets.UTF_8));

        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(excelConversionService.convert(file)).thenReturn(PAYLOAD);
        when(publicationCreationRunner.run(artefact, PAYLOAD, false))
            .thenReturn(artefactWithNoMatchLocationId);

        ResponseEntity<Artefact> responseEntity = publicationController.nonStrategicUploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO,
            LIST_TYPE, LOCATION_ID, CONTENT_DATE, TEST_STRING, file
        );

        verify(publicationService, never()).processCreatedPublication(artefactWithId, PAYLOAD);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithNoMatchLocationId, responseEntity.getBody(), ARTEFACT_MATCH_MESSAGE);
    }

    @Test
    void testDeleteArtefactReturnsOk() {
        doNothing().when(artefactDeleteService).deleteArtefactById(any(), any());
        assertEquals(HttpStatus.OK, publicationController.deleteArtefact(TEST_STRING, TEST_STRING).getStatusCode(),
                     STATUS_CODE_MATCH
        );
        assertEquals(
            DELETED_MESSAGE + TEST_STRING,
            publicationController.deleteArtefact(TEST_STRING, TEST_STRING).getBody(),
            MESSAGES_MATCH
        );
    }

    @Test
    void testGetLocationTypeReturnsOk() {
        when(artefactService.getLocationType(ListType.CIVIL_DAILY_CAUSE_LIST)).thenReturn(LocationType.VENUE);
        assertEquals(
            HttpStatus.OK,
            publicationController.getLocationType(ListType.CIVIL_DAILY_CAUSE_LIST).getStatusCode(),
            STATUS_CODE_MATCH
        );
    }

    @Test
    void testCreatePublicationLogsWhenHeaderIsPresent() throws IOException {
        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationCreationRunner.run(artefact, PAYLOAD, true)).thenReturn(artefactWithId);

        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationController.class)) {
            publicationController.uploadPublication(
                PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
                SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE, TEST_STRING,
                PAYLOAD
            );
            assertEquals(1, logCaptor.getInfoLogs().size(), "Should have logged upload");
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Test
    void testSendNewArtefactsForSubscriptionSuccess() {
        doNothing().when(artefactTriggerService).checkNewlyActiveArtefacts();
        assertThat(publicationController.sendNewArtefactsForSubscription().getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testReportNoMatchArtefactsSuccess() {
        doNothing().when(artefactTriggerService).reportNoMatchArtefacts();
        assertThat(publicationController.reportNoMatchArtefacts().getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testDeleteExpiredArtefactsSuccess() {
        doNothing().when(artefactDeleteService).archiveExpiredArtefacts();
        assertThat(publicationController.archiveExpiredArtefacts().getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testArchiveArtefact() {
        String issuerId = UUID.randomUUID().toString();
        String artefactId = UUID.randomUUID().toString();

        doNothing().when(artefactDeleteService).archiveArtefactById(artefactId, issuerId);

        ResponseEntity<String> response = publicationController.archiveArtefact(issuerId, artefactId);

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(String.format("Artefact of ID %s has been archived", artefactId),
                     response.getBody(), "Response from archiving does not match expected message"
        );
    }

    @Test
    void testDeleteArtefactsByLocationReturnsOk() throws JsonProcessingException {
        int locationId = 1;
        String requesterId = UUID.randomUUID().toString();
        when(artefactDeleteService.deleteArtefactByLocation(locationId, requesterId)).thenReturn("Success");

        assertEquals(HttpStatus.OK,
                     publicationController.deleteArtefactsByLocation(requesterId, locationId).getStatusCode(),
                     "Delete artefacts for location endpoint has not returned OK");
    }

    @Test
    void testGetAllNoMatchArtefacts() {
        List<Artefact> artefactList = List.of(artefactWithId);

        when(artefactService.findAllNoMatchArtefacts()).thenReturn(artefactList);

        ResponseEntity<List<Artefact>> response = publicationController.getAllNoMatchArtefacts(USER_ID.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactList, response.getBody(), "Body should match");
    }

    @Test
    void testMiDataReturnsSuccessfully() {
        PublicationMiData publicationMiData = new PublicationMiData(
            UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), Language.ENGLISH, "MANUAL_UPLOAD",
            Sensitivity.PUBLIC, UUID.randomUUID().toString(), 0, ArtefactType.GENERAL_PUBLICATION,
            LocalDateTime.now(),"1", ListType.CIVIL_DAILY_CAUSE_LIST);

        PublicationMiData publicationMiData2 = new PublicationMiData(
            UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), Language.ENGLISH, "MANUAL_UPLOAD",
            Sensitivity.PUBLIC, UUID.randomUUID().toString(), 1, ArtefactType.GENERAL_PUBLICATION,
            LocalDateTime.now(), "NoMatch2", ListType.CIVIL_DAILY_CAUSE_LIST);

        when(publicationService.getMiData()).thenReturn(List.of(publicationMiData, publicationMiData2));

        ResponseEntity<List<PublicationMiData>> response = publicationController.getMiData();

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_CODE_MATCH);
        assertThat(response.getBody()).containsExactlyInAnyOrder(publicationMiData, publicationMiData2);
    }

}
