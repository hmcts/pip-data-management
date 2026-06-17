package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactSearchService;
import uk.gov.hmcts.reform.pip.data.management.models.publication.CaseSearchResult;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationSearchService;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_VALUES;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_KEY;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_VALUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.MESSAGES_MATCH;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.STATUS_CODE_MATCH;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationSearchControllerTest {
    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final String PROVENANCE = "provenance";
    private static final String LOCATION_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String PAYLOAD_URL = "This is a test payload";
    private static final String EMPTY_FIELD = "";
    private static final CaseSearchTerm SEARCH_TERM = CaseSearchTerm.CASE_ID;
    private static final String TEST_STRING = "test";
    private static final String VALIDATION_EXPECTED_MESSAGE =
        "The expected exception does not contain the correct message";

    private static final Artefact ARTEFACT_WITH_ID = Artefact.builder()
        .artefactId(ARTEFACT_ID)
        .displayFrom(DISPLAY_FROM)
        .displayTo(DISPLAY_TO)
        .language(Language.ENGLISH)
        .provenance(PROVENANCE)
        .sensitivity(Sensitivity.PUBLIC)
        .type(ArtefactType.LIST)
        .payload(PAYLOAD_URL)
        .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
        .locationId(LOCATION_ID)
        .contentDate(CONTENT_DATE)
        .search(new ConcurrentHashMap<>())
        .payloadSize(10f)
        .build();

    private static final ListSearchConfig LIST_SEARCH_CONFIG = new ListSearchConfig();
    private static final String CASE_NUMBER_FIELD_NAME = "caseNumber";
    private static final String CASE_NAME_FIELD_NAME = "caseName";

    @Mock
    private PublicationSearchService publicationSearchService;

    @Mock
    private ArtefactSearchService artefactSearchService;

    @InjectMocks
    private PublicationSearchController publicationSearchController;

    @BeforeAll
    static void setup() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
        LIST_SEARCH_CONFIG.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        LIST_SEARCH_CONFIG.setCaseNumberFieldName(CASE_NUMBER_FIELD_NAME);
        LIST_SEARCH_CONFIG.setCaseNameFieldName(CASE_NAME_FIELD_NAME);
    }

    @Test
    void testCreateListSearchConfigReturnsCreated() {
        doNothing().when(publicationSearchService).createListSearchConfig(LIST_SEARCH_CONFIG, USER_ID);

        ResponseEntity<String> result = publicationSearchController.createListSearchConfig(LIST_SEARCH_CONFIG, USER_ID);

        assertThat(result.getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.CREATED);

        assertThat(result.getBody())
            .as(MESSAGES_MATCH)
            .isEqualTo("List search config successfully added by user " + USER_ID);
    }

    @Test
    void testUpdateListSearchConfigReturnsOk() {
        doNothing().when(publicationSearchService).updateListSearchConfig(TEST_STRING, LIST_SEARCH_CONFIG, USER_ID);

        ResponseEntity<String> result = publicationSearchController.updateListSearchConfig(
            TEST_STRING, LIST_SEARCH_CONFIG, USER_ID
        );

        assertThat(result.getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.OK);

        assertThat(result.getBody())
            .as(MESSAGES_MATCH)
            .isEqualTo("List search config successfully updated by user " + USER_ID);
    }

    @Test
    void testDeleteListSearchConfigReturnsOk() {
        doNothing().when(publicationSearchService).deleteListSearchConfig(TEST_STRING, USER_ID);

        ResponseEntity<String> result = publicationSearchController.deleteListSearchConfig(TEST_STRING, USER_ID);

        assertThat(result.getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.OK);

        assertThat(result.getBody())
            .as(MESSAGES_MATCH)
            .isEqualTo("List search config successfully deleted by user " + USER_ID);
    }

    @Test
    void testFindListSearchConfigByListTypeReturnsOk() {
        when(publicationSearchService.findListSearchConfigByListType(ListType.CIVIL_DAILY_CAUSE_LIST))
            .thenReturn(LIST_SEARCH_CONFIG);

        ResponseEntity<ListSearchConfig> result = publicationSearchController.getListSearchConfigByListType(
            ListType.CIVIL_DAILY_CAUSE_LIST, USER_ID
        );

        assertThat(result.getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.OK);

        ListSearchConfig body = result.getBody();
        assertThat(body.getListType())
            .as(MESSAGES_MATCH)
            .isEqualTo(ListType.CIVIL_DAILY_CAUSE_LIST);

        assertThat(body.getCaseNumberFieldName())
            .as(MESSAGES_MATCH)
            .isEqualTo(CASE_NUMBER_FIELD_NAME);

        assertThat(body.getCaseNameFieldName())
            .as(MESSAGES_MATCH)
            .isEqualTo(CASE_NAME_FIELD_NAME);

    }

    @Test
    void testGetArtefactsBySearchV2ReturnsWhenTrue() {
        when(publicationSearchService.findAllBySearch(SEARCH_TERM, TEST_STRING, USER_ID))
            .thenReturn(List.of(ARTEFACT_WITH_ID));
        assertEquals(HttpStatus.OK, publicationSearchController
                         .getAllRelevantArtefactsBySearchValue(SEARCH_TERM, TEST_STRING, USER_ID).getStatusCode(),
                     STATUS_CODE_MATCH
        );
    }

    @Test
    void testGetArtefactsBySearchV2ReturnsWhenFalse() {
        when(publicationSearchService.findAllBySearch(SEARCH_TERM, TEST_STRING, USER_ID))
            .thenReturn(List.of(ARTEFACT_WITH_ID));
        assertEquals(HttpStatus.OK, publicationSearchController
                         .getAllRelevantArtefactsBySearchValue(SEARCH_TERM, TEST_STRING, USER_ID)
                         .getStatusCode(),
                     STATUS_CODE_MATCH
        );
    }

    @Test
    void checkGetArtefactsByCourtIdReturnsOkWhenFalse() {
        List<Artefact> artefactList = List.of(ARTEFACT_WITH_ID);

        when(publicationSearchService.findAllByLocationIdAdmin(EMPTY_FIELD, USER_ID, false)).thenReturn(artefactList);
        ResponseEntity<List<Artefact>> unmappedArtefact = publicationSearchController
            .getAllRelevantArtefactsByLocationId(EMPTY_FIELD, USER_ID, false);

        assertEquals(artefactList, unmappedArtefact.getBody(), VALIDATION_EXPECTED_MESSAGE);
        assertEquals(HttpStatus.OK, unmappedArtefact.getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testFindArtefactSearchByArtefactIdReturnsOk() {
        ArtefactSearch artefactSearch = ArtefactSearch.builder()
            .artefactId(ARTEFACT_ID)
            .build();

        List<ArtefactSearch> expected = List.of(artefactSearch);

        when(artefactSearchService.findByArtefactId(ARTEFACT_ID)).thenReturn(expected);

        ResponseEntity<List<ArtefactSearch>> result =
            publicationSearchController.findArtefactSearchByArtefactId(ARTEFACT_ID, USER_ID);

        assertThat(result.getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.OK);

        assertThat(result.getBody())
            .as(MESSAGES_MATCH)
            .isEqualTo(expected);

    }

    @Test
    void testDeleteArtefactSearchByArtefactIdReturnsOk() {
        doNothing().when(artefactSearchService).deleteByArtefactId(ARTEFACT_ID);

        ResponseEntity<String> result =
            publicationSearchController.deleteArtefactSearchByArtefactId(ARTEFACT_ID, USER_ID);

        assertThat(result.getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.OK);

        assertThat(result.getBody())
            .as(MESSAGES_MATCH)
            .isEqualTo("Artefact search rows successfully deleted for artefactId " + ARTEFACT_ID);
    }

    @Test
    void testGetCasesByCaseNumberReturnsOk() {
        CaseSearchResult caseSearchResult = new CaseSearchResult(TEST_STRING, "Test Case Name");
        when(publicationSearchService.findCasesByCaseNumber(TEST_STRING))
            .thenReturn(List.of(caseSearchResult));

        ResponseEntity<List<CaseSearchResult>> result =
            publicationSearchController.getCasesByCaseNumber(TEST_STRING, USER_ID);

        assertThat(result.getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.OK);

        assertThat(result.getBody())
            .as(MESSAGES_MATCH)
            .hasSize(1);

        assertThat(result.getBody().get(0).getCaseNumber())
            .as(MESSAGES_MATCH)
            .isEqualTo(TEST_STRING);
    }

    @Test
    void testGetCasesByCaseNameReturnsOk() {
        CaseSearchResult caseSearchResult = new CaseSearchResult("123", TEST_STRING);
        when(publicationSearchService.findCasesByCaseName(TEST_STRING))
            .thenReturn(List.of(caseSearchResult));

        ResponseEntity<List<CaseSearchResult>> result =
            publicationSearchController.getCasesByCaseName(TEST_STRING, USER_ID);

        assertThat(result.getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.OK);

        assertThat(result.getBody())
            .as(MESSAGES_MATCH)
            .hasSize(1);

        assertThat(result.getBody().get(0).getCaseName())
            .as(MESSAGES_MATCH)
            .isEqualTo(TEST_STRING);
    }
}
