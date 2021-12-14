package uk.gov.hmcts.reform.pip.data.management.database;

import org.apache.tomcat.jni.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ArtefactRepository extends JpaRepository<Artefact, Long> {
    Optional<Artefact> findBySourceArtefactIdAndProvenance(String sourceArtefactId, String provenance);

    List<Artefact> findArtefactsByArtefactIdIsNotNull();


    @Query(value = "select * from Artefact where search->'court-id'->>0 = :searchVal and display_from < :curr_date "
        + "and display_to> :curr_date",
    nativeQuery = true)
    List<Artefact> findArtefactsBySearchVerified(@Param("searchVal") String searchVal,
                                         @Param("curr_date") LocalDateTime current_date);

    @Query(value = "select * from Artefact where search->'court-id'->>0 = :searchVal and sensitivity = 'PUBLIC' and "
        + "display_from < :curr_date and display_to> :curr_date",
        nativeQuery = true)
    List<Artefact> findArtefactsBySearchUnverified(@Param("searchVal") String searchVal,
                                                 @Param("curr_date") LocalDateTime current_date);
}
