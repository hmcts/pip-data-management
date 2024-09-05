package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.publication;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.controllers.tests.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationService;

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
import static uk.gov.hmcts.reform.pip.data.management.controllers.tests.helpers.ArtefactConstantTestHelper.FILE;
import static uk.gov.hmcts.reform.pip.data.management.controllers.tests.helpers.ArtefactConstantTestHelper.PAYLOAD;
import static uk.gov.hmcts.reform.pip.data.management.controllers.tests.helpers.ArtefactConstantTestHelper.PAYLOAD_STRIPPED;
import static uk.gov.hmcts.reform.pip.data.management.controllers.tests.helpers.ArtefactConstantTestHelper.PAYLOAD_URL;

@ActiveProfiles(profiles = "test")
@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@EnableRetry
class CreatePublicationRetryTest {
    private static final int RETRY_MAX_ATTEMPTS = 5;

    @MockBean
    AzureArtefactBlobService azureArtefactBlobService;

    @MockBean
    ArtefactRepository artefactRepository;

    @Autowired
    private PublicationService publicationService;

    @Test
    void testCreateJsonPublicationMaxAttemptsWithCannotAcquireLockException() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
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
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
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
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
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
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenThrow(new JpaSystemException(new RuntimeException()));

        assertThatThrownBy(() -> publicationService.createPublication(artefact, PAYLOAD))
            .isInstanceOf(JpaSystemException.class);

        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(artefactRepository).save(any());
    }

    @Test
    void testCreateJsonPublicationDoesNotDeleteBlobOnErrorIfNoPayloadUrl() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> publicationService.createPublication(artefact, PAYLOAD))
            .isInstanceOf(RuntimeException.class);

        verify(azureArtefactBlobService, never()).deleteBlob(anyString());
        verify(artefactRepository, never()).save(any());
    }

    @Test
    void testCreateFlatFilePublicationMaxAttemptsWithCannotAcquireLockException() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
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
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
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
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
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
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        when(azureArtefactBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenThrow(new JpaSystemException(new RuntimeException()));

        assertThatThrownBy(() -> publicationService.createPublication(artefact, FILE))
            .isInstanceOf(JpaSystemException.class);

        verify(azureArtefactBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(artefactRepository).save(any());
    }

    @Test
    void testCreateFlatFilePublicationDoesNotDeleteBlobOnErrorIfNoPayloadUrl() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        when(azureArtefactBlobService.uploadFlatFile(any(), eq(FILE))).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> publicationService.createPublication(artefact, FILE))
            .isInstanceOf(RuntimeException.class);

        verify(azureArtefactBlobService, never()).deleteBlob(anyString());
        verify(artefactRepository, never()).save(any());
    }
}
