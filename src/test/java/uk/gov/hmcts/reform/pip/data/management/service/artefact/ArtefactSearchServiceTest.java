package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import org.junit.Assert;
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
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.CONTENT_DATE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_VENUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD_URL;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_TERM_CASE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_TERM_CASE_NAME;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_TERM_CASE_URN;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_TERM_PARTY_NAME;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_VALUES;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SOURCE_ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_KEY;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_VALUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.USER_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.VALIDATION_ARTEFACT_NOT_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.VALIDATION_MORE_THAN_PUBLIC;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@SuppressWarnings({"PMD.ExcessiveImports"})
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

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrlClassified;

    private Location location;
    private static final String ABC = "abc";

    @BeforeAll
    public static void setupSearchValues() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
    }

    @BeforeEach
    void setup() {
        createPayloads();
        createClassifiedPayloads();

        location = ArtefactConstantTestHelper.initialiseCourts();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                                    artefact.getLanguage().name(),
                                                                    artefact.getListType().name(),
                                                                    artefact.getProvenance()
            ))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        lenient().when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID,
                                                                       LOCATION_VENUE
            ))
            .thenReturn(Optional.of(location));
    }

    private void createPayloads() {
        artefact = ArtefactConstantTestHelper.buildArtefact();
        artefactWithPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithPayloadUrl();
        artefactWithIdAndPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithIdAndPayloadUrl();
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

        location = ArtefactConstantTestHelper.initialiseCourts();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(), artefact.getContentDate(),
                                                                    artefact.getLanguage().name(),
                                                                    artefact.getListType().name(),
                                                                    artefact.getProvenance()
            ))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        lenient().when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID,
                                                                       LOCATION_VENUE
            ))
            .thenReturn(Optional.of(location));
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

        assertEquals(artefactList, artefactSearchService.findAllByLocationId(ABC, USER_ID),
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

        List<Artefact> artefacts = artefactSearchService.findAllByLocationId(ABC, USER_ID);

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

        List<Artefact> artefacts = artefactSearchService.findAllByLocationId(ABC, USER_ID);

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
            VALIDATION_ARTEFACT_NOT_MATCH
        );
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
    void testFindAllByPartyNameClassifiedAndAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactsByPartyName(eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(true);

        assertEquals(
            list,
            artefactSearchService.findAllBySearch(SEARCH_TERM_PARTY_NAME, TEST_VALUE, USER_ID),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllByPartyNameClassifiedAndNotAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactsByPartyName(eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(false);

        List<Artefact> artefacts = artefactSearchService.findAllBySearch(SEARCH_TERM_PARTY_NAME, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByPartyNameUnverified() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactsByPartyName(eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(artefactService.isAuthorised(artefactWithIdAndPayloadUrl, null))
            .thenReturn(true);

        List<Artefact> artefacts = artefactSearchService.findAllBySearch(SEARCH_TERM_PARTY_NAME, TEST_VALUE, null);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testInvalidEnumTypeThrows() {
        try {
            CaseSearchTerm.valueOf("invalid");
            Assert.fail("No exception thrown for invalid enum");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage(), "Exception is not empty");
        }
    }

    @Test
    void testFindAllByCourtIdAdminNotAdmin() {
        when(artefactRepository.findArtefactsByLocationId(any(), any())).thenReturn(List.of(artefact));
        when(artefactService.isAuthorised(artefact, USER_ID))
            .thenReturn(true);
        assertEquals(List.of(artefact), artefactSearchService.findAllByLocationIdAdmin(TEST_VALUE, USER_ID, false),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllByCourtIdAdmin() {
        when(artefactRepository.findArtefactsByLocationIdAdmin(TEST_VALUE)).thenReturn(List.of(artefact));
        assertEquals(List.of(artefact), artefactSearchService.findAllByLocationIdAdmin(TEST_VALUE, USER_ID, true),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }
}
