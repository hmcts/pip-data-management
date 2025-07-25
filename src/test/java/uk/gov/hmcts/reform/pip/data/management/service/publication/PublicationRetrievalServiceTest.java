package uk.gov.hmcts.reform.pip.data.management.service.publication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_VENUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_FILE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.USER_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.VALIDATION_ARTEFACT_NOT_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.VALIDATION_NOT_THROWN_MESSAGE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationRetrievalServiceTest {
    private static final String PAYLOAD = "payload";

    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private AzureArtefactBlobService azureArtefactBlobService;

    @Mock
    private AccountManagementService accountManagementService;

    @InjectMocks
    private PublicationRetrievalService publicationRetrievalService;

    private Artefact artefact;
    private Artefact artefactClassified;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithPayloadUrlClassified;
    private Artefact artefactWithIdAndPayloadUrl;

    @BeforeEach
    void setup() {
        createPayloads();
        createClassifiedPayloads();

        Location location = ArtefactConstantTestHelper.initialiseCourts();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),
                                                                    artefact.getContentDate(),
                                                                    artefact.getLanguage(),
                                                                    artefact.getListType(),
                                                                    artefact.getProvenance()))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        lenient().when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID,
                                                                       LOCATION_VENUE))
            .thenReturn(Optional.of(location));
    }

    private void createPayloads() {
        artefact = ArtefactConstantTestHelper.buildArtefact();
        artefactWithPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithPayloadUrl();
        artefactWithIdAndPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithIdAndPayloadUrl();
    }

    private void createClassifiedPayloads() {
        artefactClassified = ArtefactConstantTestHelper.buildClassifiedPayloads();
        artefactWithPayloadUrlClassified = ArtefactConstantTestHelper.buildArtefactWithPayloadUrlClassified();

        Location location = ArtefactConstantTestHelper.initialiseCourts();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),
                                                                    artefact.getContentDate(),
                                                                    artefact.getLanguage(),
                                                                    artefact.getListType(),
                                                                    artefact.getProvenance()))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        lenient().when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID,
                                                                       LOCATION_VENUE))
            .thenReturn(Optional.of(location));
    }

    @Test
    void testArtefactPayloadFromAzureWhenAdmin() {
        when(artefactRepository.findArtefactByArtefactId(any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureArtefactBlobService.getBlobData(any())).thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, publicationRetrievalService.getPayloadByArtefactId(ARTEFACT_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactPayloadFromAzureWhenArtefactIsPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureArtefactBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, publicationRetrievalService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactPayloadFromAzureWhenArtefactIsNotPublicAndIsAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));
        when(azureArtefactBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        when(accountManagementService.getIsAuthorised(USER_ID,
                                                      ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(PAYLOAD, publicationRetrievalService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactPayloadFromAzureWhenArtefactIsNotPublicAndIsNotAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));

        when(accountManagementService.getIsAuthorised(USER_ID,
                                                      ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> publicationRetrievalService.getPayloadByArtefactId(ARTEFACT_ID,
                                                                                                       USER_ID),
                     VALIDATION_NOT_THROWN_MESSAGE);
    }

    @Test
    void testArtefactFileFromAzureWhenAdmin() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findArtefactByArtefactId(any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureArtefactBlobService.getBlobFile(any())).thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), publicationRetrievalService.getFlatFileByArtefactID(
            ARTEFACT_ID), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactFileFromAzureWhenArtefactIsPublic() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureArtefactBlobService.getBlobFile(any())).thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), publicationRetrievalService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            USER_ID), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactFileFromAzureWhenArtefactIsNotPublic() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));
        when(azureArtefactBlobService.getBlobFile(any())).thenReturn(new ByteArrayResource(testData));

        when(accountManagementService.getIsAuthorised(USER_ID,
                                                      ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(new ByteArrayResource(testData), publicationRetrievalService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            USER_ID), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactFileFromAzureWhenArtefactIsNotPublicAndNotAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> publicationRetrievalService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            USER_ID), VALIDATION_NOT_THROWN_MESSAGE);
    }

    @Test
    void testArtefactPayloadFromAzureWhenUnauthorized() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));
        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST,
                                                       Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> publicationRetrievalService.getPayloadByArtefactId(ARTEFACT_ID,
                                                                                                       USER_ID),
                     VALIDATION_NOT_THROWN_MESSAGE);
        verify(azureArtefactBlobService, never()).getBlobFile(any());
    }

    @Test
    void testArtefactContentFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            ()
                -> publicationRetrievalService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testArtefactFileFromAzureWhenUnauthorized() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));
        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST,
                                                      Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> publicationRetrievalService.getFlatFileByArtefactID(ARTEFACT_ID,
                                                                                                        USER_ID),
                     VALIDATION_NOT_THROWN_MESSAGE);
        verify(azureArtefactBlobService, never()).getBlobFile(any());
    }

    @Test
    void testArtefactFileFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            ()
                -> publicationRetrievalService.getFlatFileByArtefactID(ARTEFACT_ID, USER_ID),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefact));

        assertEquals(artefact, publicationRetrievalService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenNotPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(artefactClassified, publicationRetrievalService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenNotPublicAndNotAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> publicationRetrievalService.getMetadataByArtefactId(ARTEFACT_ID,
                                                                                                        USER_ID),
                     VALIDATION_NOT_THROWN_MESSAGE
        );
    }

    @Test
    void testGetArtefactMetadataForAdmin() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));
        assertEquals(artefactWithIdAndPayloadUrl, publicationRetrievalService.getMetadataByArtefactId(ARTEFACT_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testGetArtefactMetadataForAdminThrows() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString())).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                                                publicationRetrievalService.getMetadataByArtefactId(ARTEFACT_ID),
                                            "Not found exception should be thrown"
        );
        assertEquals("No artefact found with the ID: " + ARTEFACT_ID, ex.getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testGetArtefactMetadataCallsNonAdmin() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));
        assertEquals(artefactWithIdAndPayloadUrl, publicationRetrievalService.getMetadataByArtefactId(ARTEFACT_ID,
                                                                                                      USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }
}
