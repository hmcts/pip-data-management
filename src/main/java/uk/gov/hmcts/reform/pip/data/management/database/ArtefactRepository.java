package uk.gov.hmcts.reform.pip.data.management.database;

import org.apache.tomcat.jni.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtefactRepository extends JpaRepository<Artefact, Long> {
    Optional<Artefact> findBySourceArtefactIdAndProvenance(String sourceArtefactId, String provenance);

    List<Artefact> findArtefactsByArtefactIdIsNotNull();

    List<Artefact> findArtefactsByDisplayFromBeforeAndDisplayToAfter(LocalDateTime currentDate,
                                                                     LocalDateTime currentDate1);

    List<Artefact> findArtefactsByDisplayFromBeforeAndDisplayToAfterAndSensitivityEquals(LocalDateTime currentDate,
                                                                                         LocalDateTime currentDate1,
                                                                                         Sensitivity sensitivity);

}
