package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
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
    private static final String PAYLOAD_URL = "This is a test payload";
    private static final String EMPTY_FIELD = "";
    private static final String VALIDATION_EXPECTED_MESSAGE =
        "The expected exception does not contain the correct message";


    @Test
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    void testCreationOfPublication() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER,PROVENANCE);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER,SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER,LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER,DISPLAY_FROM);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER,DISPLAY_TO);

        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .displayFrom(DISPLAY_FROM)
            .displayTo(DISPLAY_TO)
            .language(LANGUAGE)
            .provenance(PROVENANCE)
            .sensitivity(SENSITIVITY)
            .type(ARTEFACT_TYPE)
            .build();

        Artefact artefactWithId = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .displayFrom(DISPLAY_FROM)
            .displayTo(DISPLAY_TO)
            .language(LANGUAGE)
            .provenance(PROVENANCE)
            .sensitivity(SENSITIVITY)
            .type(ARTEFACT_TYPE)
            .payload(PAYLOAD_URL)
            .build();

        when(validationService.validateHeaders(any())).thenReturn(headerMap);
        when(publicationService.createPublication(argThat(arg -> arg.equals(artefact)), eq(PAYLOAD)))
            .thenReturn(artefactWithId);



        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, PAYLOAD
        );

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), "A created status code is returned");
        assertEquals(artefactWithId, responseEntity.getBody(), "The expected return ID is returned");
    }





    @Test
    void testBlobEndpointReturnsOk() {
        assertEquals(HttpStatus.OK, publicationController.getBlobData(ARTEFACT_ID, true)
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
    void checkBodyBlobs() {
        Artefact artefactWithId = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .displayFrom(DISPLAY_FROM)
            .displayTo(DISPLAY_TO)
            .language(LANGUAGE)
            .provenance(PROVENANCE)
            .sensitivity(SENSITIVITY)
            .type(ARTEFACT_TYPE)
            .payload(PAYLOAD_URL)
            .build();
        when(publicationService.getByArtefactId(any(), any())).thenReturn(String.valueOf(artefactWithId));
        ResponseEntity<String> unmappedBlob = publicationController.getBlobData(UUID.randomUUID(), true);
        assertEquals(HttpStatus.OK, unmappedBlob.getStatusCode(),
                     STATUS_CODE_MATCH
        );
        assertEquals(artefactWithId.toString(), unmappedBlob.getBody(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void checkBodyArtefacts() {
        Artefact artefactWithId = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .displayFrom(DISPLAY_FROM)
            .displayTo(DISPLAY_TO)
            .language(LANGUAGE)
            .provenance(PROVENANCE)
            .sensitivity(SENSITIVITY)
            .type(ARTEFACT_TYPE)
            .payload(PAYLOAD_URL)
            .build();
        List<Artefact> artefactList = List.of(artefactWithId);

        when(publicationService.findAllByCourtId(any(), any())).thenReturn(artefactList);
        ResponseEntity<List<Artefact>> unmappedArtefact = publicationController
            .getAllRelevantArtefactsByCourtId(EMPTY_FIELD, true);
        assertEquals(unmappedArtefact.getBody(), artefactList, VALIDATION_EXPECTED_MESSAGE);
        assertEquals(unmappedArtefact.getStatusCode(), HttpStatus.OK, STATUS_CODE_MATCH);

    }

}
