package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PublicationControllerTest {

    @Mock
    private PublicationService publicationService;

    @InjectMocks
    private PublicationController publicationController;

    private Artefact artefact;

    private final String artefactId = "artefactId";
    private final String sourceArtefactId = "sourceArtefactId";
    private final LocalDateTime displayFrom = LocalDateTime.now();
    private final LocalDateTime displayTo = LocalDateTime.now();
    private final Language language = Language.ENGLISH;
    private final String provenance = "provenance";
    private final String search = "search";
    private final Sensitivity sensitivity = Sensitivity.PUBLIC;
    private final ArtefactType artefactType = ArtefactType.LIST;
    private final String payload = "payload";

    private final String returnId = "1234";

    @BeforeEach
    void setup() {

        artefact = Artefact.builder()
            .artefactId(artefactId)
            .sourceArtefactId(sourceArtefactId)
            .displayFrom(displayFrom)
            .displayTo(displayTo)
            .language(language)
            .provenance(provenance)
            .search(search)
            .sensitivity(sensitivity)
            .type(artefactType)
            .payload(payload)
            .build();

        when(publicationService.createPublication(argThat(arg -> arg.equals(artefact)))).thenReturn(returnId);
    }

    @Test
    void testCreationOfPublication() {
        ResponseEntity<String> responseEntity = publicationController.uploadPublication(
            artefactId, provenance,sourceArtefactId, artefactType,
            sensitivity, language, search, displayFrom, displayTo, payload);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(returnId, responseEntity.getBody());
    }
}
