package uk.gov.hmcts.reform.pip.data.management.service;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationCsv;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.TestConstants.MESSAGES_MATCH;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports",
    "PMD.TooManyMethods", "PMD.TooManyFields", "PMD.ExcessiveClassLength", "PMD.LawOfDemeter"})
class PublicationServiceTest {

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    AzureBlobService azureBlobService;

    @Mock
    PayloadExtractor payloadExtractor;

    @Mock
    SubscriptionManagementService subscriptionManagementService;

    @Mock
    AccountManagementService accountManagementService;

    @Mock
    PublicationServicesService publicationServicesService;

    @InjectMocks
    PublicationService publicationService;

    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String PROVENANCE = "provenance";
    private static final String PROVENANCE_ID = "1234";
    private static final String MANUAL_UPLOAD_PROVENANCE = "MANUAL_UPLOAD";
    private static final String PAYLOAD = "This is a payload";
    private static final String PAYLOAD_URL = "https://ThisIsATestPayload";
    private static final String PAYLOAD_STRIPPED = "ThisIsATestPayload";
    private static final String LOCATION_ID = "123";
    private static final String TEST_KEY = "TestKey";
    private static final String TEST_VALUE = "TestValue";
    private static final CaseSearchTerm SEARCH_TERM_CASE_ID = CaseSearchTerm.CASE_ID;
    private static final CaseSearchTerm SEARCH_TERM_CASE_NAME = CaseSearchTerm.CASE_NAME;
    private static final CaseSearchTerm SEARCH_TERM_CASE_URN = CaseSearchTerm.CASE_URN;
    private static final Map<String, List<Object>> SEARCH_VALUES = new ConcurrentHashMap<>();
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);
    private static final String VALIDATION_ARTEFACT_NOT_MATCH = "Artefacts do not match";
    private static final String VALIDATION_NOT_THROWN_MESSAGE = "Expected exception has not been thrown";
    private static final String SUCCESSFUL_TRIGGER = "success - subscription sent";
    private static final String SUCCESS = "Success";
    private static final String DELETION_TRACK_LOG_MESSAGE = "Track: TestValue, Removed %s, at ";
    private static final String ROWID_RETURNS_UUID = "Row ID must match returned UUID";
    private static final String TEST_FILE = "Hello";
    private static final String LOCATION_TYPE_MATCH = "Location types should match";

    private static final String NO_COURT_EXISTS_IN_REFERENCE_DATA = "NoMatch1234";
    private static final String VALIDATION_MORE_THAN_PUBLIC = "More than the public artefact has been found";

    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final LocalDateTime START_OF_TODAY_CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay();

    private Artefact artefact;
    private Artefact artefactClassified;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithPayloadUrlClassified;
    private Artefact artefactWithIdAndPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrlClassified;
    private Artefact artefactInTheFuture;
    private Artefact artefactFromThePast;
    private Artefact artefactFromNow;
    private Artefact artefactWithNullDateTo;
    private Artefact artefactWithSameDateFromAndTo;
    private Artefact artefactManualUpload;
    private Artefact noMatchArtefact;

    private Location location;

    @BeforeAll
    public static void setupSearchValues() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
    }

    @BeforeEach
    void setup() {
        createPayloads();
        createClassifiedPayloads();

        intialiseManualUploadArtefact();
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

        artefactFromThePast = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now().minusDays(1))
            .displayTo(LocalDateTime.now().plusDays(1))
            .build();

        artefactFromNow = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now())
            .displayTo(LocalDateTime.now().plusHours(3))
            .build();

        artefactWithNullDateTo = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now())
            .displayTo(null)
            .build();

        artefactWithSameDateFromAndTo = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now())
            .displayTo(LocalDateTime.now())
            .build();

        artefactInTheFuture = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now().plusDays(1))
            .displayTo(LocalDateTime.now().plusDays(2))
            .build();

        noMatchArtefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .locationId(NO_COURT_EXISTS_IN_REFERENCE_DATA)
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

        artefactWithIdAndPayloadUrlClassified = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now())
            .displayTo(LocalDateTime.now())
            .locationId(LOCATION_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .build();

        intialiseManualUploadArtefact();
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

    private void intialiseManualUploadArtefact() {
        artefactManualUpload = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(MANUAL_UPLOAD_PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();
    }

    @Test
    void testCreationOfNewArtefact() {
        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testCreationOfNewArtefactWhenCourtDoesNotExists() {
        artefactWithPayloadUrl.setLocationId(NO_COURT_EXISTS_IN_REFERENCE_DATA);
        when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID, LocationType.VENUE.name()))
            .thenReturn(Optional.empty());
        when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testCreationOfNewArtefactWithManualUpload() {
        artefactWithPayloadUrl.setProvenance(MANUAL_UPLOAD_PROVENANCE);
        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        artefactWithPayloadUrl.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        artefactWithPayloadUrl.setLanguage(Language.ENGLISH);
        artefactWithPayloadUrl.setContentDate(START_OF_TODAY_CONTENT_DATE);

        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(artefactManualUpload, PAYLOAD);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testUpdatingOfExistingArtefact() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .build();

        Artefact newArtefactWithId = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .locationId(PROVENANCE_ID)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .build();

        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(newArtefactWithId)).thenReturn(newArtefactWithId);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(newArtefactWithId, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testUpdatingOfExistingArtefactForManualUpload() {

        Artefact artefact = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(MANUAL_UPLOAD_PROVENANCE)
            .locationId(PROVENANCE_ID)
            .language(Language.ENGLISH)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        Artefact newArtefactWithId = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(MANUAL_UPLOAD_PROVENANCE)
            .locationId(PROVENANCE_ID)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(newArtefactWithId)).thenReturn(newArtefactWithId);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(newArtefactWithId, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testUpdatingOfExistingArtefactWhenCourtDoesNotExists() {

        Artefact existingArtefact = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .locationId(NO_COURT_EXISTS_IN_REFERENCE_DATA)
            .sensitivity(Sensitivity.PUBLIC)
            .build();

        Artefact newArtefactWithId = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(NO_COURT_EXISTS_IN_REFERENCE_DATA)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();

        when(artefactRepository.findArtefactByUpdateLogic(NO_COURT_EXISTS_IN_REFERENCE_DATA, artefact.getContentDate(),
                                                          artefact.getLanguage().name(),
                                                          artefact.getListType().name(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.of(existingArtefact));

        when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID, LocationType.VENUE.name()))
            .thenReturn(Optional.empty());
        when(artefactRepository.save(newArtefactWithId)).thenReturn(newArtefactWithId);
        when(azureBlobService.createPayload(PAYLOAD_STRIPPED, PAYLOAD)).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(existingArtefact)).thenReturn(existingArtefact);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(existingArtefact, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testCreationOfNewArtefactWithFile() {
        artefactWithPayloadUrl.setSearch(null);
        artefactWithPayloadUrl.setLocationId(NO_COURT_EXISTS_IN_REFERENCE_DATA);
        when(azureBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenReturn(artefactWithIdAndPayloadUrl);
        Artefact returnedArtefact = publicationService.createPublication(artefact, FILE);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testCreationOfNewArtefactWithFileByManualUpload() {
        artefactWithPayloadUrl.setSearch(null);
        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        artefactWithPayloadUrl.setProvenance(MANUAL_UPLOAD_PROVENANCE);
        when(azureBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);

        Artefact returnedArtefact = publicationService.createPublication(artefactManualUpload, FILE);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testCreationOfNewArtefactWithFileWhenCourtDoesNotExists() {
        artefactWithPayloadUrl.setSearch(null);
        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID, LocationType.VENUE.name()))
            .thenReturn(Optional.of(location));
        Artefact returnedArtefact = publicationService.createPublication(artefact, FILE);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactPayloadFromAzureWhenAdmin() {
        when(artefactRepository.findArtefactByArtefactId(any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobData(any())).thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, publicationService.getPayloadByArtefactId(ARTEFACT_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactPayloadFromAzureWhenArtefactIsPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, publicationService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
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

        assertEquals(PAYLOAD, publicationService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
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

        assertThrows(NotFoundException.class, () -> publicationService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_NOT_THROWN_MESSAGE);
    }

    @Test
    void testArtefactFileFromAzureWhenAdmin() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findArtefactByArtefactId(any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobFile(any())).thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), publicationService.getFlatFileByArtefactID(
            ARTEFACT_ID), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactFileFromAzureWhenArtefactIsPublic() {
        byte[] testData = TEST_FILE.getBytes();
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobFile(any())).thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), publicationService.getFlatFileByArtefactID(
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

        assertEquals(new ByteArrayResource(testData), publicationService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            USER_ID), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactFileFromAzureWhenArtefactIsNotPublicAndNotAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrlClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> publicationService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            USER_ID), VALIDATION_NOT_THROWN_MESSAGE);
    }

    @Test
    void testArtefactPayloadFromAzureWhenUnauthorized() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, publicationService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactContentFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            ()
                -> publicationService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
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

        assertEquals(new ByteArrayResource(testData), publicationService.getFlatFileByArtefactID(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactFileFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            ()
                -> publicationService.getFlatFileByArtefactID(ARTEFACT_ID, USER_ID),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefact));

        assertEquals(artefact, publicationService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenNotPublic() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(artefactClassified, publicationService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenNotPublicAndNotAuthorised() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.of(artefactClassified));

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        assertThrows(NotFoundException.class, () -> publicationService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_NOT_THROWN_MESSAGE
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactId(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            () -> publicationService.getPayloadByArtefactId(ARTEFACT_ID, USER_ID),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testGetArtefactMetadataForAdmin() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));
        assertEquals(artefactWithIdAndPayloadUrl, publicationService.getMetadataByArtefactId(ARTEFACT_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testGetArtefactMetadataForAdminThrows() {
        when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString())).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                                                publicationService.getMetadataByArtefactId(ARTEFACT_ID),
                                            "Not found exception should be thrown"
        );
        assertEquals("No artefact found with the ID: " + ARTEFACT_ID, ex.getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testGetArtefactMetadataCallsNonAdmin() {
        when(artefactRepository.findByArtefactId(any(), any()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));
        assertEquals(artefactWithIdAndPayloadUrl, publicationService.getMetadataByArtefactId(ARTEFACT_ID, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindByCourtIdWhenVerifiedAndAuthorised() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        Artefact artefact2 = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.WELSH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        List<Artefact> artefactList = new ArrayList<>();
        artefactList.add(artefact);
        artefactList.add(artefact2);

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        when(artefactRepository.findArtefactsByLocationId(any(), any()))
            .thenReturn(artefactList);

        assertEquals(artefactList, publicationService.findAllByLocationId("abc", USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindByCourtIdWhenVerifiedAndNotAuthorised() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        Artefact artefact2 = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.WELSH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        List<Artefact> artefactList = new ArrayList<>();
        artefactList.add(artefact);
        artefactList.add(artefact2);

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        when(artefactRepository.findArtefactsByLocationId(any(), any()))
            .thenReturn(artefactList);

        List<Artefact> artefacts = publicationService.findAllByLocationId("abc", USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefact, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindByCourtIdWhenUnverified() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .build();

        Artefact artefact2 = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.WELSH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();

        List<Artefact> artefactList = new ArrayList<>();
        artefactList.add(artefact);
        artefactList.add(artefact2);

        when(artefactRepository.findArtefactsByLocationId(any(), any()))
            .thenReturn(artefactList);

        List<Artefact> artefacts = publicationService.findAllByLocationId("abc", USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefact2, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllBySearchCaseIdClassifiedAndAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_ID.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(
            list,
            publicationService.findAllBySearch(SEARCH_TERM_CASE_ID, TEST_VALUE, USER_ID),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllBySearchCaseIdClassifiedAndNotAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_ID.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        List<Artefact> artefacts = publicationService.findAllBySearch(SEARCH_TERM_CASE_ID, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllBySearchCaseIdUnverified() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_ID.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        List<Artefact> artefacts = publicationService.findAllBySearch(SEARCH_TERM_CASE_ID, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllNoArtefactsThrowsNotFound() {
        ArtefactNotFoundException ex = assertThrows(ArtefactNotFoundException.class, () ->
            publicationService.findAllBySearch(SEARCH_TERM_CASE_ID, "not found", USER_ID)
        );
        assertEquals("No Artefacts found with for CASE_ID with the value: not found",
                     ex.getMessage(), MESSAGES_MATCH
        );
    }

    @Test
    void testFindAllByCaseNameClassifiedAndAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactByCaseName(eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(
            list,
            publicationService.findAllBySearch(SEARCH_TERM_CASE_NAME, TEST_VALUE, USER_ID),
            VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCaseNameClassifiedAndNotAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactByCaseName(eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        List<Artefact> artefacts = publicationService.findAllBySearch(SEARCH_TERM_CASE_NAME, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCaseNameUnverified() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactByCaseName(eq(TEST_VALUE), any()))
            .thenReturn(list);

        List<Artefact> artefacts = publicationService.findAllBySearch(SEARCH_TERM_CASE_NAME, TEST_VALUE, null);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCaseUrnClassifiedAndAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_URN.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(true);

        assertEquals(
            list,
            publicationService.findAllBySearch(SEARCH_TERM_CASE_URN, TEST_VALUE, USER_ID),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllByCaseUrnClassifiedAndNotAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_URN.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(accountManagementService.getIsAuthorised(USER_ID, ListType.CIVIL_DAILY_CAUSE_LIST, Sensitivity.CLASSIFIED))
            .thenReturn(false);

        List<Artefact> artefacts = publicationService.findAllBySearch(SEARCH_TERM_CASE_URN, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCaseUrnUnverified() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_URN.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        List<Artefact> artefacts = publicationService.findAllBySearch(SEARCH_TERM_CASE_URN, TEST_VALUE, null);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testInvalidEnumTypeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            publicationService.findAllBySearch(CaseSearchTerm.valueOf("invalid"), TEST_VALUE, USER_ID));
    }

    @Test
    void testTriggerIfDateIsFuture() throws IOException {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationService.class)) {
            publicationService.checkAndTriggerSubscriptionManagement(artefactInTheFuture);
            assertEquals(
                0,
                logCaptor.getInfoLogs().size(),
                "Should not have returned a log as no trigger was sent."
            );
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Test
    void testTriggerIfDateIsNow() {
        when(subscriptionManagementService.sendArtefactForSubscription(artefactFromNow)).thenReturn(SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationService.class)) {
            publicationService.checkAndTriggerSubscriptionManagement(artefactFromNow);
            assertEquals(SUCCESSFUL_TRIGGER, logCaptor.getInfoLogs().get(0),
                         "should have returned the Subscription List."
            );
        }
    }

    @Test
    void testTriggerIfDateIsPast() {
        when(subscriptionManagementService.sendArtefactForSubscription(artefactFromThePast)).thenReturn(
            SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationService.class)) {
            publicationService.checkAndTriggerSubscriptionManagement(artefactFromThePast);
            assertEquals(SUCCESSFUL_TRIGGER, logCaptor.getInfoLogs().get(0),
                         "Should have returned the subscription list"
            );
        }
    }

    @Test
    void testTriggerIfDateToNull() {
        when(subscriptionManagementService.sendArtefactForSubscription(artefactWithNullDateTo)).thenReturn(
            SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationService.class)) {
            publicationService.checkAndTriggerSubscriptionManagement(artefactWithNullDateTo);
            assertEquals(SUCCESSFUL_TRIGGER, logCaptor.getInfoLogs().get(0),
                         "Should have returned the subscription list"
            );
        }
    }

    @Test
    void testTriggerIfSameDateFromTo() {
        when(subscriptionManagementService.sendArtefactForSubscription(artefactWithSameDateFromAndTo)).thenReturn(
            SUCCESSFUL_TRIGGER);
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationService.class)) {
            publicationService.checkAndTriggerSubscriptionManagement(artefactWithSameDateFromAndTo);
            assertEquals(SUCCESSFUL_TRIGGER, logCaptor.getInfoLogs().get(0),
                         "Should have returned the subscription list"
            );
        }
    }

    @Test
    void testFindAllByCourtIdAdmin() {
        when(artefactRepository.findArtefactsByLocationIdAdmin(TEST_VALUE)).thenReturn(List.of(artefact));
        assertEquals(List.of(artefact), publicationService.findAllByLocationIdAdmin(TEST_VALUE, USER_ID, true),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCourtIdAdminNotAdmin() {
        when(artefactRepository.findArtefactsByLocationId(any(), any())).thenReturn(List.of(artefact));
        assertEquals(List.of(artefact), publicationService.findAllByLocationIdAdmin(TEST_VALUE, USER_ID, false),
                     VALIDATION_ARTEFACT_NOT_MATCH);

    }

    @Test
    void testSendArtefactForSubscription() {
        when(subscriptionManagementService.sendArtefactForSubscription(artefact))
            .thenReturn(SUCCESS);
        assertEquals(SUCCESS, publicationService.sendArtefactForSubscription(artefact),
                     MESSAGES_MATCH);
    }

    @Test
    void testCheckNewlyActiveArtefactsLogs() throws IOException {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationService.class)) {
            when(artefactRepository.findArtefactsByDisplayFrom(any())).thenReturn(List.of(new Artefact()));
            when(subscriptionManagementService.sendArtefactForSubscription(any())).thenReturn(SUCCESS);
            publicationService.checkNewlyActiveArtefacts();
            assertEquals(SUCCESS, logCaptor.getInfoLogs().get(0),
                         MESSAGES_MATCH
            );
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Test
    void testDeleteArtefactById() throws IOException {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationService.class)) {
            when(subscriptionManagementService.sendDeletedArtefactForThirdParties(artefactWithPayloadUrl))
                .thenReturn(SUCCESS);
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithPayloadUrl));
            when(azureBlobService.deleteBlob(PAYLOAD_STRIPPED)).thenReturn(SUCCESS);
            doNothing().when(artefactRepository).delete(artefactWithPayloadUrl);

            publicationService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertTrue(logCaptor.getInfoLogs().get(0).contains(SUCCESS), MESSAGES_MATCH);
            assertTrue(logCaptor.getInfoLogs().get(1).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);
            assertTrue(logCaptor.getInfoLogs().get(2).contains(SUCCESS), MESSAGES_MATCH);
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Test
    void testDeleteArtefactByIdThrows() {
        ArtefactNotFoundException ex = assertThrows(ArtefactNotFoundException.class, () ->
                                                        publicationService.deleteArtefactById(TEST_VALUE, TEST_VALUE),
                                                    "ArtefactNotFoundException should be thrown");

        assertEquals("No artefact found with the ID: " + TEST_VALUE, ex.getMessage(),
                     MESSAGES_MATCH);
    }

    @Test
    void testRunDailyTasks() throws IOException {
        when(artefactRepository.findOutdatedArtefacts(LocalDate.now())).thenReturn(List.of(artefactWithPayloadUrl));
        when(artefactRepository.findAllNoMatchArtefacts()).thenReturn(List.of(noMatchArtefact));
        when(azureBlobService.deleteBlob(any())).thenReturn("Success");
        Map<String, String> testMap = new ConcurrentHashMap<>();
        testMap.put("1234", "provenance");
        when(publicationServicesService.sendNoMatchArtefactsForReporting(testMap))
            .thenReturn("Success no match artefacts sent");
        lenient().doNothing().when(artefactRepository).deleteAll(List.of(artefact));
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationService.class)) {
            publicationService.runDailyTasks();
            assertEquals("Success no match artefacts sent", logCaptor.getInfoLogs().get(0), MESSAGES_MATCH);
            assertEquals(SUCCESS, logCaptor.getInfoLogs().get(1), MESSAGES_MATCH);
            assertEquals("1 outdated artefacts found and deleted for before " + LocalDate.now(),
                         logCaptor.getInfoLogs().get(2), MESSAGES_MATCH);
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Test
    void testRunDailyTasksWithNoBlobsFound() throws IOException {
        when(artefactRepository.findOutdatedArtefacts(LocalDate.now())).thenReturn(List.of());
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationService.class)) {
            publicationService.runDailyTasks();
            assertEquals("0 outdated artefacts found and deleted for before " + LocalDate.now(),
                         logCaptor.getInfoLogs().get(0), MESSAGES_MATCH);
            verify(publicationServicesService, times(0))
                .sendNoMatchArtefactsForReporting(any());
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Test
    void testGetLocationTypeVenue() {
        List<ListType> venueListTypes = new ArrayList<>();
        venueListTypes.add(ListType.CROWN_DAILY_LIST);
        venueListTypes.add(ListType.CROWN_FIRM_LIST);
        venueListTypes.add(ListType.CROWN_WARNED_LIST);
        venueListTypes.add(ListType.MAGS_PUBLIC_LIST);
        venueListTypes.add(ListType.MAGS_STANDARD_LIST);
        venueListTypes.add(ListType.CIVIL_DAILY_CAUSE_LIST);
        venueListTypes.add(ListType.FAMILY_DAILY_CAUSE_LIST);
        venueListTypes.add(ListType.IAC_DAILY_LIST);

        venueListTypes.forEach(listType ->
                                   assertEquals(LocationType.VENUE,
                                                publicationService.getLocationType(listType),
                                                LOCATION_TYPE_MATCH));
    }

    @Test
    void testGetLocationTypeNational() {
        List<ListType> nationalListTypes = new ArrayList<>();
        nationalListTypes.add(ListType.SJP_PRESS_LIST);
        nationalListTypes.add(ListType.SJP_PUBLIC_LIST);

        nationalListTypes.forEach(listType ->
                                      assertEquals(LocationType.NATIONAL,
                                                   publicationService.getLocationType(listType),
                                                   LOCATION_TYPE_MATCH));

    }

    @Test
    void testMaskEmail() {
        assertEquals("t*******@email.com",
                     publicationService.maskEmail("testUser@email.com"),
                     "Email was not masked correctly");
    }

    @Test
    void testMaskEmailNotValidEmail() {
        assertEquals("a****",
                     publicationService.maskEmail("abcde"),
                     "Email was not masked correctly");
    }

    @Test
    void testMaskEmailEmptyString() {
        assertEquals("",
                     publicationService.maskEmail(""),
                     "Email was not masked correctly");
    }

}
