package uk.gov.hmcts.reform.pip.data.management.database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactArchived;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-jpa")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArtefactArchivedRepositoryTest {
    private static final LocalDateTime TODAY = LocalDateTime.of(2025, 2, 5, 1, 1, 2);
    private static final LocalDateTime TOMORROW = LocalDateTime.of(2025, 2, 6, 1, 1, 2);
    private static final LocalDateTime YESTERDAY = LocalDateTime.of(2025, 2, 4, 1, 1, 2);
    private static final String LOCATION_ID = "1";
    private static final String PROVENANCE = "MANUAL_UPLOAD";

    private UUID artefactId1;

    @Autowired
    ArtefactArchivedRepository artefactArchivedRepository;

    @BeforeAll
    void setup() {
        LocalDateTime publicationReceivedDateTime = LocalDateTime.now();

        ArtefactArchived artefact1 = new ArtefactArchived();
        artefact1.setArtefactId(UUID.randomUUID());
        artefact1.setLocationId(LOCATION_ID);
        artefact1.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        artefact1.setDisplayTo(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusDays(60));
        artefact1.setLastReceivedDate(publicationReceivedDateTime.minusDays(5));
        artefact1.setContentDate(TODAY);
        artefact1.setLanguage(Language.ENGLISH);
        artefact1.setProvenance(PROVENANCE);
        artefact1.setDisplayFrom(YESTERDAY);
        artefact1.setDisplayTo(TOMORROW);
        artefact1.setSensitivity(Sensitivity.PUBLIC);
        artefact1.setSupersededCount(1);

        ArtefactArchived savedArtefactArchived =
            artefactArchivedRepository.save(artefact1);
        artefactId1 = savedArtefactArchived.getArtefactId();

        ArtefactArchived artefact2 = new ArtefactArchived();
        artefact2.setArtefactId(UUID.randomUUID());
        artefact2.setLocationId(LOCATION_ID);
        artefact2.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        artefact2.setDisplayTo(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusDays(60));
        artefact2.setLastReceivedDate(publicationReceivedDateTime.minusDays(40));
        artefact2.setContentDate(TODAY);
        artefact2.setLanguage(Language.ENGLISH);
        artefact2.setProvenance(PROVENANCE);
        artefact2.setDisplayFrom(YESTERDAY);
        artefact2.setDisplayTo(TOMORROW);
        artefact2.setSensitivity(Sensitivity.PUBLIC);
        artefact2.setSupersededCount(1);

        artefactArchivedRepository.save(artefact2);
    }

    @AfterAll
    void shutdown() {
        artefactArchivedRepository.deleteAll();
    }


    @Test
    void shouldRetrieveArtefactsForMiData() {
        LocalDateTime publicationReceivedDate = LocalDate.now()
            .minusDays(31)
            .atStartOfDay();
        List<PublicationMiData> miDataList = artefactArchivedRepository.getArchivedArtefacts(publicationReceivedDate);

        //artefactId2 has last lastReceivedDate more than 31 days old, so it will not be picked.
        assertThat(miDataList).hasSize(1).extracting(PublicationMiData::getArtefactId)
            .containsExactlyInAnyOrder(artefactId1);

        PublicationMiData miData = miDataList.get(0);
        assertThat(miData.getSourceArtefactId()).isEmpty();
        assertThat(miData.getLocationId()).isEqualTo(LOCATION_ID);
        assertThat(miData.getListType()).isEqualTo(ListType.CIVIL_DAILY_CAUSE_LIST);
        assertThat(miData.getLanguage()).isEqualTo(Language.ENGLISH);
        assertThat(miData.getProvenance()).isEqualTo(PROVENANCE);
        assertThat(miData.getDisplayFrom()).isEqualTo(YESTERDAY);
        assertThat(miData.getDisplayTo()).isEqualTo(TOMORROW);
        assertThat(miData.getContentDate()).isEqualTo(TODAY);
        assertThat(miData.getSensitivity()).isEqualTo(Sensitivity.PUBLIC);
        assertThat(miData.getSupersededCount()).isEqualTo(1);


        //none of the publication has lastReceivedDate one day old. So it will return empty
        publicationReceivedDate = LocalDate.now()
            .minusDays(1)
            .atStartOfDay();
        miDataList = artefactArchivedRepository.getArchivedArtefacts(publicationReceivedDate);

        assertThat(miDataList).hasSize(0);
    }
}
