package uk.gov.hmcts.reform.pip.data.management.database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-jpa")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArtefactRepositoryTest {
    private static final LocalDateTime TODAY = LocalDateTime.of(2025, 2, 5, 1, 1, 2);
    private static final LocalDateTime TOMORROW = LocalDateTime.of(2025, 2, 6, 1, 1, 2);
    private static final LocalDateTime YESTERDAY = LocalDateTime.of(2025, 2, 4, 1, 1, 2);
    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String LOCATION_ID = "1";
    private static final String NO_MATCH_LOCATION_ID = "NoMatch99";
    private static final String INVALID_LOCATION_ID = "9";
    private static final String INVALID_ARTEFACT_ID = UUID.randomUUID().toString();
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String ARTEFACT_MATCHED_MESSAGE = "Artefact does not match";
    private static final String ARTEFACT_EMPTY_MESSAGE = "Artefact is not empty";
    private static final String RESULT_MATCHED_MESSAGE = "Result does not match";

    private UUID artefactId1;
    private UUID artefactId2;
    private UUID artefactId3;
    private UUID artefactId4;
    private UUID artefactId5;

    @Autowired
    ArtefactRepository artefactRepository;

    @BeforeAll
    void setup() {
        Artefact artefact1 = new Artefact();
        artefact1.setLocationId(LOCATION_ID);
        artefact1.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        artefact1.setIsArchived(false);
        setCommonArtefactProperties(artefact1);

        Artefact savedArtefact = artefactRepository.save(artefact1);
        artefactId1 = savedArtefact.getArtefactId();

        Artefact artefact2 = new Artefact();
        artefact2.setLocationId(LOCATION_ID);
        artefact2.setListType(ListType.FAMILY_DAILY_CAUSE_LIST);
        artefact2.setIsArchived(false);
        setCommonArtefactProperties(artefact2);

        savedArtefact = artefactRepository.save(artefact2);
        artefactId2 = savedArtefact.getArtefactId();

        Artefact artefact3 = new Artefact();
        artefact3.setLocationId(LOCATION_ID);
        artefact3.setListType(ListType.SJP_PUBLIC_LIST);
        artefact3.setIsArchived(false);
        setCommonArtefactProperties(artefact3);

        savedArtefact = artefactRepository.save(artefact3);
        artefactId3 = savedArtefact.getArtefactId();

        Artefact artefact4 = new Artefact();
        artefact4.setLocationId(LOCATION_ID);
        artefact4.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        artefact4.setIsArchived(true);
        setCommonArtefactProperties(artefact4);

        savedArtefact = artefactRepository.save(artefact4);
        artefactId4 = savedArtefact.getArtefactId();

        Artefact artefact5 = new Artefact();
        artefact5.setLocationId(NO_MATCH_LOCATION_ID);
        artefact5.setListType(ListType.SJP_PUBLIC_LIST);
        artefact5.setIsArchived(false);
        setCommonArtefactProperties(artefact5);

        savedArtefact = artefactRepository.save(artefact5);
        artefactId5 = savedArtefact.getArtefactId();
    }

    @AfterAll
    void shutdown() {
        artefactRepository.deleteAll();
    }

    private void setCommonArtefactProperties(Artefact artefact) {
        artefact.setSourceArtefactId(SOURCE_ARTEFACT_ID);
        artefact.setContentDate(TODAY);
        artefact.setLanguage(Language.ENGLISH);
        artefact.setProvenance(PROVENANCE);
        artefact.setDisplayFrom(YESTERDAY);
        artefact.setDisplayTo(TOMORROW);
        artefact.setSensitivity(Sensitivity.PUBLIC);
        artefact.setSupersededCount(1);
    }

    @Test
    void shouldFindArtefactByUpdateLogic() {
        assertThat(artefactRepository.findArtefactByUpdateLogic(LOCATION_ID, TODAY, Language.ENGLISH,
                                                                ListType.FAMILY_DAILY_CAUSE_LIST, PROVENANCE))
            .as(ARTEFACT_MATCHED_MESSAGE)
            .isPresent()
            .hasValueSatisfying(a -> a.getArtefactId().equals(artefactId2));
    }

    @Test
    void shouldNotFindArtefactByUpdateLogicIfNotAllValuesMatched() {
        assertThat(artefactRepository.findArtefactByUpdateLogic(LOCATION_ID, TODAY, Language.WELSH,
                                                                ListType.FAMILY_DAILY_CAUSE_LIST, PROVENANCE))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldFindByArtefactId() {
        assertThat(artefactRepository.findByArtefactId(artefactId3.toString(), TODAY))
            .as(ARTEFACT_MATCHED_MESSAGE)
            .isPresent()
            .hasValueSatisfying(a -> a.getArtefactId().equals(artefactId3));
    }

    @Test
    void shouldNotFindByArtefactIdIfCurrentDateNotAfterDisplayFromDate() {
        assertThat(artefactRepository.findByArtefactId(artefactId3.toString(), YESTERDAY))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldFindArtefactsByLocationId() {
        assertThat(artefactRepository.findArtefactsByLocationId(LOCATION_ID, TODAY))
            .as(ARTEFACT_MATCHED_MESSAGE)
            .hasSize(3)
            .extracting(Artefact::getArtefactId)
            .containsExactlyInAnyOrder(artefactId1, artefactId2, artefactId3);
    }

    @Test
    void shouldNotFindArtefactsByLocationIdIfCurrentDateNotBeforeDisplayToDate() {
        assertThat(artefactRepository.findArtefactsByLocationId(INVALID_LOCATION_ID, TOMORROW))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldCountArtefactsByLocationExcludingUnmatchedLocationAndArchivedArtefacts() {
        List<Object[]> results = artefactRepository.countArtefactsByLocation();
        assertThat(results)
            .as(RESULT_MATCHED_MESSAGE)
            .hasSize(1);

        assertThat(results.get(0)[0])
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo(LOCATION_ID);

        assertThat(results.get(0)[1])
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo(3L);
    }

    @Test
    void shouldFindArtefactsByLocationIdAdmin() {
        assertThat(artefactRepository.findArtefactsByLocationIdAdmin(LOCATION_ID))
            .as(ARTEFACT_MATCHED_MESSAGE)
            .hasSize(3)
            .extracting(Artefact::getArtefactId)
            .containsExactlyInAnyOrder(artefactId1, artefactId2, artefactId3);
    }

    @Test
    void shouldNotFindArtefactsByLocationIdAdminIfInvalid() {
        assertThat(artefactRepository.findArtefactsByLocationIdAdmin(INVALID_LOCATION_ID))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldFindArtefactByArtefactId() {
        assertThat(artefactRepository.findArtefactByArtefactId(artefactId4.toString()))
            .as(ARTEFACT_MATCHED_MESSAGE)
            .isPresent()
            .hasValueSatisfying(a -> a.getArtefactId().equals(4));
    }

    @Test
    void shouldNotFindArtefactByInvalidArtefactId() {
        assertThat(artefactRepository.findArtefactByArtefactId(INVALID_ARTEFACT_ID))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldFindArtefactsByDisplayFrom() {
        assertThat(artefactRepository.findArtefactsByDisplayFrom(YESTERDAY.toLocalDate()))
            .as(ARTEFACT_MATCHED_MESSAGE)
            .hasSize(4)
            .extracting(Artefact::getArtefactId)
            .containsExactlyInAnyOrder(artefactId1, artefactId2, artefactId3, artefactId5);
    }

    @Test
    void shouldNotFindArtefactsByDisplayFrom() {
        assertThat(artefactRepository.findArtefactsByDisplayFrom(TODAY.toLocalDate()))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldFindOutdatedArtefacts() {
        assertThat(artefactRepository.findOutdatedArtefacts(LocalDateTime.now().plusDays(2)))
            .as(ARTEFACT_MATCHED_MESSAGE)
            .hasSize(4)
            .extracting(Artefact::getArtefactId)
            .containsExactlyInAnyOrder(artefactId1, artefactId2, artefactId3, artefactId5);
    }

    @Test
    void shouldNotFindOutdatedArtefacts() {
        assertThat(artefactRepository.findOutdatedArtefacts(TOMORROW))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldFindAllNoMatchArtefacts() {
        assertThat(artefactRepository.findAllNoMatchArtefacts())
            .as(ARTEFACT_MATCHED_MESSAGE)
            .hasSize(1)
            .extracting(Artefact::getArtefactId)
            .containsExactly(artefactId5);
    }

    @Test
    void shouldCountNoMatchArtefacts() {
        assertThat(artefactRepository.countNoMatchArtefacts())
            .as(ARTEFACT_MATCHED_MESSAGE)
            .isEqualTo(1);
    }

    @Test
    void shouldFindActiveArtefactsForLocation() {
        assertThat(artefactRepository.findActiveArtefactsForLocation(TOMORROW, LOCATION_ID))
            .as(ARTEFACT_MATCHED_MESSAGE)
            .hasSize(3)
            .extracting(Artefact::getArtefactId)
            .containsExactlyInAnyOrder(artefactId1, artefactId2, artefactId3);
    }

    @Test
    void shouldNotFindActiveArtefactsForLocation() {
        assertThat(artefactRepository.findActiveArtefactsForLocation(LocalDateTime.now().plusDays(2),
                                                                     INVALID_LOCATION_ID))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldArchiveArtefact() {
        Artefact artefact = new Artefact();
        artefact.setPayload("Test payload");
        artefact.setSourceArtefactId("123");
        artefact.setSearch(Map.of("1", List.of("Test")));
        artefact.setIsArchived(false);
        Artefact savedArtefact = artefactRepository.save(artefact);

        artefactRepository.archiveArtefact(savedArtefact.getArtefactId().toString());

        Optional<Artefact> updatedArtefact = artefactRepository.findArtefactByArtefactId(
            savedArtefact.getArtefactId().toString()
        );

        assertThat(updatedArtefact)
            .as(ARTEFACT_MATCHED_MESSAGE)
            .isPresent()
            .hasValueSatisfying(a -> a.getPayload().isEmpty())
            .hasValueSatisfying(a -> a.getSourceArtefactId().isEmpty())
            .hasValueSatisfying(a -> a.getSearch().isEmpty())
            .hasValueSatisfying(Artefact::getIsArchived);
    }

    @Test
    void shouldRetrieveArtefactsForMiData() {
        List<PublicationMiData> miDataList = artefactRepository.getMiData();

        assertThat(miDataList).hasSize(5).extracting(PublicationMiData::getArtefactId)
            .containsExactlyInAnyOrder(artefactId1, artefactId2, artefactId3, artefactId4, artefactId5);

        PublicationMiData miData = miDataList.get(0);
        assertThat(miData.getSourceArtefactId()).isEqualTo(SOURCE_ARTEFACT_ID);
        assertThat(miData.getLocationId()).isEqualTo(LOCATION_ID);
        assertThat(miData.getListType()).isEqualTo(ListType.CIVIL_DAILY_CAUSE_LIST);
        assertThat(miData.getLanguage()).isEqualTo(Language.ENGLISH);
        assertThat(miData.getProvenance()).isEqualTo(PROVENANCE);
        assertThat(miData.getDisplayFrom()).isEqualTo(YESTERDAY);
        assertThat(miData.getDisplayTo()).isEqualTo(TOMORROW);
        assertThat(miData.getContentDate()).isEqualTo(TODAY);
        assertThat(miData.getSensitivity()).isEqualTo(Sensitivity.PUBLIC);
        assertThat(miData.getSupersededCount()).isEqualTo(1);
    }
}
