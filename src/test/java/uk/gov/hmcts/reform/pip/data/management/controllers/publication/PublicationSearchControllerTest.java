package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.STATUS_CODE_MATCH;

@ExtendWith(MockitoExtension.class)
class PublicationSearchControllerTest {
    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final String PROVENANCE = "provenance";
    private static final String LOCATION_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String PAYLOAD = "payload";
    private static final Float PAYLOAD_SIZE = (float) PAYLOAD.getBytes().length / 1024;
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
        .payloadSize(PAYLOAD_SIZE)
        .build();

    @Mock
    private PublicationSearchService publicationSearchService;

    @InjectMocks
    private PublicationSearchController publicationSearchController;

    @Test
    void testGetArtefactsBySearchReturnsWhenTrue() {
        when(publicationSearchService.findAllBySearch(SEARCH_TERM, TEST_STRING, USER_ID))
            .thenReturn(List.of(ARTEFACT_WITH_ID));
        assertEquals(HttpStatus.OK, publicationSearchController
                         .getAllRelevantArtefactsBySearchValue(SEARCH_TERM, TEST_STRING, USER_ID).getStatusCode(),
                     STATUS_CODE_MATCH
        );
    }

    @Test
    void testGetArtefactsBySearchReturnsWhenFalse() {
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
}
