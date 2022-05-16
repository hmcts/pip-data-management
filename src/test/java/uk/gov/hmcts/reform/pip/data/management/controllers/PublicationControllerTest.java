package uk.gov.hmcts.reform.pip.data.management.controllers;

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
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.TestConstants.MESSAGES_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.TestConstants.STATUS_CODE_MATCH;

@SuppressWarnings("PMD.UseConcurrentHashMap")
@ExtendWith(MockitoExtension.class)
class PublicationControllerTest {

    @Mock
    private PublicationService publicationService;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private PublicationController publicationController;

    private static final UUID ARTEFACT_ID = UUID.randomUUID();
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

    private Artefact artefact;
    private Artefact artefactWithId;
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
            .build();
    }

    @Test
    void testCreationOfPublication() {
        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationService.createPublication(argThat(arg -> arg.equals(artefact)), eq(PAYLOAD)))
            .thenReturn(artefactWithId);


        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE, PAYLOAD
        );

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithId, responseEntity.getBody(), "The expected return ID is returned");
    }

    @Test
    void testGetMetadataEndpointReturnsOk() {
        assertEquals(HttpStatus.OK, publicationController.getArtefactMetadata(ARTEFACT_ID, true, false)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testGetPayloadEndpointReturnsOk() {
        assertEquals(HttpStatus.OK, publicationController.getArtefactPayload(ARTEFACT_ID, true)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testSearchEndpointReturnsOkWithTrue() {
        assertEquals(HttpStatus.OK, publicationController.getAllRelevantArtefactsByCourtId(EMPTY_FIELD, true, true)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testSearchEndpointReturnsOkWithFalse() {
        assertEquals(HttpStatus.OK, publicationController.getAllRelevantArtefactsByCourtId(EMPTY_FIELD, false, false)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testGetArtefactsBySearchReturnsWhenTrue() {
        when(publicationService.findAllBySearch(SEARCH_TERM, TEST_STRING, true)).thenReturn(List.of(artefactWithId));
        assertEquals(HttpStatus.OK, publicationController.getAllRelevantArtefactsBySearchValue(SEARCH_TERM, TEST_STRING,
                                                                                               true).getStatusCode(),
                     STATUS_CODE_MATCH);
    }

    @Test
    void testGetArtefactsBySearchReturnsWhenFalse() {
        when(publicationService.findAllBySearch(SEARCH_TERM, TEST_STRING, false)).thenReturn(List.of(artefactWithId));
        assertEquals(HttpStatus.OK, publicationController.getAllRelevantArtefactsBySearchValue(SEARCH_TERM, TEST_STRING,
                                                                                               false).getStatusCode(),
                     STATUS_CODE_MATCH);
    }

    @Test
    void checkGetMetadataContentReturns() {
        when(publicationService.getMetadataByArtefactId(any(), anyBoolean()))
            .thenReturn(artefactWithId);
        ResponseEntity<Artefact> unmappedBlob = publicationController
            .getArtefactMetadata(UUID.randomUUID(), true, false);

        assertEquals(HttpStatus.OK, unmappedBlob.getStatusCode(),
                     STATUS_CODE_MATCH
        );
        assertEquals(artefactWithId, unmappedBlob.getBody(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void checkGetMetadataContentReturnsAdmin() {
        when(publicationService.getMetadataByArtefactId(any()))
            .thenReturn(artefactWithId);
        ResponseEntity<Artefact> unmappedBlob = publicationController
            .getArtefactMetadata(UUID.randomUUID(), true, true);

        assertEquals(HttpStatus.OK, unmappedBlob.getStatusCode(),
                     STATUS_CODE_MATCH
        );
        assertEquals(artefactWithId, unmappedBlob.getBody(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void checkGetPayloadContentReturns() {
        when(publicationService.getPayloadByArtefactId(any(), any())).thenReturn(String.valueOf(artefactWithId));
        ResponseEntity<String> unmappedBlob = publicationController.getArtefactPayload(UUID.randomUUID(), true);
        assertEquals(HttpStatus.OK, unmappedBlob.getStatusCode(),
                     STATUS_CODE_MATCH
        );
        assertEquals(artefactWithId.toString(), unmappedBlob.getBody(), NOT_EQUAL_MESSAGE);
    }


    @Test
    void checkGetFileContentReturns() {
        String string = "Hello";
        byte[] testData = string.getBytes();
        when(publicationService.getFlatFileByArtefactID(any(), any())).thenReturn(new ByteArrayResource(testData));
        when(publicationService.getMetadataByArtefactId(any(), any())).thenReturn(artefactWithId);
        ResponseEntity<Resource> flatFileBlob = publicationController.getArtefactFile(
            UUID.randomUUID(),
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

        when(publicationService.findAllByCourtIdAdmin(EMPTY_FIELD, true, true)).thenReturn(artefactList);
        ResponseEntity<List<Artefact>> unmappedArtefact = publicationController
            .getAllRelevantArtefactsByCourtId(EMPTY_FIELD, true, true);

        assertEquals(artefactList, unmappedArtefact.getBody(), VALIDATION_EXPECTED_MESSAGE);
        assertEquals(HttpStatus.OK, unmappedArtefact.getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void checkGetArtefactsByCourtIdReturnsOkWhenFalse() {
        List<Artefact> artefactList = List.of(artefactWithId);

        when(publicationService.findAllByCourtIdAdmin(EMPTY_FIELD, false, false)).thenReturn(artefactList);
        ResponseEntity<List<Artefact>> unmappedArtefact = publicationController
            .getAllRelevantArtefactsByCourtId(EMPTY_FIELD, false, false);

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
            SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, LOCATION_ID, CONTENT_DATE, FILE
        );

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithId, responseEntity.getBody(), "Artefacts should match");
    }

    @Test
    void testDeleteArtefactReturnsOk() {
        doNothing().when(publicationService).deleteArtefactById(any(), any());
        assertEquals(HttpStatus.OK, publicationController.deleteArtefact(TEST_STRING, TEST_STRING).getStatusCode(),
                     STATUS_CODE_MATCH);
        assertEquals(DELETED_MESSAGE + TEST_STRING,
                     publicationController.deleteArtefact(TEST_STRING, TEST_STRING).getBody(),
                     MESSAGES_MATCH);
    }

    @Test
    void testGetLocationTypeReturnsOk() {
        when(publicationService.getLocationType(ListType.CIVIL_DAILY_CAUSE_LIST)).thenReturn(LocationType.VENUE);
        assertEquals(HttpStatus.OK,
                     publicationController.getLocationType(ListType.CIVIL_DAILY_CAUSE_LIST).getStatusCode(),
                     STATUS_CODE_MATCH);
    }

}
