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
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactDeleteService;
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactSearchService;
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactService;
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactTriggerService;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.STATUS_CODE_MATCH;

@SuppressWarnings({"PMD.UseConcurrentHashMap", "PMD.ExcessiveImports", "PMD.TooManyMethods"})
@ExtendWith(MockitoExtension.class)
class PublicationControllerTest {

    @Mock
    private PublicationService publicationService;

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
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);
    private static final String PAYLOAD_URL = "This is a test payload";
    private static final CaseSearchTerm SEARCH_TERM = CaseSearchTerm.CASE_ID;
    private static final String EMPTY_FIELD = "";
    private static final String TEST_STRING = "test";
    private static final String VALIDATION_EXPECTED_MESSAGE =
        "The expected exception does not contain the correct message";
    private static final String NOT_EQUAL_MESSAGE = "The expected strings are not the same";
    private static final String DELETED_MESSAGE = "Successfully deleted artefact: ";
    private static final String COUNT_MSG = "location,count\n1,3\n2,4\n3,6\n";
    private static final String NO_MATCH = "NoMatch";

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
            .expiryDate(DISPLAY_TO)
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
            .expiryDate(DISPLAY_TO)
            .search(new ConcurrentHashMap<>())
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
            .expiryDate(DISPLAY_TO)
            .search(new ConcurrentHashMap<>())
            .build();
    }

    @Test
    void testCreationOfPublication() {
        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationService.createPublication(argThat(arg -> arg.equals(artefact)), eq(PAYLOAD)))
            .thenReturn(artefactWithId);


        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
            DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE, TEST_STRING, PAYLOAD
        );

        verify(publicationService).processCreatedPublication(any(Artefact.class));

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithId, responseEntity.getBody(), "The expected return ID is returned");
    }

    @Test
    void testCreationOfPublicationWithNonExistentLocationId() {
        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationService.createPublication(argThat(arg -> arg.equals(artefact)), eq(PAYLOAD)))
            .thenReturn(artefactWithNoMatchLocationId);

        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
            DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE, TEST_STRING, PAYLOAD
        );

        verify(publicationService, never()).processCreatedPublication(any(Artefact.class));

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithNoMatchLocationId, responseEntity.getBody(), "The expected return ID is returned");
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
        ResponseEntity<List<LocationArtefact>> result = publicationController.countByLocation();
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
        artefactWithId.setIsFlatFile(true);
        artefactWithId.setSearch(search);

        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationService.createPublication(artefact, FILE)).thenReturn(artefactWithId);

        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE, TEST_STRING, FILE
        );

        verify(artefactTriggerService).checkAndTriggerSubscriptionManagement(any(Artefact.class));

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithId, responseEntity.getBody(), "Artefacts should match");
    }

    @Test
    void testCreatePublicationMultipartFileWithNonExistentLocationId() {
        Map<String, List<Object>> search = new HashMap<>();
        search.put("location-id", List.of(LOCATION_ID));
        artefact.setSearch(search);
        artefact.setIsFlatFile(true);
        artefactWithNoMatchLocationId.setIsFlatFile(true);
        artefactWithNoMatchLocationId.setSearch(search);

        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationService.createPublication(artefact, FILE)).thenReturn(artefactWithNoMatchLocationId);

        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE, TEST_STRING, FILE
        );

        verify(artefactTriggerService, never()).checkAndTriggerSubscriptionManagement(any(Artefact.class));

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithNoMatchLocationId, responseEntity.getBody(), "Artefacts should match");
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
        when(publicationService.createPublication(argThat(arg -> arg.equals(artefact)), eq(PAYLOAD)))
            .thenReturn(artefactWithId);
        when(publicationService.maskEmail(TEST_STRING)).thenReturn(TEST_STRING);


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
    void testMiDataReturnsOk() {
        assertEquals(
            HttpStatus.OK,
            publicationController.getMiData().getStatusCode(),
            STATUS_CODE_MATCH
        );
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
        String requesterName = "ReqName";
        when(artefactDeleteService.deleteArtefactByLocation(locationId, requesterName)).thenReturn("Success");

        assertEquals(HttpStatus.OK,
                     publicationController.deleteArtefactsByLocation(requesterName, locationId).getStatusCode(),
                     "Delete artefacts for location endpoint has not returned OK");
    }

    @Test
    void testGetAllNoMatchArtefacts() {
        List<Artefact> artefactList = List.of(artefactWithId);

        when(artefactService.findAllNoMatchArtefacts()).thenReturn(artefactList);

        ResponseEntity<List<Artefact>> response = publicationController.getAllNoMatchArtefacts();

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactList, response.getBody(), "Body should match");
    }
}
