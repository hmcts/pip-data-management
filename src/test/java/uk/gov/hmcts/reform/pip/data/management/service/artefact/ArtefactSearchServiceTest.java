package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationCsv;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ExtendWith(MockitoExtension.class)
class ArtefactSearchServiceTest {
    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    ArtefactService artefactService;

    @InjectMocks
    ArtefactSearchService artefactSearchService;

    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String PROVENANCE = "provenance";
    private static final String PROVENANCE_ID = "1234";
    private static final String PAYLOAD_URL = "https://ThisIsATestPayload";
    private static final String LOCATION_ID = "123";
    private static final String TEST_KEY = "TestKey";
    private static final String TEST_VALUE = "TestValue";
    private static final CaseSearchTerm SEARCH_TERM_CASE_ID = CaseSearchTerm.CASE_ID;
    private static final CaseSearchTerm SEARCH_TERM_CASE_NAME = CaseSearchTerm.CASE_NAME;
    private static final CaseSearchTerm SEARCH_TERM_CASE_URN = CaseSearchTerm.CASE_URN;
    private static final Map<String, List<Object>> SEARCH_VALUES = new ConcurrentHashMap<>();
    private static final String VALIDATION_ARTEFACT_NOT_MATCH = "Artefacts do not match";
    private static final String VALIDATION_MORE_THAN_PUBLIC = "More than the public artefact has been found";

    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final LocalDateTime START_OF_TODAY_CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay();

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrlClassified;

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

        when(artefactRepository.findArtefactsByLocationId(any(), any()))
            .thenReturn(artefactList);

        when(artefactService.isAuthorised(artefact, USER_ID))
            .thenReturn(true);

        when(artefactService.isAuthorised(artefact2, USER_ID))
            .thenReturn(true);

        assertEquals(artefactList, artefactSearchService.findAllByLocationId("abc", USER_ID),
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

        when(artefactRepository.findArtefactsByLocationId(any(), any()))
            .thenReturn(artefactList);

        when(artefactService.isAuthorised(artefact, USER_ID))
            .thenReturn(true);

        when(artefactService.isAuthorised(artefact2, USER_ID))
            .thenReturn(false);

        List<Artefact> artefacts = artefactSearchService.findAllByLocationId("abc", USER_ID);

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

        when(artefactService.isAuthorised(artefact, USER_ID))
            .thenReturn(false);

        when(artefactService.isAuthorised(artefact2, USER_ID))
            .thenReturn(true);

        List<Artefact> artefacts = artefactSearchService.findAllByLocationId("abc", USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefact2, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllBySearchCaseIdClassifiedAndAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_ID.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(true);

        assertEquals(
            list,
            artefactSearchService.findAllBySearch(SEARCH_TERM_CASE_ID, TEST_VALUE, USER_ID),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllBySearchCaseIdClassifiedAndNotAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_ID.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(false);

        List<Artefact> artefacts = artefactSearchService.findAllBySearch(SEARCH_TERM_CASE_ID, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllBySearchCaseIdUnverified() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_ID.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        List<Artefact> artefacts = artefactSearchService.findAllBySearch(SEARCH_TERM_CASE_ID, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllNoArtefactsThrowsNotFound() {
        ArtefactNotFoundException ex = assertThrows(ArtefactNotFoundException.class, () ->
            artefactSearchService.findAllBySearch(SEARCH_TERM_CASE_ID, "not found", USER_ID)
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

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(true);

        assertEquals(
            list,
            artefactSearchService.findAllBySearch(SEARCH_TERM_CASE_NAME, TEST_VALUE, USER_ID),
            VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCaseNameClassifiedAndNotAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactByCaseName(eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(false);

        List<Artefact> artefacts = artefactSearchService.findAllBySearch(SEARCH_TERM_CASE_NAME, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCaseNameUnverified() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactByCaseName(eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, null))
            .thenReturn(true);

        List<Artefact> artefacts = artefactSearchService.findAllBySearch(SEARCH_TERM_CASE_NAME, TEST_VALUE, null);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCaseUrnClassifiedAndAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_URN.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(true);

        assertEquals(
            list,
            artefactSearchService.findAllBySearch(SEARCH_TERM_CASE_URN, TEST_VALUE, USER_ID),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllByCaseUrnClassifiedAndNotAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_URN.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(false);

        List<Artefact> artefacts = artefactSearchService.findAllBySearch(SEARCH_TERM_CASE_URN, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCaseUrnUnverified() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_URN.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, null))
            .thenReturn(true);

        List<Artefact> artefacts = artefactSearchService.findAllBySearch(SEARCH_TERM_CASE_URN, TEST_VALUE, null);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testInvalidEnumTypeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            artefactSearchService.findAllBySearch(CaseSearchTerm.valueOf("invalid"), TEST_VALUE, USER_ID));
    }

    @Test
    void testFindAllByCourtIdAdminNotAdmin() {
        when(artefactRepository.findArtefactsByLocationId(any(), any())).thenReturn(List.of(artefact));
        when(artefactService.isAuthorised(artefact, USER_ID))
            .thenReturn(true);
        assertEquals(List.of(artefact), artefactSearchService.findAllByLocationIdAdmin(TEST_VALUE, USER_ID, false),
                     VALIDATION_ARTEFACT_NOT_MATCH);
    }
}
