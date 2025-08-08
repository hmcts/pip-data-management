package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactArchived;

@Repository
public interface ArtefactArchivedRepository extends JpaRepository<ArtefactArchived, Long> {
}
