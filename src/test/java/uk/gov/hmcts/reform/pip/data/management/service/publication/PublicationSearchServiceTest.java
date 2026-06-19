package uk.gov.hmcts.reform.pip.data.management.service.publication;

import nl.altindag.log.LogCaptor;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactSearchRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ListSearchConfigRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateListSearchConfigConflictException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactCaseInfo;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.CONTENT_DATE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD_URL;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_TERM_CASE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_TERM_CASE_NAME;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_TERM_CASE_URN;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_VALUES;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SOURCE_ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_KEY;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_VALUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.USER_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.VALIDATION_ARTEFACT_NOT_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.VALIDATION_MORE_THAN_PUBLIC;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationSearchServiceTest {
    private static final UUID ACTIONING_USER_ID = UUID.randomUUID();
    private static final UUID TEST_ID = UUID.randomUUID();
    private static final String ABC = "abc";
    private static final ListSearchConfig LIST_SEARCH_CONFIG = new ListSearchConfig();
    private static final String CASE_NUMBER_FIELD_NAME = "caseNumber";
    private static final String CASE_NAME_FIELD_NAME = "caseName";

    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private ArtefactSearchRepository artefactSearchRepository;

    @Mock
    private ListSearchConfigRepository listSearchConfigRepository;

    @Mock
    private PublicationRetrievalService publicationRetrievalService;

    @InjectMocks
    private PublicationSearchService publicationSearchService;

    private final Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
    private final Artefact artefactWithIdAndPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithIdAndPayloadUrl();
    private final Artefact artefactWithIdAndPayloadUrlClassified = Artefact.builder()
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

    @BeforeAll
    static void setup() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
        LIST_SEARCH_CONFIG.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        LIST_SEARCH_CONFIG.setCaseNumberFieldName(CASE_NUMBER_FIELD_NAME);
        LIST_SEARCH_CONFIG.setCaseNameFieldName(CASE_NAME_FIELD_NAME);
    }

    @Test
    void testCreateListSearchConfigSuccess() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSearchService.class)) {
            when(listSearchConfigRepository.save(LIST_SEARCH_CONFIG)).thenReturn(LIST_SEARCH_CONFIG);

            publicationSearchService.createListSearchConfig(LIST_SEARCH_CONFIG, ACTIONING_USER_ID);

            assertThat(logCaptor.getInfoLogs())
                .hasSize(1);

            assertThat((logCaptor.getInfoLogs().get(0)))
                .contains("Add list search config");

            assertThat(logCaptor.getErrorLogs())
                .isEmpty();
        }
    }

    @Test
    void testCreateListSearchConfigWithDataIntegrityViolationException() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSearchService.class)) {
            when(listSearchConfigRepository.save(LIST_SEARCH_CONFIG))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

            assertThatThrownBy(() -> publicationSearchService
                .createListSearchConfig(LIST_SEARCH_CONFIG, ACTIONING_USER_ID))
                .isInstanceOf(CreateListSearchConfigConflictException.class)
                .hasMessage("List search config for list type CIVIL_DAILY_CAUSE_LIST already exists");

            assertThat(logCaptor.getInfoLogs())
                .isEmpty();

            assertThat(logCaptor.getErrorLogs())
                .hasSize(1);

            assertThat((logCaptor.getErrorLogs().get(0)))
                .contains("List search config for list type CIVIL_DAILY_CAUSE_LIST already exists");
        }
    }

    @Test
    void testUpdateListSearchConfigSuccess() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSearchService.class)) {
            when(listSearchConfigRepository.findById(TEST_ID)).thenReturn(Optional.of(LIST_SEARCH_CONFIG));

            publicationSearchService.updateListSearchConfig(TEST_ID.toString(), LIST_SEARCH_CONFIG, ACTIONING_USER_ID);

            assertThat(logCaptor.getInfoLogs())
                .hasSize(1);

            assertThat((logCaptor.getInfoLogs().get(0)))
                .contains("Update list search config");

            assertThat(logCaptor.getErrorLogs())
                .isEmpty();

            verify(listSearchConfigRepository).save(LIST_SEARCH_CONFIG);
        }
    }

    @Test
    void testUpdateListSearchConfigNotFound() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSearchService.class)) {
            when(listSearchConfigRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicationSearchService
                .updateListSearchConfig(TEST_ID.toString(), LIST_SEARCH_CONFIG, ACTIONING_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("List search config for ID %s does not exist", TEST_ID));

            assertThat(logCaptor.getInfoLogs())
                .isEmpty();

            verify(listSearchConfigRepository, never()).save(LIST_SEARCH_CONFIG);
        }
    }

    @Test
    void testDeleteListSearchConfigSuccess() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSearchService.class)) {
            when(listSearchConfigRepository.findById(TEST_ID)).thenReturn(Optional.of(LIST_SEARCH_CONFIG));

            publicationSearchService.deleteListSearchConfig(TEST_ID.toString(), ACTIONING_USER_ID);

            assertThat(logCaptor.getInfoLogs())
                .hasSize(1);

            assertThat((logCaptor.getInfoLogs().get(0)))
                .contains("Delete list search config");

            assertThat(logCaptor.getErrorLogs())
                .isEmpty();

            verify(listSearchConfigRepository).deleteById(TEST_ID);
        }
    }

    @Test
    void testDeleteListSearchConfigNotFound() {
        try (LogCaptor logCaptor = LogCaptor.forClass(PublicationSearchService.class)) {
            when(listSearchConfigRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicationSearchService
                .deleteListSearchConfig(TEST_ID.toString(), ACTIONING_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("List search config for ID %s does not exist", TEST_ID));

            assertThat(logCaptor.getInfoLogs())
                .isEmpty();

            verify(listSearchConfigRepository, never()).deleteById(TEST_ID);
        }
    }

    @Test
    void testFindListSearchConfigByListTypeSuccess() {
        when(listSearchConfigRepository.findByListType(ListType.CIVIL_DAILY_CAUSE_LIST))
            .thenReturn(Optional.of(LIST_SEARCH_CONFIG));

        ListSearchConfig listSearchConfig = publicationSearchService.findListSearchConfigByListType(
            ListType.CIVIL_DAILY_CAUSE_LIST
        );

        assertThat(listSearchConfig.getListType())
            .isEqualTo(ListType.CIVIL_DAILY_CAUSE_LIST);

        assertThat(listSearchConfig.getCaseNumberFieldName())
            .isEqualTo(CASE_NUMBER_FIELD_NAME);

        assertThat(listSearchConfig.getCaseNameFieldName())
            .isEqualTo(CASE_NAME_FIELD_NAME);
    }

    @Test
    void testFindListSearchConfigByListTypeNotFound() {
        when(listSearchConfigRepository.findByListType(ListType.CIVIL_DAILY_CAUSE_LIST))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicationSearchService
            .findListSearchConfigByListType(ListType.CIVIL_DAILY_CAUSE_LIST))
            .isInstanceOf(NotFoundException.class)
            .hasMessage(String.format("List search config for list type %s does not exist",
                                      ListType.CIVIL_DAILY_CAUSE_LIST));
    }

    @Test
    void testFindByCourtIdWhenVerifiedAndAuthorised() {
        Artefact artefactPublic = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        Artefact artefactClassified = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.WELSH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        List<Artefact> artefactList = new ArrayList<>();
        artefactList.add(artefactPublic);
        artefactList.add(artefactClassified);

        when(artefactRepository.findArtefactsByLocationId(any(), any()))
            .thenReturn(artefactList);

        when(publicationRetrievalService.isAuthorised(artefactPublic, USER_ID))
            .thenReturn(true);

        when(publicationRetrievalService.isAuthorised(artefactClassified, USER_ID))
            .thenReturn(true);

        assertEquals(artefactList, publicationSearchService.findAllByLocationId(ABC, USER_ID),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindByCourtIdWhenVerifiedAndNotAuthorised() {
        Artefact artefactPublic = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        Artefact artefactClassified = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.WELSH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .build();

        List<Artefact> artefactList = new ArrayList<>();
        artefactList.add(artefactPublic);
        artefactList.add(artefactClassified);

        when(artefactRepository.findArtefactsByLocationId(any(), any()))
            .thenReturn(artefactList);

        when(publicationRetrievalService.isAuthorised(artefactPublic, USER_ID))
            .thenReturn(true);

        when(publicationRetrievalService.isAuthorised(artefactClassified, USER_ID))
            .thenReturn(false);

        List<Artefact> artefacts = publicationSearchService.findAllByLocationId(ABC, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactPublic, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindByCourtIdWhenUnverified() {
        Artefact artefactClassified = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .build();

        Artefact artefactPublic = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.WELSH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();

        List<Artefact> artefactList = new ArrayList<>();
        artefactList.add(artefactClassified);
        artefactList.add(artefactPublic);

        when(artefactRepository.findArtefactsByLocationId(any(), any()))
            .thenReturn(artefactList);

        when(publicationRetrievalService.isAuthorised(artefactClassified, USER_ID))
            .thenReturn(false);

        when(publicationRetrievalService.isAuthorised(artefactPublic, USER_ID))
            .thenReturn(true);

        List<Artefact> artefacts = publicationSearchService.findAllByLocationId(ABC, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactPublic, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllBySearchCaseIdClassifiedAndAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_ID.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(true);

        assertEquals(
            list,
            publicationSearchService.findAllBySearch(SEARCH_TERM_CASE_ID, TEST_VALUE, USER_ID),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllBySearchCaseIdClassifiedAndNotAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_ID.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(false);

        List<Artefact> artefacts = publicationSearchService.findAllBySearch(SEARCH_TERM_CASE_ID, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllBySearchCaseIdUnverified() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_ID.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        List<Artefact> artefacts = publicationSearchService.findAllBySearch(SEARCH_TERM_CASE_ID, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllNoArtefactsThrowsNotFound() {
        ArtefactNotFoundException ex = assertThrows(ArtefactNotFoundException.class, () ->
            publicationSearchService.findAllBySearch(SEARCH_TERM_CASE_ID, "not found", USER_ID)
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

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(true);

        assertEquals(
            list,
            publicationSearchService.findAllBySearch(SEARCH_TERM_CASE_NAME, TEST_VALUE, USER_ID),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllByCaseNameClassifiedAndNotAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactByCaseName(eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(false);

        List<Artefact> artefacts = publicationSearchService.findAllBySearch(SEARCH_TERM_CASE_NAME, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCaseNameUnverified() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactByCaseName(eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrl, null))
            .thenReturn(true);

        List<Artefact> artefacts = publicationSearchService.findAllBySearch(SEARCH_TERM_CASE_NAME, TEST_VALUE, null);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCaseUrnClassifiedAndAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_URN.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(true);

        assertEquals(
            list,
            publicationSearchService.findAllBySearch(SEARCH_TERM_CASE_URN, TEST_VALUE, USER_ID),
            VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllByCaseUrnClassifiedAndNotAuthorised() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_URN.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrl, USER_ID))
            .thenReturn(true);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrlClassified, USER_ID))
            .thenReturn(false);

        List<Artefact> artefacts = publicationSearchService.findAllBySearch(SEARCH_TERM_CASE_URN, TEST_VALUE, USER_ID);

        assertEquals(1, artefacts.size(), VALIDATION_MORE_THAN_PUBLIC);
        assertEquals(artefactWithIdAndPayloadUrl, artefacts.get(0), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindAllByCaseUrnUnverified() {
        List<Artefact> list = List.of(artefactWithIdAndPayloadUrl, artefactWithIdAndPayloadUrlClassified);
        when(artefactRepository.findArtefactBySearch(eq(SEARCH_TERM_CASE_URN.dbValue), eq(TEST_VALUE), any()))
            .thenReturn(list);

        when(publicationRetrievalService.isAuthorised(artefactWithIdAndPayloadUrl, null))
            .thenReturn(true);

        List<Artefact> artefacts = publicationSearchService.findAllBySearch(SEARCH_TERM_CASE_URN, TEST_VALUE, null);

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
        when(publicationRetrievalService.isAuthorised(artefact, USER_ID))
            .thenReturn(true);
        assertEquals(List.of(artefact), publicationSearchService.findAllByLocationIdAdmin(TEST_VALUE, USER_ID, false),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindAllByCourtIdAdmin() {
        when(artefactRepository.findArtefactsByLocationIdAdmin(any(), any())).thenReturn(List.of(artefact));
        assertEquals(List.of(artefact), publicationSearchService.findAllByLocationIdAdmin(TEST_VALUE, USER_ID, true),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testFindCasesByCaseNumber() {
        ArtefactSearch artefactSearch = ArtefactSearch.builder()
            .caseNumber(TEST_VALUE)
            .caseName("Test Case Name")
            .build();

        when(artefactSearchRepository.findByCaseNumberIgnoreCase(eq(TEST_VALUE), any()))
            .thenReturn(List.of(artefactSearch));

        List<ArtefactCaseInfo> results = publicationSearchService.findCasesByCaseNumber(TEST_VALUE);

        assertEquals(1, results.size(), VALIDATION_ARTEFACT_NOT_MATCH);
        assertEquals(TEST_VALUE, results.get(0).getCaseNumber(), VALIDATION_ARTEFACT_NOT_MATCH);
        assertEquals("Test Case Name", results.get(0).getCaseName(), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindCasesByCaseNumberReturnsEmptyListWhenNotFound() {
        when(artefactSearchRepository.findByCaseNumberIgnoreCase(any(), any())).thenReturn(List.of());

        List<ArtefactCaseInfo> results = publicationSearchService.findCasesByCaseNumber("not found");
        assertEquals(0, results.size(), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindCasesByCaseName() {
        ArtefactSearch artefactSearch = ArtefactSearch.builder()
            .caseNumber("123")
            .caseName(TEST_VALUE)
            .build();

        when(artefactSearchRepository.findTop50ByCaseNameContainingIgnoreCase(eq(TEST_VALUE), any()))
            .thenReturn(List.of(artefactSearch));

        List<ArtefactCaseInfo> results = publicationSearchService.findCasesByCaseName(TEST_VALUE);

        assertEquals(1, results.size(), VALIDATION_ARTEFACT_NOT_MATCH);
        assertEquals(TEST_VALUE, results.get(0).getCaseName(), VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testFindCasesByCaseNameReturnsEmptyListWhenNotFound() {
        when(artefactSearchRepository.findTop50ByCaseNameContainingIgnoreCase(any(), any())).thenReturn(List.of());

        List<ArtefactCaseInfo> results = publicationSearchService.findCasesByCaseName("not found");
        assertEquals(0, results.size(), VALIDATION_ARTEFACT_NOT_MATCH);
    }
}
