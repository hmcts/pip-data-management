package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArtefactRepository extends JpaRepository<Artefact, Long> {

    String SEARCH_VAL = "searchVal";
    String CURRENT_DATE = "curr_date";

    Optional<Artefact> findBySourceArtefactIdAndProvenance(String sourceArtefactId, String provenance);

    Optional<Artefact> findByArtefactId(UUID artefactId);

    @Query(value = "select * from Artefact where court_id = :searchVal and display_from < "
        + ":curr_date and (display_to> :curr_date or display_to is null)",
        nativeQuery = true)
    List<Artefact> findArtefactsByCourtIdVerified(@Param(SEARCH_VAL) String searchVal,
                                                  @Param(CURRENT_DATE) LocalDateTime currentDate);

    @Query(value = "select * from Artefact where court_id = :searchVal and sensitivity = 'PUBLIC' "
        + "and display_from < :curr_date and (display_to> :curr_date or display_to is null)",
        nativeQuery = true)
    List<Artefact> findArtefactsByCourtIdUnverified(@Param(SEARCH_VAL) String searchVal,
                                                    @Param(CURRENT_DATE) LocalDateTime currentDate);

    @Query(value = "SELECT * FROM Artefact where jsonb_exists(CAST(search -> :searchTerm as jsonb), :searchVal) and "
        + "display_from < :curr_date and (display_to > :curr_date or display_to is null)", nativeQuery = true)
    List<Artefact> findArtefactBySearchVerified(@Param("searchTerm") String searchTerm,
                                                @Param(SEARCH_VAL) String searchVal,
                                                @Param(CURRENT_DATE) LocalDateTime currentDate);

    @Query(value = "SELECT * FROM Artefact where jsonb_exists(CAST(search -> :searchTerm as jsonb), :searchVal) and "
        + "display_from < :curr_date and (display_to > :curr_date or display_to is null) and sensitivity = 'PUBLIC'",
        nativeQuery = true)
    List<Artefact> findArtefactBySearchUnverified(@Param("searchTerm") String searchTerm,
                                                @Param(SEARCH_VAL) String searchVal,
                                                @Param(CURRENT_DATE) LocalDateTime currentDate);

    @Query(value = "SELECT * FROM Artefact WHERE LOWER(search ->> 'case-name') LIKE LOWER('%' || :searchVal || '%') "
        + "and display_from < :curr_date and (display_to > :curr_date or display_to is null)", nativeQuery = true)
    List<Artefact> findArtefactByCaseNameVerified(@Param(SEARCH_VAL) String searchVal,
                                          @Param(CURRENT_DATE) LocalDateTime currentDate);

    @Query(value = "SELECT * FROM Artefact WHERE LOWER(search ->> 'case-name') LIKE LOWER('%' || :searchVal || '%') "
        + "and display_from < :curr_date and (display_to > :curr_date or display_to is null) and sensitivity = "
        + "'PUBLIC'", nativeQuery = true)
    List<Artefact> findArtefactByCaseNameUnverified(@Param(SEARCH_VAL) String searchVal,
                                                  @Param(CURRENT_DATE) LocalDateTime currentDate);
}
