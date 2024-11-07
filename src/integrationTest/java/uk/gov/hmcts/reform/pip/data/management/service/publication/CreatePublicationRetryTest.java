package uk.gov.hmcts.reform.pip.data.management.service.publication;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration")
@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@EnableRetry
class CreatePublicationRetryTest {
    private static final int RETRY_MAX_ATTEMPTS = 5;
    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String PROVENANCE = "provenance";
    private static final String PROVENANCE_ID = "1234";
    private static final LocalDateTime START_OF_TODAY_CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay();
    private static final String PAYLOAD = "This is a payload";
    private static final String PAYLOAD_URL = "https://ThisIsATestPayload";
    private static final String PAYLOAD_STRIPPED = "ThisIsATestPayload";
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);

    private final Artefact artefact = buildArtefact();

    @MockBean
    AzureArtefactBlobService azureArtefactBlobService;

    @MockBean
    ArtefactRepository artefactRepository;

    @Autowired
    private PublicationService publicationService;

    @Test
    void testCreateJsonPublicationMaxAttemptsWithCannotAcquireLockException() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage(), artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenThrow(CannotAcquireLockException.class);

        assertThatThrownBy(() -> publicationService.createPublication(artefact, PAYLOAD))
            .isInstanceOf(CannotAcquireLockException.class);
        verify(artefactRepository, times(RETRY_MAX_ATTEMPTS)).save(any());
    }

    @Test
    void testCreateJsonPublicationMaxAttemptsWithDataIntegrityViolationException() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage(), artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> publicationService.createPublication(artefact, PAYLOAD))
            .isInstanceOf(DataIntegrityViolationException.class);
        verify(artefactRepository, times(RETRY_MAX_ATTEMPTS)).save(any());
    }

    @Test
    void testCreateJsonPublicationStopRetryIfSuccess() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage(), artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact))
            .thenThrow(CannotAcquireLockException.class)
            .thenThrow(CannotAcquireLockException.class)
            .thenReturn(artefact);

        assertThatCode(() -> publicationService.createPublication(artefact, PAYLOAD)).doesNotThrowAnyException();
        verify(artefactRepository, times(3)).save(any());
    }

    @Test
    void testCreateJsonPublicationDeleteBlobOnErrorIfPayloadUrlExists() {
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenThrow(new JpaSystemException(new RuntimeException()));

        assertThatThrownBy(() -> publicationService.createPublication(artefact, PAYLOAD))
            .isInstanceOf(JpaSystemException.class);

        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(artefactRepository).save(any());
    }

    @Test
    void testCreateJsonPublicationDoesNotDeleteBlobOnErrorIfNoPayloadUrl() {
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> publicationService.createPublication(artefact, PAYLOAD))
            .isInstanceOf(RuntimeException.class);

        verify(azureArtefactBlobService, never()).deleteBlob(anyString());
        verify(artefactRepository, never()).save(any());
    }

    @Test
    void testCreateFlatFilePublicationMaxAttemptsWithCannotAcquireLockException() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage(), artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());
        when(azureArtefactBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenThrow(CannotAcquireLockException.class);

        assertThatThrownBy(() -> publicationService.createPublication(artefact, FILE))
            .isInstanceOf(CannotAcquireLockException.class);
        verify(artefactRepository, times(RETRY_MAX_ATTEMPTS)).save(any());
    }

    @Test
    void testCreateFlatFilePublicationMaxAttemptsWithDataIntegrityViolationException() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage(), artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());
        when(azureArtefactBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> publicationService.createPublication(artefact, FILE))
            .isInstanceOf(DataIntegrityViolationException.class);
        verify(artefactRepository, times(RETRY_MAX_ATTEMPTS)).save(any());
    }

    @Test
    void testCreateFlatFilePublicationStopRetryIfSuccess() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage(), artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());
        when(azureArtefactBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact))
            .thenThrow(DataIntegrityViolationException.class)
            .thenThrow(DataIntegrityViolationException.class)
            .thenReturn(artefact);

        assertThatCode(() -> publicationService.createPublication(artefact, PAYLOAD)).doesNotThrowAnyException();
        verify(artefactRepository, times(3)).save(any());
    }

    @Test
    void testCreateFlatFilePublicationDeleteBlobOnErrorIfPayloadUrlExists() {
        when(azureArtefactBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenThrow(new JpaSystemException(new RuntimeException()));

        assertThatThrownBy(() -> publicationService.createPublication(artefact, FILE))
            .isInstanceOf(JpaSystemException.class);

        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(artefactRepository).save(any());
    }

    @Test
    void testCreateFlatFilePublicationDoesNotDeleteBlobOnErrorIfNoPayloadUrl() {
        when(azureArtefactBlobService.uploadFlatFile(any(), eq(FILE))).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> publicationService.createPublication(artefact, FILE))
            .isInstanceOf(RuntimeException.class);

        verify(azureArtefactBlobService, never()).deleteBlob(anyString());
        verify(artefactRepository, never()).save(any());
    }

    private Artefact buildArtefact() {
        return Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();
    }
}
