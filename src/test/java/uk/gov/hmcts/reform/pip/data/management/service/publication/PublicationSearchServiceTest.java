package uk.gov.hmcts.reform.pip.data.management.service.publication;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@SuppressWarnings({"PMD.ExcessiveImports"})
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationSearchServiceTest {
    private static final String ABC = "abc";

    @Mock
    private ArtefactRepository artefactRepository;

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
    public static void setupSearchValues() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
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
}
