package uk.gov.hmcts.reform.pip.data.management.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.FILE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD_URL;

@ActiveProfiles(profiles = "test")
@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@EnableRetry
class CreatePublicationRetryTest {
    @MockBean
    AzureBlobService azureBlobService;

    @MockBean
    ArtefactRepository artefactRepository;

    @Autowired
    private PublicationService publicationService;

    @Test
    void testCreateJsonPublicationMaxAttempts() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage(), artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());
        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenThrow(CannotAcquireLockException.class);

        assertThatThrownBy(() -> publicationService.createPublication(artefact, PAYLOAD))
            .isInstanceOf(CannotAcquireLockException.class);
        verify(artefactRepository, times(10)).save(any());
    }

    @Test
    void testCreateJsonPublicationStopRetryIfSuccess() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage(), artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());
        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact))
            .thenThrow(CannotAcquireLockException.class)
            .thenThrow(CannotAcquireLockException.class)
            .thenReturn(artefact);

        publicationService.createPublication(artefact, PAYLOAD);
        verify(artefactRepository, times(3)).save(any());
    }

    @Test
    void testCreateFlatFilePublicationMaxAttempts() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage(), artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());
        when(azureBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenThrow(CannotAcquireLockException.class);

        assertThatThrownBy(() -> publicationService.createPublication(artefact, FILE))
            .isInstanceOf(CannotAcquireLockException.class);
        verify(artefactRepository, times(10)).save(any());
    }

    @Test
    void testCreateFlatFilePublicationStopRetryIfSuccess() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage(), artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());
        when(azureBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact))
            .thenThrow(CannotAcquireLockException.class)
            .thenThrow(CannotAcquireLockException.class)
            .thenReturn(artefact);

        publicationService.createPublication(artefact, FILE);
        verify(artefactRepository, times(3)).save(any());
    }
}
