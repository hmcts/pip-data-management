package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtefactRepository extends JpaRepository<Artefact, Long> {

    String ARTEFACT_ID_PARAM = "artefact_id";
    String CURRENT_DATE_PARAM = "curr_date";
    String SEARCH_VAL_PARAM = "searchVal";

    Optional<Artefact> findBySourceArtefactIdAndProvenance(String sourceArtefactId, String provenance);

    @Query(value = "select * from Artefact where artefact_id = CAST(:artefact_id AS uuid) and display_from < "
        + ":curr_date and (display_to> :curr_date or display_to is null)",
        nativeQuery = true)
    Optional<Artefact> findByArtefactIdVerified(@Param(ARTEFACT_ID_PARAM) String artefactId,
                                                @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);

    @Query(value = "select * from Artefact where artefact_id = CAST(:artefact_id AS uuid) and sensitivity = 'PUBLIC' "
        + "and display_from < :curr_date and (display_to> :curr_date or display_to is null)",
        nativeQuery = true)
    Optional<Artefact> findByArtefactIdUnverified(@Param(ARTEFACT_ID_PARAM) String artefactId,
                                                  @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);


    @Query(value = "select * from Artefact where court_id = :searchVal and display_from < "
        + ":curr_date and (display_to> :curr_date or display_to is null)",
        nativeQuery = true)
    List<Artefact> findArtefactsBySearchVerified(@Param(SEARCH_VAL_PARAM) String searchVal,
                                                 @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);

    @Query(value = "select * from Artefact where court_id = :searchVal and sensitivity = 'PUBLIC' "
        + "and display_from < :curr_date and (display_to> :curr_date or display_to is null)",
        nativeQuery = true)
    List<Artefact> findArtefactsBySearchUnverified(@Param(SEARCH_VAL_PARAM) String searchVal,
                                                   @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);
}
