package uk.gov.hmcts.reform.pip.data.management.service;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.pip.data.management.service.artefact.ArtefactSearchService;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.TooManyFields", "PMD.ExcessiveClassLength",
    "PMD.LawOfDemeter", "PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
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
    PublicationServicesService publicationServicesService;

    @Mock
    ChannelManagementService channelManagementService;

    @InjectMocks
    PublicationService publicationService;

    @InjectMocks
    ArtefactSearchService artefactSearchService;

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
    private static final Map<String, List<Object>> SEARCH_VALUES = new ConcurrentHashMap<>();
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);
    private static final String VALIDATION_ARTEFACT_NOT_MATCH = "Artefacts do not match";
    private static final String SUCCESSFUL_TRIGGER = "success - subscription sent";
    private static final String SUCCESS = "Success";
    private static final String DELETION_TRACK_LOG_MESSAGE = "Track: TestValue, Removed %s, at ";
    private static final String ROWID_RETURNS_UUID = "Row ID must match returned UUID";
    private static final String LOCATION_TYPE_MATCH = "Location types should match";

    private static final String NO_COURT_EXISTS_IN_REFERENCE_DATA = "NoMatch1234";

    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final LocalDateTime START_OF_TODAY_CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay();

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;
    private Artefact artefactInTheFuture;
    private Artefact artefactFromThePast;
    private Artefact artefactFromNow;
    private Artefact artefactWithNullDateTo;
    private Artefact artefactWithSameDateFromAndTo;
    private Artefact artefactManualUpload;
    private Artefact noMatchArtefact;
    private Artefact sjpPublicArtefact;
    private Artefact sjpPressArtefact;

    private Location location;
    private static final List<String> EXAMPLE_CSV =
        List.of(
            "0beac960-68a3-41db-9f51-8c71826eaf30,2022-07-25 14:45:18.836,2022-09-29 14:45:18.836,BI_LINGUAL,"
                + "MANUAL_UPLOAD,PUBLIC,MANUAL_UPLOAD,LIST,2022-06-29 00:00:00.0,1823,FAMILY_DAILY_CAUSE_LIST",
            "165ca91d-1e58-412a-80f5-1e5475a093e4,2022-06-29 14:45:18.836,2022-09-29 14:45:18.836,WELSH,"
                + "MANUAL_UPLOAD,PUBLIC,MANUAL_UPLOAD,GENERAL_PUBLICATION,2022-06-29 00:00:00.0,1815,SJP_PUBLIC_LIST",
            "10238a0f-d398-4356-9af4-a4dbbb17d455,2022-06-29 14:45:18.836,2022-09-29 14:45:18.836,ENGLISH,"
                + "MANUAL_UPLOAD,PUBLIC,MANUAL_UPLOAD,GENERAL_PUBLICATION,2022-06-29 00:00:00.0,1815,SJP_PUBLIC_LIST"
        );

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

        sjpPublicArtefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.SJP_PUBLIC_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .displayFrom(LocalDateTime.now().plusDays(1))
            .displayTo(LocalDateTime.now().plusDays(2))
            .expiryDate(LocalDateTime.now())
            .build();

        sjpPressArtefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.SJP_PRESS_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .displayFrom(LocalDateTime.now().plusDays(1))
            .displayTo(LocalDateTime.now().plusDays(2))
            .expiryDate(LocalDateTime.now())
            .build();
    }

    private void createClassifiedPayloads() {

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
        when(artefactRepository.save(any())).thenReturn(artefactWithIdAndPayloadUrl);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testCreationOfNewArtefactWhenCourtDoesNotExists() {
        artefactWithPayloadUrl.setLocationId(NO_COURT_EXISTS_IN_REFERENCE_DATA);
        when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID, LocationType.VENUE.name()))
            .thenReturn(Optional.empty());
        when(artefactRepository.save(any())).thenReturn(artefactWithIdAndPayloadUrl);
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
        when(artefactRepository.save(any())).thenReturn(artefactWithIdAndPayloadUrl);
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
        when(artefactRepository.save(any())).thenReturn(newArtefactWithId);
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
        when(artefactRepository.save(any())).thenReturn(newArtefactWithId);
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
        when(artefactRepository.save(any())).thenReturn(newArtefactWithId);
        when(azureBlobService.createPayload(PAYLOAD_STRIPPED, PAYLOAD)).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(any())).thenReturn(existingArtefact);
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
        when(artefactRepository.save(any())).thenReturn(artefactWithIdAndPayloadUrl);
        when(azureBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);

        Artefact returnedArtefact = publicationService.createPublication(artefactManualUpload, FILE);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testCreationOfNewArtefactWithFileWhenCourtDoesNotExists() {
        artefactWithPayloadUrl.setSearch(null);
        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(any())).thenReturn(artefactWithIdAndPayloadUrl);
        when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID, LocationType.VENUE.name()))
            .thenReturn(Optional.of(location));
        Artefact returnedArtefact = publicationService.createPublication(artefact, FILE);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testCreationOfNewArtefactWhenListTypeSjpPublic() {
        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(sjpPublicArtefact)).thenReturn(sjpPublicArtefact);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(sjpPublicArtefact, PAYLOAD);

        assertEquals(LocalDateTime.now().plusDays(7).toLocalDate(), returnedArtefact.getExpiryDate().toLocalDate(),
                     "Expiry date not set correctly for SJP public list");
    }

    @Test
    void testCreationOfNewArtefactWhenListTypeSjpPress() {
        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(sjpPressArtefact)).thenReturn(sjpPressArtefact);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(sjpPressArtefact, PAYLOAD);

        assertEquals(LocalDateTime.now().plusDays(7).toLocalDate(), returnedArtefact.getExpiryDate().toLocalDate(),
                     "Expiry date not set correctly for SJP press list");
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
        assertEquals(List.of(artefact), artefactSearchService.findAllByLocationIdAdmin(TEST_VALUE, USER_ID, true),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactCountService() {
        when(artefactRepository.countArtefactsByLocation()).thenReturn(List.of("1,3","2,4", "3,6"));
        assertEquals("location,count\n1,3\n2,4\n3,6\n", publicationService.countArtefactsByLocation(),
                     MESSAGES_MATCH);
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
            assertTrue(logCaptor.getInfoLogs().get(0).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);
            assertTrue(logCaptor.getInfoLogs().get(1).contains(SUCCESS), MESSAGES_MATCH);
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
    void testReportNoMatchArtefacts() {
        when(artefactRepository.findAllNoMatchArtefacts()).thenReturn(List.of(noMatchArtefact));
        publicationService.reportNoMatchArtefacts();
        verify(publicationServicesService).sendNoMatchArtefactsForReporting(Map.of(PROVENANCE_ID, PROVENANCE));
    }

    @Test
    void testReportMatchArtefactsWhenArtefactsNotFound() {
        when(artefactRepository.findAllNoMatchArtefacts()).thenReturn(Collections.emptyList());
        publicationService.reportNoMatchArtefacts();
        verifyNoInteractions(publicationServicesService);
    }

    @Test
    void testArchiveExpiredArtefacts() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));
        publicationService.archiveExpiredArtefacts();
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(azureBlobService).deletePublicationBlob(testArtefactId + ".pdf");
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
    }

    @Test
    void testArchiveExpiredArtefactsSjpPublic() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PUBLIC_LIST);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));
        publicationService.archiveExpiredArtefacts();
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(azureBlobService).deletePublicationBlob(artefactWithPayloadUrl.getArtefactId() + ".pdf");
        verify(azureBlobService).deletePublicationBlob(artefactWithPayloadUrl.getArtefactId() + ".xlsx");
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
    }

    @Test
    void testArchiveExpiredArtefactsSjpPress() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PRESS_LIST);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));
        publicationService.archiveExpiredArtefacts();
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(azureBlobService).deletePublicationBlob(artefactWithPayloadUrl.getArtefactId() + ".pdf");
        verify(azureBlobService).deletePublicationBlob(artefactWithPayloadUrl.getArtefactId() + ".xlsx");
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
    }

    @Test
    void testArchiveExpiredArtefactsFlatFile() {
        UUID testArtefactId = UUID.randomUUID();
        artefactWithPayloadUrl.setArtefactId(testArtefactId);
        artefactWithPayloadUrl.setListType(ListType.SJP_PRESS_LIST);
        artefactWithPayloadUrl.setIsFlatFile(true);
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(List.of(artefactWithPayloadUrl));
        publicationService.archiveExpiredArtefacts();
        verify(azureBlobService).deleteBlob(PAYLOAD_STRIPPED);
        verify(artefactRepository).archiveArtefact(testArtefactId.toString());
    }

    @Test
    void testArchiveExpiredArtefactsWhenArtefactsNotFound() {
        when(artefactRepository.findOutdatedArtefacts(any())).thenReturn(Collections.emptyList());
        publicationService.archiveExpiredArtefacts();
        verifyNoInteractions(azureBlobService);
    }

    @Test
    void testGetLocationTypeVenue() {
        List<ListType> venueListTypes = new ArrayList<>();
        venueListTypes.add(ListType.CROWN_DAILY_LIST);
        venueListTypes.add(ListType.CROWN_FIRM_LIST);
        venueListTypes.add(ListType.CROWN_WARNED_LIST);
        venueListTypes.add(ListType.MAGISTRATES_PUBLIC_LIST);
        venueListTypes.add(ListType.MAGISTRATES_STANDARD_LIST);
        venueListTypes.add(ListType.CIVIL_DAILY_CAUSE_LIST);
        venueListTypes.add(ListType.FAMILY_DAILY_CAUSE_LIST);
        venueListTypes.add(ListType.IAC_DAILY_LIST);

        venueListTypes.forEach(listType ->
                                   assertEquals(LocationType.VENUE,
                                                publicationService.getLocationType(listType),
                                                LOCATION_TYPE_MATCH));
    }

    @Test
    void testGetLocationTypeOwningHearingLocation() {
        List<ListType> venueListTypes = new ArrayList<>();
        venueListTypes.add(ListType.ET_DAILY_LIST);
        venueListTypes.add(ListType.ET_FORTNIGHTLY_PRESS_LIST);

        venueListTypes.forEach(listType ->
                                   assertEquals(LocationType.OWNING_HEARING_LOCATION,
                                                publicationService.getLocationType(listType),
                                                LOCATION_TYPE_MATCH));
    }

    @Test
    void testGetLocationTypeNational() {
        List<ListType> nationalListTypes = new ArrayList<>();
        nationalListTypes.add(ListType.SJP_PRESS_LIST);
        nationalListTypes.add(ListType.SJP_PUBLIC_LIST);
        nationalListTypes.add(ListType.CARE_STANDARDS_LIST);
        nationalListTypes.add(ListType.PRIMARY_HEALTH_LIST);

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

    @Test
    void testMiService() {
        when(artefactRepository.getMiData()).thenReturn(EXAMPLE_CSV);
        String testString = publicationService.getMiData();
        String[] splitLineString = testString.split(System.lineSeparator());
        long countLine1 = splitLineString[0].chars().filter(character -> character == ',').count();
        assertThat(testString)
            .as("Header row missing")
            .contains("source_artefact_id");
        assertThat(splitLineString)
            .as("Only one line exists - data must be missing, as only headers are printing")
            .hasSizeGreaterThanOrEqualTo(2);
        assertThat(splitLineString)
            .as("Wrong comma count compared to header row!")
            .allSatisfy(
                e -> assertThat(e.chars().filter(character -> character == ',').count()).isEqualTo(countLine1));
    }

    @Test
    void testLastReceivedDateIsSetForBlob() {
        ArgumentCaptor<Artefact> captor = ArgumentCaptor.forClass(Artefact.class);

        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(captor.capture())).thenReturn(artefactWithIdAndPayloadUrl);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        publicationService.createPublication(artefact, PAYLOAD);

        assertNotNull(captor.getValue().getLastReceivedDate(), "Last received date has not been populated");
    }

    @Test
    void testLastReceivedDateIsSetForFlatFile() {
        ArgumentCaptor<Artefact> captor = ArgumentCaptor.forClass(Artefact.class);

        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(captor.capture())).thenReturn(artefactWithIdAndPayloadUrl);
        publicationService.createPublication(artefact, FILE);

        assertNotNull(captor.getValue().getLastReceivedDate(), "Last received date has not been populated");
    }

    @Test
    void testSupersededCountIsUpdated() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage().name(),
                                                          artefact.getListType().name(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.of(artefact));

        artefact.setPayload("/" + UUID.randomUUID());
        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);

        ArgumentCaptor<Artefact> captor = ArgumentCaptor.forClass(Artefact.class);
        when(artefactRepository.save(captor.capture())).thenReturn(artefactWithIdAndPayloadUrl);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(1, captor.getValue().getSupersededCount(), "Superseded count has not been incremented");
    }

    @Test
    void testSupersededCountIsNotUpdated() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                          artefact.getLanguage().name(),
                                                          artefact.getListType().name(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());

        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);

        ArgumentCaptor<Artefact> captor = ArgumentCaptor.forClass(Artefact.class);
        when(artefactRepository.save(captor.capture())).thenReturn(artefactWithIdAndPayloadUrl);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(0, captor.getValue().getSupersededCount(), "Superseded count has been incremented");
    }

    @Test
    void testArchivedEndpoint() {
        String artefactId = UUID.randomUUID().toString();

        when(artefactRepository.findArtefactByArtefactId(artefactId))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));

        publicationService.archiveArtefactById(artefactId, UUID.randomUUID().toString());

        verify(azureBlobService, times(1))
            .deleteBlob(any());
        verify(azureBlobService, times(1))
            .deletePublicationBlob(any());
        verify(subscriptionManagementService, times(1))
            .sendDeletedArtefactForThirdParties(any());
        verify(artefactRepository, times(1))
            .archiveArtefact(artefactId);
    }

    @Test
    void testArchivedEndpointNotFound() {
        String artefactId = UUID.randomUUID().toString();

        when(artefactRepository.findArtefactByArtefactId(artefactId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            publicationService.archiveArtefactById(artefactId, UUID.randomUUID().toString());
        }, "Attempting to archive an artefact that does not exist should throw an exception");
    }

    @Test
    void testProcessCreatedPublication() {
        publicationService.processCreatedPublication(sjpPublicArtefact);
        verify(channelManagementService, times(1))
            .requestFileGeneration(sjpPublicArtefact.getArtefactId());
    }
}
