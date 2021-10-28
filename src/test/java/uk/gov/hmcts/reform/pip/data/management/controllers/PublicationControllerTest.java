package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.InvalidPublicationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationControllerTest {

    @Mock
    private PublicationService publicationService;

    @InjectMocks
    private PublicationController publicationController;

    private static final String ARTEFACT_ID = "artefactId";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String PROVENANCE = "provenance";
    private static final String SEARCH = "CASE_ID=1234";
    private static final String INVALID_SEARCH_ENUM = "CASE_IDS=1234";
    private static final String INVALID_SEARCH_VALUE = "CASE_IDS";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String PAYLOAD = "payload";

    private static final String RETURN_ID = "1234";


    @Test
    void testCreationOfPublication() {
        Artefact artefact = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .displayFrom(DISPLAY_FROM)
            .displayTo(DISPLAY_TO)
            .language(LANGUAGE)
            .provenance(PROVENANCE)
            .search(SEARCH)
            .sensitivity(SENSITIVITY)
            .type(ARTEFACT_TYPE)
            .payload(PAYLOAD)
            .build();

        when(publicationService.createPublication(argThat(arg -> arg.equals(artefact)))).thenReturn(RETURN_ID);

        ResponseEntity<String> responseEntity = publicationController.uploadPublication(
            ARTEFACT_ID, PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, SEARCH, DISPLAY_FROM, DISPLAY_TO, PAYLOAD
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "An OK status code is returned");
        assertEquals(RETURN_ID, responseEntity.getBody(), "The expected return ID is returned");
    }

    @Test
    void testCreationWithInvalidSearchEnum() {
        Exception exception = assertThrows(InvalidPublicationException.class, () ->
            publicationController.uploadPublication(
            ARTEFACT_ID, PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, INVALID_SEARCH_ENUM, DISPLAY_FROM, DISPLAY_TO, PAYLOAD
        ));

        assertEquals(String.format("Invalid search parameter provided %s", INVALID_SEARCH_ENUM),
                     exception.getMessage(), "Correct error message is displayed");
    }

    @Test
    void testCreationWithInvalidSearchValue() {
        Exception exception = assertThrows(InvalidPublicationException.class, () ->
            publicationController.uploadPublication(
            ARTEFACT_ID, PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE,
            SENSITIVITY, LANGUAGE, INVALID_SEARCH_VALUE, DISPLAY_FROM, DISPLAY_TO, PAYLOAD
        ));

        assertEquals(String.format("Invalid search parameter provided %s", INVALID_SEARCH_VALUE),
                     exception.getMessage(), "Correct error message is displayed");
    }
}
