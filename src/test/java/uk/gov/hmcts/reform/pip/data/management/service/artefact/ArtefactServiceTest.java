package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationCsv;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ExtendWith(MockitoExtension.class)
class ArtefactServiceTest {

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    AzureBlobService azureBlobService;

    @Mock
    AccountManagementService accountManagementService;

    @InjectMocks
    ArtefactService artefactService;

    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String PROVENANCE = "provenance";
    private static final String PROVENANCE_ID = "1234";
    private static final String PAYLOAD = "This is a payload";
    private static final String PAYLOAD_URL = "https://ThisIsATestPayload";
    private static final String LOCATION_ID = "123";
    private static final String TEST_KEY = "TestKey";
    private static final String TEST_VALUE = "TestValue";
    private static final Map<String, List<Object>> SEARCH_VALUES = new ConcurrentHashMap<>();
    private static final String VALIDATION_ARTEFACT_NOT_MATCH = "Artefacts do not match";
    private static final String VALIDATION_NOT_THROWN_MESSAGE = "Expected exception has not been thrown";
    private static final String TEST_FILE = "Hello";

    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final LocalDateTime START_OF_TODAY_CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay();

    private Artefact artefact;
    private Artefact artefactClassified;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithPayloadUrlClassified;
    private Artefact artefactWithIdAndPayloadUrl;

    private Location location;

    @BeforeAll
    public static void setupSearchValues() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
    }

    @BeforeEach
    void setup() {
        createPayloads();
        createClassifiedPayloads();

        initialiseCourts();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),artefact.getContentDate(),
                                                                    artefact.getLanguage().name(),
                                                                    artefact.getListType().name(),
                                                                    artefact.getProvenance()))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        lenient().when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID,
                                                                       LocationType.VENUE.name()))
            .thenReturn(Optional.of(location));
    }

    private void createPayloads() {
        artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();

        artefactWithPayloadUrl = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .locationId(LOCATION_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();

        artefactWithIdAndPayloadUrl = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .locationId(LOCATION_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();
    }

    private void createClassifiedPayloads() {
        artefactClassified = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .build();

        artefactWithPayloadUrlClassified = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now())
            .displayTo(null)
            .locationId(LOCATION_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .build();

        initialiseCourts();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),artefact.getContentDate(),
                                                                    artefact.getLanguage().name(),
                                                                    artefact.getListType().name(),
                                                                    artefact.getProvenance()))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        lenient().when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID,
                                                                       LocationType.VENUE.name()))
            .thenReturn(Optional.of(location));
    }

    private void initialiseCourts() {
        LocationCsv locationCsvFirstExample = new LocationCsv();
        locationCsvFirstExample.setLocationName("Court Name First Example");
        locationCsvFirstExample.setProvenanceLocationType("venue");
        location = new Location(locationCsvFirstExample);
        location.setLocationId(1234);

    }

    @Test
    void testArtefactPayloadFromAzureWhenAdmin() {
        when(artefactRepository.findArtefactByArtefactId(any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobData(any())).thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, artefactService.getPayloadByArtefactId(ARTEFACT_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactPayloadFromAzureWhenArtefactIsPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactPayloadFromAzureWhenArtefactIsNotPublicAndIsAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));
        when(azureBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        when(accountManagementService.getIsAuthorised(USER_ID,
                                                      ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(PAYLOAD, artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
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

        assertThrows(NotFoundException.class, () -> artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_NOT_THROWN_MESSAGE);
    }

    @Test
    void testArtefactFileFromAzureWhenAdmin() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findArtefactByArtefactId(any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobFile(any())).thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), artefactService.getFlatFileByArtefactID(
            ARTEFACT_ID), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactFileFromAzureWhenArtefactIsPublic() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobFile(any())).thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), artefactService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            USER_ID), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactFileFromAzureWhenArtefactIsNotPublic() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));
        when(azureBlobService.getBlobFile(any())).thenReturn(new ByteArrayResource(testData));

        when(accountManagementService.getIsAuthorised(USER_ID,
                                                      ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(new ByteArrayResource(testData), artefactService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            USER_ID), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactFileFromAzureWhenArtefactIsNotPublicAndNotAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> artefactService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            USER_ID), VALIDATION_NOT_THROWN_MESSAGE);
    }

    @Test
    void testArtefactPayloadFromAzureWhenUnauthorized() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactContentFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            ()
                -> artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testArtefactFileFromAzureWhenUnauthorized() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobFile(any()))
            .thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), artefactService.getFlatFileByArtefactID(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactFileFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            ()
                -> artefactService.getFlatFileByArtefactID(ARTEFACT_ID, USER_ID),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefact));

        assertEquals(artefact, artefactService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenNotPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(artefactClassified, artefactService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenNotPublicAndNotAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> artefactService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_NOT_THROWN_MESSAGE
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            () -> artefactService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testGetArtefactMetadataForAdmin() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));
        assertEquals(artefactWithIdAndPayloadUrl, artefactService.getMetadataByArtefactId(ARTEFACT_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testGetArtefactMetadataForAdminThrows() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString())).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                                                artefactService.getMetadataByArtefactId(ARTEFACT_ID),
                                            "Not found exception should be thrown"
        );
        assertEquals("No artefact found with the ID: " + ARTEFACT_ID, ex.getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testGetArtefactMetadataCallsNonAdmin() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));
        assertEquals(artefactWithIdAndPayloadUrl, artefactService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }

}
