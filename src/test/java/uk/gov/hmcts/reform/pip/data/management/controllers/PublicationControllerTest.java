package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
    private static final String PAYLOAD = "payload";
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);
    private static final String PAYLOAD_URL = "This is a test payload";
    private static final String EMPTY_FIELD = "";
    private static final String VALIDATION_EXPECTED_MESSAGE =
        "The expected exception does not contain the correct message";
    private static final ListType LIST_TYPE = ListType.DL;
    private static final String COURT_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();

    private Artefact artefact;
    private Artefact artefactWithId;
    private HeaderGroup headers;

    @BeforeEach
    void setup() {
        headers = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                              DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, COURT_ID, CONTENT_DATE);
        artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .displayFrom(DISPLAY_FROM)
            .displayTo(DISPLAY_TO)
            .language(LANGUAGE)
            .provenance(PROVENANCE)
            .sensitivity(SENSITIVITY)
            .type(ARTEFACT_TYPE)
            .listType(LIST_TYPE)
            .courtId(COURT_ID)
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
            .courtId(COURT_ID)
            .contentDate(CONTENT_DATE)
            .build();
    }

    @Test
    void testCreationOfPublication() {
        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationService.createPublication(argThat(arg -> arg.equals(artefact)), eq(PAYLOAD)))
            .thenReturn(artefactWithId);



        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, COURT_ID, CONTENT_DATE, PAYLOAD
        );

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithId, responseEntity.getBody(), "The expected return ID is returned");
    }

    @Test
    void testGetMetadataEndpointReturnsOk() {
        assertEquals(HttpStatus.OK, publicationController.getArtefactMetadata(ARTEFACT_ID, true)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testGetPayloadEndpointReturnsOk() {
        assertEquals(HttpStatus.OK, publicationController.getArtefactPayload(ARTEFACT_ID, true)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testSearchEndpointReturnsOkWithTrue() {
        assertEquals(HttpStatus.OK, publicationController.getAllRelevantArtefactsByCourtId(EMPTY_FIELD, true)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testSearchEndpointReturnsOkWithFalse() {
        assertEquals(HttpStatus.OK, publicationController.getAllRelevantArtefactsByCourtId(EMPTY_FIELD, false)
            .getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void checkGetMetadataContentReturns() {
        when(publicationService.getMetadataByArtefactId(any(), any())).thenReturn(artefactWithId);
        ResponseEntity<Artefact> unmappedBlob = publicationController.getArtefactMetadata(UUID.randomUUID(), true);
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
        assertEquals(artefactWithId.toString(), unmappedBlob.getBody(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void checkGetByCourtReturnsOk() {
        List<Artefact> artefactList = List.of(artefactWithId);

        when(publicationService.findAllByCourtId(any(), any())).thenReturn(artefactList);
        ResponseEntity<List<Artefact>> unmappedArtefact = publicationController
            .getAllRelevantArtefactsByCourtId(EMPTY_FIELD, true);
        assertEquals(artefactList, unmappedArtefact.getBody(), VALIDATION_EXPECTED_MESSAGE);
        assertEquals(HttpStatus.OK, unmappedArtefact.getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testCreatePublicationMultipartFile() {
        Map<String, List<Object>> search = new HashMap<>();
        search.put("court-id", List.of(COURT_ID));
        artefact.setSearch(search);
        artefact.setIsFlatFile(true);
        artefactWithId.setIsFlatFile(true);
        artefactWithId.setSearch(search);

        when(validationService.validateHeaders(any())).thenReturn(headers);
        when(publicationService.createPublication(artefact, FILE)).thenReturn(artefactWithId);

        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, COURT_ID, CONTENT_DATE, FILE);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactWithId, responseEntity.getBody(), "Artefacts should match");
    }

}
