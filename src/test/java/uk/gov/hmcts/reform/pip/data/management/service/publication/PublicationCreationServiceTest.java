package uk.gov.hmcts.reform.pip.data.management.service.publication;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.FILE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_VENUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.MANUAL_UPLOAD_PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.NO_COURT_EXISTS_IN_REFERENCE_DATA;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD_URL;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.ROWID_RETURNS_UUID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_VALUES;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SOURCE_ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.START_OF_TODAY_CONTENT_DATE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_KEY;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_VALUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.VALIDATION_ARTEFACT_NOT_MATCH;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports"})
class PublicationCreationServiceTest {

    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private AzureArtefactBlobService azureArtefactBlobService;

    @Mock
    private PublicationFileManagementService publicationFileManagementService;

    @InjectMocks
    private PublicationCreationService publicationCreationService;

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;

    private static final Float PAYLOAD_SIZE_WITHIN_LIMIT = 90f;
    private static final Float PAYLOAD_SIZE_OVER_LIMIT = 110f;

    @BeforeAll
    public static void setupSearchValues() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
    }

    @BeforeEach
    void setup() {
        createPayloads();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),
                                                                    artefact.getContentDate(),
                                                                    artefact.getLanguage(),
                                                                    artefact.getListType(),
                                                                    artefact.getProvenance()))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
    }

    private void createPayloads() {
        artefact = ArtefactConstantTestHelper.buildArtefact();
        artefactWithPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithPayloadUrl();
        artefactWithIdAndPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithIdAndPayloadUrl();
    }

    @Test
    void testCreationOfNewArtefactWhenVenue() {
        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(any())).thenReturn(artefactWithIdAndPayloadUrl);

        Artefact returnedArtefact = publicationCreationService.createPublication(artefact, PAYLOAD);

        verify(azureArtefactBlobService, never()).deleteBlob(anyString());
        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testCreationOfNewArtefactWhenOwningHearingLocation() {
        artefact.setListType(ListType.SSCS_DAILY_LIST);
        artefactWithPayloadUrl.setLocationId("12341234");

        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(any())).thenReturn(artefactWithIdAndPayloadUrl);

        Artefact returnedArtefact = publicationCreationService.createPublication(artefact, PAYLOAD);

        verify(azureArtefactBlobService, never()).deleteBlob(anyString());
        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testUpdatingOfExistingArtefact() {
        Artefact existingArtefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .payloadSize(PAYLOAD_SIZE_WITHIN_LIMIT)
            .build();

        Artefact artefactToBeCreated = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .locationId(PROVENANCE_ID)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .payloadSize(PAYLOAD_SIZE_WITHIN_LIMIT)
            .build();

        when(artefactRepository.findArtefactByUpdateLogic(artefactToBeCreated.getLocationId(),
                                                          artefactToBeCreated.getContentDate(),
                                                          artefactToBeCreated.getLanguage(),
                                                          artefactToBeCreated.getListType(),
                                                          artefactToBeCreated.getProvenance()))
            .thenReturn(Optional.of(existingArtefact));
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(any())).thenReturn(artefactToBeCreated);

        Artefact returnedArtefact = publicationCreationService.createPublication(artefact, PAYLOAD);

        verify(azureArtefactBlobService).deleteBlob(anyString());
        assertEquals(artefactToBeCreated, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testUpdatingOfExistingArtefactWithNewPayloadNotWithinLimit() {
        Artefact existingArtefact = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .payloadSize(PAYLOAD_SIZE_OVER_LIMIT)
            .build();

        Artefact artefactToBeCreated = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .provenance(PROVENANCE)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .locationId(PROVENANCE_ID)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .payloadSize(PAYLOAD_SIZE_OVER_LIMIT)
            .build();

        when(artefactRepository.findArtefactByUpdateLogic(artefactToBeCreated.getLocationId(),
                                                          artefactToBeCreated.getContentDate(),
                                                          artefactToBeCreated.getLanguage(),
                                                          artefactToBeCreated.getListType(),
                                                          artefactToBeCreated.getProvenance()))
            .thenReturn(Optional.of(existingArtefact));
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(any())).thenReturn(artefactToBeCreated);

        Artefact returnedArtefact = publicationCreationService.createPublication(artefactToBeCreated, PAYLOAD);

        verify(azureArtefactBlobService).deleteBlob(anyString());
        verify(publicationFileManagementService).deleteFiles(artefactToBeCreated.getArtefactId(),
                                                         artefactToBeCreated.getListType(),
                                                         artefactToBeCreated.getLanguage());

        assertEquals(artefactToBeCreated, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testCreationOfNewArtefactWithFile() {
        artefactWithPayloadUrl.setSearch(null);
        artefactWithPayloadUrl.setLocationId(NO_COURT_EXISTS_IN_REFERENCE_DATA);
        when(azureArtefactBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenReturn(artefactWithIdAndPayloadUrl);

        Artefact returnedArtefact = publicationCreationService.createPublication(artefact, FILE);

        verify(azureArtefactBlobService, never()).deleteBlob(anyString());
        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testApplyInternalLocationIdForManualUpload() {
        Artefact artefact = new Artefact();
        artefact.setLocationId(PROVENANCE_ID);
        artefact.setProvenance(MANUAL_UPLOAD_PROVENANCE);

        publicationCreationService.applyInternalLocationId(artefact);

        verifyNoInteractions(locationRepository);
        assertThat(artefact.getLocationId()).isEqualTo(PROVENANCE_ID);

    }

    @Test
    void testApplyInternalLocationIdIfLocationExists() {
        Location location = new Location();
        location.setLocationId(Integer.valueOf(LOCATION_ID));
        when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID, LOCATION_VENUE))
            .thenReturn(Optional.of(location));

        publicationCreationService.applyInternalLocationId(artefact);
        assertThat(artefact.getLocationId()).isEqualTo(LOCATION_ID);
    }

    @Test
    void testApplyInternalLocationIdIfLocationDoesNotExist() {
        when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID, LOCATION_VENUE))
            .thenReturn(Optional.empty());

        publicationCreationService.applyInternalLocationId(artefact);
        assertThat(artefact.getLocationId()).isEqualTo("NoMatch" + PROVENANCE_ID);
    }

    @Test
    void testCreationOfNewArtefactWhenListTypeSjpPress() {
        publicationCreationService.applyInternalLocationId(artefact);
        assertThat(artefact.getLocationId()).isEqualTo(NO_COURT_EXISTS_IN_REFERENCE_DATA);
    }

    @Test
    void testSupersededCountIsUpdated() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),
                                                          artefact.getContentDate(),
                                                          artefact.getLanguage(),
                                                          artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.of(artefact));

        artefact.setPayload("/" + UUID.randomUUID());
        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);

        ArgumentCaptor<Artefact> captor = ArgumentCaptor.forClass(Artefact.class);
        when(artefactRepository.save(captor.capture())).thenReturn(artefactWithIdAndPayloadUrl);

        publicationCreationService.createPublication(artefact, PAYLOAD);

        assertEquals(1, captor.getValue().getSupersededCount(), "Superseded count has not been incremented");
    }

    @Test
    void testSupersededCountIsNotUpdated() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),
                                                          artefact.getContentDate(),
                                                          artefact.getLanguage(),
                                                          artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());

        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);

        ArgumentCaptor<Artefact> captor = ArgumentCaptor.forClass(Artefact.class);
        when(artefactRepository.save(captor.capture())).thenReturn(artefactWithIdAndPayloadUrl);

        publicationCreationService.createPublication(artefact, PAYLOAD);

        assertEquals(0, captor.getValue().getSupersededCount(), "Superseded count has been incremented");
    }
}
