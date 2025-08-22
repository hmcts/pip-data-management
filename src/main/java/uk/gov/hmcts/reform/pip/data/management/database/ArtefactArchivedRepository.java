package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactArchived;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArtefactArchivedRepository extends JpaRepository<ArtefactArchived, Long> {

    @Query("SELECT new uk.gov.hmcts.reform.pip.model.report.PublicationMiData("
        + "artefactId, displayFrom, displayTo, language, "
        + "provenance, sensitivity, '' as sourceArtefactId, "
        + "supersededCount, type, contentDate, locationId, listType) "
        + "FROM ArtefactArchived "
        + "WHERE lastReceivedDate >= :publicationReceivedDate")
    List<PublicationMiData> getArchivedMiData(@Param("publicationReceivedDate") LocalDateTime date);
}
