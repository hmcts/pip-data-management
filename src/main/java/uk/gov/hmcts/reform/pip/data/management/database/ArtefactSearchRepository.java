package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface ArtefactSearchRepository extends JpaRepository<ArtefactSearch, UUID> {

    Optional<List<ArtefactSearch>> findByArtefactId(UUID artefactId);

    void deleteByArtefactId(UUID artefactId);

}

