package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactSearchRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ListSearchConfigRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ArtefactSearchServiceTest {
    private static final UUID TEST_ID = UUID.randomUUID();
    private static final ListSearchConfig LIST_SEARCH_CONFIG = new ListSearchConfig();
    private static final String caseNumber = "123";
    private static final String caseName = "Case A";
    private static final String CASE_NUMBER_FIELD_NAME = "caseNumber";
    private static final String CASE_NAME_FIELD_NAME = "caseName";
    private final Artefact artefactWithIdAndListType = Artefact.builder()
        .artefactId(UUID.randomUUID())
        .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
        .build();

    @Mock
    private ArtefactSearchRepository artefactSearchRepository;

    @Mock
    private ListSearchConfigRepository listSearchConfigRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ArtefactSearchService artefactSearchService;

    @Test
    void testFindArtefactSearchByIDSuccess() {
        UUID artefactId = UUID.randomUUID();
        ArtefactSearch row = ArtefactSearch.builder()
            .artefactId(artefactId)
            .caseNumber(caseNumber)
            .caseName(caseName)
            .build();

        when(artefactSearchRepository.findByArtefactId(artefactId))
            .thenReturn(Optional.of(List.of(row)));

        List<ArtefactSearch> result = artefactSearchService.findByArtefactId(artefactId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCaseNumber()).isEqualTo(caseNumber);
    }


    @Test
    void testFindArtefactSearchByIDException() {
        UUID artefactId = UUID.randomUUID();

        when(artefactSearchRepository.findByArtefactId(artefactId))
            .thenReturn(Optional.of(Collections.emptyList()));

        assertThrows(NotFoundException.class, () -> artefactSearchService.findByArtefactId(artefactId));
    }

    @Test
    void testDeleteArtefactSearchByIDSuccess() {
        UUID artefactId = UUID.randomUUID();
        when(artefactSearchRepository.findByArtefactId(artefactId))
            .thenReturn(Optional.of(List.of(ArtefactSearch.builder().artefactId(artefactId).build())));

        artefactSearchService.deleteByArtefactId(artefactId);

        verify(artefactSearchRepository).deleteByArtefactId(artefactId);
    }

    @Test
    void testDeleteArtefactSearchByIDException() {
        UUID artefactId = UUID.randomUUID();
        when(artefactSearchRepository.findByArtefactId(artefactId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> artefactSearchService.deleteByArtefactId(artefactId));

        verify(artefactSearchRepository, never()).deleteByArtefactId(artefactId);
    }


    @Test
    void testArtefactSearchStoreSuccess() throws Exception {
        UUID artefactId = artefactWithIdAndListType.getArtefactId();
        String payload = new ObjectMapper().writeValueAsString(
            Map.of(
                "hearing", Map.of(
                    "caseNumber", "123",
                    "caseName", "Case A"
                )
            )
        );

        ListSearchConfig listSearchConfig = new ListSearchConfig();
        listSearchConfig.setCaseNumberFieldName(CASE_NUMBER_FIELD_NAME);
        listSearchConfig.setCaseNameFieldName(CASE_NAME_FIELD_NAME);

        when(listSearchConfigRepository.findByListType(artefactWithIdAndListType.getListType()))
            .thenReturn(Optional.of(listSearchConfig));

        when(objectMapper.readTree(payload)).thenReturn(new ObjectMapper().readTree(payload));

        artefactSearchService.artefactSearchStore(artefactWithIdAndListType, payload);

        verify(artefactSearchRepository).deleteByArtefactId(artefactId);

        verify(artefactSearchRepository).saveAll(assertArg(rows -> {
            assertThat(rows).singleElement().satisfies(row -> {
                assertThat(row.getArtefactId()).isEqualTo(artefactId);
                assertThat(row.getCaseNumber()).isEqualTo("123");
                assertThat(row.getCaseName()).isEqualTo("Case A");
            });
        }));
    }
}
