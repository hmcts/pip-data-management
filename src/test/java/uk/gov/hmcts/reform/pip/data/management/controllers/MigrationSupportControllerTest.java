package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.InvalidDateRangeException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.PublicationWithRelatedData;
import uk.gov.hmcts.reform.pip.data.management.service.MigrationSupportFileService;
import uk.gov.hmcts.reform.pip.data.management.service.MigrationSupportService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrationSupportControllerTest {
    private static final LocalDate FROM = LocalDate.of(2024, 1, 10);
    private static final LocalDate TO = LocalDate.of(2024, 1, 20);

    @Mock
    private MigrationSupportService migrationSupportService;

    @Mock
    private MigrationSupportFileService migrationSupportFileService;

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

    @Test
    void testGetAllPublicationFilesWritesZip() throws IOException {
        List<Artefact> artefacts = List.of(new Artefact());
        when(migrationSupportService.getAllArtefacts(FROM, TO)).thenReturn(artefacts);
        MockHttpServletResponse response = new MockHttpServletResponse();

        migrationSupportController.getAllPublicationFiles(FROM, TO, response);

        assertThat(response.getContentType()).isEqualTo("application/zip");
        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION))
            .isEqualTo("attachment; filename=\"publication-files.zip\"");
        verify(migrationSupportFileService).writePublicationFiles(artefacts, response.getOutputStream());
    }

    @Test
    void testGetAllPublicationFilesInvalidDateRangeDoesNotWriteZip() {
        when(migrationSupportService.getAllArtefacts(TO, FROM))
            .thenThrow(new InvalidDateRangeException("Invalid"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> migrationSupportController.getAllPublicationFiles(TO, FROM, response))
            .isInstanceOf(InvalidDateRangeException.class);
        assertThat(response.getContentType()).isNull();
        verifyNoInteractions(migrationSupportFileService);
    }
}
