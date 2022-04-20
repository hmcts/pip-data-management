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
import uk.gov.hmcts.reform.pip.data.management.database.CourtRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.court.Court;
import uk.gov.hmcts.reform.pip.data.management.models.court.CourtCsv;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;

import java.io.IOException;
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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.TestConstants.MESSAGES_MATCH;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports",
    "PMD.TooManyMethods", "PMD.TooManyFields"})
class PublicationServiceTest {

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    CourtRepository courtRepository;

    @Mock
    AzureBlobService azureBlobService;

    @Mock
    PayloadExtractor payloadExtractor;

    @Mock
    SubscriptionManagementService subscriptionManagementService;

    @InjectMocks
    PublicationService publicationService;

    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String PROVENANCE = "provenance";
    private static final String PROVENANCE_ID = "1234";
    private static final String MANUAL_UPLOAD_PROVENANCE = "MANUAL_UPLOAD";
    private static final String PAYLOAD = "This is a payload";
    private static final String PAYLOAD_URL = "https://ThisIsATestPayload";
    private static final String PAYLOAD_STRIPPED = "ThisIsATestPayload";
    private static final String COURT_ID = "123";
    private static final String TEST_KEY = "TestKey";
    private static final String TEST_VALUE = "TestValue";
    private static final CaseSearchTerm SEARCH_TERM_CASE_ID = CaseSearchTerm.CASE_ID;
    private static final CaseSearchTerm SEARCH_TERM_CASE_NAME = CaseSearchTerm.CASE_NAME;
    private static final CaseSearchTerm SEARCH_TERM_CASE_URN = CaseSearchTerm.CASE_URN;
    private static final Map<String, List<Object>> SEARCH_VALUES = new ConcurrentHashMap<>();
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);
    private static final String VALIDATION_ARTEFACT_NOT_MATCH = "Artefacts do not match";
    private static final String SUCCESSFUL_TRIGGER = "success - subscription sent";
    private static final String SUCCESS = "Success";
    private static final String DELETION_TRACK_LOG_MESSAGE = "Track: TestValue, Removed %s, at ";
    private static final String ROWID_RETURNS_UUID = "Row ID must match returned UUID";

    private static final String NO_COURT_EXISTS_IN_REFERENCE_DATA = "NoMatch1234";

    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;
    private Artefact artefactInTheFuture;
    private Artefact artefactFromThePast;
    private Artefact artefactFromNow;
    private Artefact artefactWithNullDateTo;
    private Artefact artefactWithSameDateFromAndTo;
    private Artefact artefactManualUpload;

    private List<Court> courts;

    @BeforeAll
    public static void setupSearchValues() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
    }

    @BeforeEach
    void setup() {
        artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .courtId(PROVENANCE_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .build();

        artefactWithPayloadUrl = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .courtId(COURT_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .build();

        artefactWithIdAndPayloadUrl = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .courtId(COURT_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
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

        intialiseManualUploadArtefact();
        initialiseCourts();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getCourtId(),artefact.getContentDate(),
                                                                    artefact.getLanguage().name(),
                                                                    artefact.getListType().name(),
                                                                    artefact.getProvenance()))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        lenient().when(courtRepository.findByCourtIdByProvenance(PROVENANCE, PROVENANCE_ID))
            .thenReturn(Optional.of(courts));
    }

    private void initialiseCourts() {
        CourtCsv courtCsvFirstExample = new CourtCsv();
        courtCsvFirstExample.setCourtName("Court Name First Example");
        Court court = new Court(courtCsvFirstExample);
        court.setCourtId(1234);

        courts = new ArrayList<>();
        courts.add(court);
    }

    private void intialiseManualUploadArtefact() {
        artefactManualUpload = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(MANUAL_UPLOAD_PROVENANCE)
            .courtId(PROVENANCE_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .build();
    }

    @Test
    void testCreationOfNewArtefact() {
        artefactWithPayloadUrl.setCourtId(PROVENANCE_ID);
        when(azureBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(courtRepository.findByCourtIdByProvenance(PROVENANCE, PROVENANCE_ID))
            .thenReturn(Optional.of(courts));
        when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testCreationOfNewArtefactWhenCourtDoesNotExists() {
        artefactWithPayloadUrl.setCourtId(NO_COURT_EXISTS_IN_REFERENCE_DATA);
        when(courtRepository.findByCourtIdByProvenance(PROVENANCE, PROVENANCE_ID))
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
        artefactWithPayloadUrl.setCourtId(PROVENANCE_ID);
        artefactWithPayloadUrl.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        artefactWithPayloadUrl.setLanguage(Language.ENGLISH);
        artefactWithPayloadUrl.setContentDate(CONTENT_DATE);

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
            .courtId(PROVENANCE_ID)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .build();

        Artefact newArtefactWithId = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .courtId(PROVENANCE_ID)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .build();

        when(courtRepository.findByCourtIdByProvenance(PROVENANCE, PROVENANCE_ID))
            .thenReturn(Optional.of(courts));
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
            .courtId(PROVENANCE_ID)
            .language(Language.ENGLISH)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        Artefact newArtefactWithId = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(MANUAL_UPLOAD_PROVENANCE)
            .courtId(PROVENANCE_ID)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .contentDate(CONTENT_DATE)
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

        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .courtId(PROVENANCE_ID)
            .contentDate(CONTENT_DATE)
            .language(Language.ENGLISH)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        Artefact existingArtefact = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .contentDate(CONTENT_DATE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .courtId(NO_COURT_EXISTS_IN_REFERENCE_DATA)
            .build();

        Artefact newArtefactWithId = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .courtId(NO_COURT_EXISTS_IN_REFERENCE_DATA)
            .contentDate(CONTENT_DATE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .build();

        when(artefactRepository.findArtefactByUpdateLogic(artefact.getCourtId(),artefact.getContentDate(),
                                                                    artefact.getLanguage().name(),
                                                                    artefact.getListType().name(),
                                                                    artefact.getProvenance()))
            .thenReturn(Optional.of(existingArtefact));

        when(courtRepository.findByCourtIdByProvenance(PROVENANCE, PROVENANCE_ID))
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
        artefactWithPayloadUrl.setCourtId(NO_COURT_EXISTS_IN_REFERENCE_DATA);
        when(azureBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(courtRepository.findByCourtIdByProvenance(PROVENANCE, PROVENANCE_ID))
            .thenReturn(Optional.empty());
        when(artefactRepository.save(artefact)).thenReturn(artefactWithIdAndPayloadUrl);
        Artefact returnedArtefact = publicationService.createPublication(artefact, FILE);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testCreationOfNewArtefactWithFileByManualUpload() {
        artefactWithPayloadUrl.setSearch(null);
        artefactWithPayloadUrl.setCourtId(PROVENANCE_ID);
        artefactWithPayloadUrl.setProvenance(MANUAL_UPLOAD_PROVENANCE);
        when(azureBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);

        Artefact returnedArtefact = publicationService.createPublication(artefactManualUpload, FILE);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testCreationOfNewArtefactWithFileWhenCourtDoesNotExists() {
        artefactWithPayloadUrl.setSearch(null);
        artefactWithPayloadUrl.setCourtId(PROVENANCE_ID);
        when(azureBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(courtRepository.findByCourtIdByProvenance(PROVENANCE, PROVENANCE_ID))
            .thenReturn(Optional.of(courts));
        Artefact returnedArtefact = publicationService.createPublication(artefact, FILE);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testArtefactPayloadFromAzureWhenAuthorized() {
        when(artefactRepository.findByArtefactIdVerified(any(), any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, publicationService.getPayloadByArtefactId(ARTEFACT_ID, true),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactFileFromAzureWhenAuthorized() {
        String string = "Hello";
        byte[] testData = string.getBytes();
        when(artefactRepository.findByArtefactIdVerified(any(), any())).thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobFile(any()))
            .thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), publicationService.getFlatFileByArtefactID(
            ARTEFACT_ID,
            true
                     ),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactPayloadFromAzureWhenUnauthorized() {
        when(artefactRepository.findByArtefactIdUnverified(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobData(any()))
            .thenReturn(PAYLOAD);
        assertEquals(PAYLOAD, publicationService.getPayloadByArtefactId(ARTEFACT_ID, false),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactContentFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactIdVerified(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            ()
                -> publicationService.getPayloadByArtefactId(ARTEFACT_ID, true),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testArtefactFileFromAzureWhenUnauthorized() {
        String string = "Hello";
        byte[] testData = string.getBytes();
        when(artefactRepository.findByArtefactIdUnverified(any(), any()))
            .thenReturn(Optional.of(artefactWithPayloadUrl));
        when(azureBlobService.getBlobFile(any()))
            .thenReturn(new ByteArrayResource(testData));

        assertEquals(new ByteArrayResource(testData), publicationService.getFlatFileByArtefactID(ARTEFACT_ID, false),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactFileFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactIdVerified(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            ()
                -> publicationService.getFlatFileByArtefactID(ARTEFACT_ID, true),
            "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenAuthorized() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .build();
        when(artefactRepository.findByArtefactIdVerified(any(), any())).thenReturn(Optional.of(artefact));
        assertEquals(artefact, publicationService.getMetadataByArtefactId(ARTEFACT_ID, true),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenUnauthorized() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .build();
        when(artefactRepository.findByArtefactIdUnverified(any(), any())).thenReturn(Optional.of(artefact));
        assertEquals(artefact, publicationService.getMetadataByArtefactId(ARTEFACT_ID, false),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactIdVerified(any(), any())).thenReturn(Optional.empty());
        assertThrows(
            NotFoundException.class,
            () -> publicationService.getPayloadByArtefactId(ARTEFACT_ID, true),
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
        when(artefactRepository.findByArtefactIdVerified(any(), any()))
            .thenReturn(Optional.of(artefactWithIdAndPayloadUrl));
        assertEquals(artefactWithIdAndPayloadUrl, publicationService.getMetadataByArtefactId(ARTEFACT_ID, true),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindByCourtIdWhenVerified() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .build();

        Artefact artefact2 = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.WELSH)
            .build();

        List<Artefact> artefactList = new ArrayList<>();
        artefactList.add(artefact);
        artefactList.add(artefact2);

        when(artefactRepository.findArtefactsByCourtIdVerified(any(), any()))
            .thenReturn(artefactList);

        assertEquals(artefactList, publicationService.findAllByCourtId("abc", true),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindByCourtIdWhenUnverified() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .build();

        Artefact artefact2 = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.WELSH)
            .build();

        List<Artefact> artefactList = new ArrayList<>();
        artefactList.add(artefact);
        artefactList.add(artefact2);

        when(artefactRepository.findArtefactsByCourtIdUnverified(any(), any()))
            .thenReturn(artefactList);

        assertEquals(artefactList, publicationService.findAllByCourtId("abc", false),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllBySearchCaseIdVerified() {
        when(artefactRepository.findArtefactBySearchVerified(eq(SEARCH_TERM_CASE_ID.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(List.of(artefactWithIdAndPayloadUrl));

        assertEquals(
            artefactWithIdAndPayloadUrl,
            publicationService.findAllBySearch(SEARCH_TERM_CASE_ID, TEST_VALUE, true).get(0),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllBySearchCaseIdUnverified() {
        when(artefactRepository.findArtefactBySearchUnverified(eq(SEARCH_TERM_CASE_ID.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(List.of(artefactWithIdAndPayloadUrl));

        assertEquals(
            artefactWithIdAndPayloadUrl,
            publicationService.findAllBySearch(SEARCH_TERM_CASE_ID, TEST_VALUE, false).get(0),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllNoArtefactsThrowsNotFound() {
        ArtefactNotFoundException ex = assertThrows(ArtefactNotFoundException.class, () ->
            publicationService.findAllBySearch(SEARCH_TERM_CASE_ID, "not found", true)
        );
        assertEquals("No Artefacts found with for CASE_ID with the value: not found",
                     ex.getMessage(), MESSAGES_MATCH
        );
    }

    @Test
    void testFindAllByCaseNameVerified() {
        when(artefactRepository.findArtefactByCaseNameVerified(eq(TEST_VALUE), any()))
            .thenReturn(List.of(artefactWithIdAndPayloadUrl));

        assertEquals(
            artefactWithIdAndPayloadUrl,
            publicationService.findAllBySearch(SEARCH_TERM_CASE_NAME, TEST_VALUE, true).get(0),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllByCaseNameUnverified() {
        when(artefactRepository.findArtefactByCaseNameUnverified(eq(TEST_VALUE), any()))
            .thenReturn(List.of(artefactWithIdAndPayloadUrl));

        assertEquals(
            artefactWithIdAndPayloadUrl,
            publicationService.findAllBySearch(SEARCH_TERM_CASE_NAME, TEST_VALUE, false).get(0),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllByCaseUrnVerified() {
        when(artefactRepository.findArtefactBySearchVerified(eq(SEARCH_TERM_CASE_URN.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(List.of(artefactWithIdAndPayloadUrl));

        assertEquals(
            artefactWithIdAndPayloadUrl,
            publicationService.findAllBySearch(SEARCH_TERM_CASE_URN, TEST_VALUE, true).get(0),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllByCaseUrnUnverified() {
        when(artefactRepository.findArtefactBySearchUnverified(eq(SEARCH_TERM_CASE_URN.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(List.of(artefactWithIdAndPayloadUrl));

        assertEquals(
            artefactWithIdAndPayloadUrl,
            publicationService.findAllBySearch(SEARCH_TERM_CASE_URN, TEST_VALUE, false).get(0),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testInvalidEnumTypeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            publicationService.findAllBySearch(CaseSearchTerm.valueOf("invalid"), TEST_VALUE, true));
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
        when(artefactRepository.findArtefactsByCourtIdAdmin(TEST_VALUE)).thenReturn(List.of(artefact));
        assertEquals(List.of(artefact), publicationService.findAllByCourtIdAdmin(TEST_VALUE, true, true),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCourtIdAdminNotAdmin() {
        when(artefactRepository.findArtefactsByCourtIdVerified(any(), any())).thenReturn(List.of(artefact));
        assertEquals(List.of(artefact), publicationService.findAllByCourtIdAdmin(TEST_VALUE, true, false),
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
            when(artefactRepository.findArtefactByArtefactId(ARTEFACT_ID.toString()))
                .thenReturn(Optional.of(artefactWithPayloadUrl));
            when(azureBlobService.deleteBlob(PAYLOAD_STRIPPED)).thenReturn(SUCCESS);
            doNothing().when(artefactRepository).delete(artefactWithPayloadUrl);

            publicationService.deleteArtefactById(ARTEFACT_ID.toString(), TEST_VALUE);
            assertEquals(SUCCESS, logCaptor.getInfoLogs().get(0), MESSAGES_MATCH);
            assertTrue(logCaptor.getInfoLogs().get(1).contains(String.format(DELETION_TRACK_LOG_MESSAGE, ARTEFACT_ID)),
                       MESSAGES_MATCH);
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

}
