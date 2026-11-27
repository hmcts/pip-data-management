package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.PublicationWithRelatedData;
import uk.gov.hmcts.reform.pip.data.management.service.MigrationSupportService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrationSupportControllerTest {
    private static final LocalDate FROM = LocalDate.of(2024, 1, 10);
    private static final LocalDate TO = LocalDate.of(2024, 1, 20);

    @Mock
    private MigrationSupportService migrationSupportService;

    @InjectMocks
    private MigrationSupportController migrationSupportController;

    @Test
    void testGetAllPublicationsReturnsOk() {
        List<PublicationWithRelatedData> publications = List.of(
            new PublicationWithRelatedData(new Artefact(), List.of())
        );
        when(migrationSupportService.getAllPublications(FROM, TO)).thenReturn(publications);

        ResponseEntity<List<PublicationWithRelatedData>> response =
            migrationSupportController.getAllPublications(FROM, TO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(publications);
    }
}
