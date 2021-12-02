package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.EmptyRequestHeaderException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationControllerTest {

    @Mock
    private PublicationService publicationService;

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
    void testCreationOfPublication() {
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

        when(publicationService.createPublication(argThat(arg -> arg.equals(artefact)), eq(PAYLOAD)))
            .thenReturn(artefactWithId);

        ResponseEntity<Artefact> responseEntity = publicationController.uploadPublication(
            PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, PAYLOAD
        );

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), "An OK status code is returned");
        assertEquals(artefactWithId, responseEntity.getBody(), "The expected return ID is returned");
    }

    @Test
    void testCreationOfPublicationEmptyProvenance() {
        EmptyRequestHeaderException emptyRequestHeaderException =
            assertThrows(EmptyRequestHeaderException.class, () -> {
                publicationController.uploadPublication(
                    EMPTY_FIELD, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
                    SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, PAYLOAD
                );
            });

        assertEquals("x-provenance is mandatory however an empty value is provided",
                     emptyRequestHeaderException.getMessage(),
                     VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testCreationOfPublicationEmptySourceArtefactId() {
        EmptyRequestHeaderException emptyRequestHeaderException =
            assertThrows(EmptyRequestHeaderException.class, () -> {
                publicationController.uploadPublication(
                    PROVENANCE, EMPTY_FIELD, ARTEFACT_TYPE,
                    SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, PAYLOAD
                );
            });

        assertEquals("x-source-artefact-id is mandatory however an empty value is provided",
                     emptyRequestHeaderException.getMessage(),
                     VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testCreationOfPublicationEmptySourceArtefactIdAndEmptyProvenanceOnlyFirstIsShown() {
        EmptyRequestHeaderException emptyRequestHeaderException =
            assertThrows(EmptyRequestHeaderException.class, () -> {
                publicationController.uploadPublication(
                    EMPTY_FIELD, EMPTY_FIELD, ARTEFACT_TYPE,
                    SENSITIVITY, LANGUAGE, DISPLAY_FROM, DISPLAY_TO, PAYLOAD
                );
            });

        assertEquals("x-provenance is mandatory however an empty value is provided",
                     emptyRequestHeaderException.getMessage(),
                     VALIDATION_EXPECTED_MESSAGE);
    }
}
