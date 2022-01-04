package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.util.Optional;

@Repository
public interface ArtefactRepository extends JpaRepository<Artefact, Long> {
    Optional<Artefact> findBySourceArtefactIdAndProvenance(String sourceArtefactId, String provenance);

}
