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

    String INITIAL_SELECT =
         "SELECT DISTINCT on (artefact.artefact_id) artefact.* FROM ARTEFACT INNER JOIN (SELECT "
         + "artefact_id, json_array_elements(search -> 'cases') caseDetails FROM artefact) searchDetails ON artefact"
         + ".artefact_id = searchDetails.artefact_id ";

    String SEARCH_TERM_PARAM = "searchTerm";
    String ARTEFACT_ID_PARAM = "artefact_id";
    String CURRENT_DATE_PARAM = "curr_date";
    String SEARCH_VAL_PARAM = "searchValue";
    String COURT_ID_PARAM = "courtId";
    String CASE_NAME_PARAM = "caseName";

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

    @Query(value = "select * from Artefact where court_id = :courtId and display_from < "
        + ":curr_date and (display_to> :curr_date or display_to is null)",
        nativeQuery = true)
    List<Artefact> findArtefactsByCourtIdVerified(@Param(COURT_ID_PARAM) String courtId,
                                                  @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);

    @Query(value = "select * from Artefact where court_id = :courtId and sensitivity = 'PUBLIC' "
        + "and display_from < :curr_date and (display_to> :curr_date or display_to is null)",
        nativeQuery = true)
    List<Artefact> findArtefactsByCourtIdUnverified(@Param(COURT_ID_PARAM) String courtId,
                                                    @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);

    @Query(value = INITIAL_SELECT + "WHERE LOWER(searchDetails.caseDetails ->> 'caseName') LIKE LOWER"
        + "('%' || :caseName || '%') and display_from < :curr_date and (display_to > :curr_date or display_to is null)",
        nativeQuery = true)
    List<Artefact> findArtefactByCaseNameVerified(@Param(CASE_NAME_PARAM) String caseName,
                                                  @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);

    @Query(value = INITIAL_SELECT + "WHERE LOWER(searchDetails.caseDetails ->> 'caseName') LIKE "
        + "LOWER('%' || :caseName || '%') and display_from < :curr_date and (display_to > :curr_date or display_to "
        + "is null) and sensitivity = 'PUBLIC'", nativeQuery = true)
    List<Artefact> findArtefactByCaseNameUnverified(@Param(CASE_NAME_PARAM) String caseName,
                                                    @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);


    @Query(value = INITIAL_SELECT + "WHERE searchDetails.caseDetails ->> :searchTerm = :searchValue and "
        + "display_from < :curr_date and (display_to > :curr_date or display_to is null)", nativeQuery = true)
    List<Artefact> findArtefactBySearchVerified(@Param(SEARCH_TERM_PARAM) String searchTerm,
                                                @Param(SEARCH_VAL_PARAM) String searchVal,
                                                @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);

    @Query(value = INITIAL_SELECT + "WHERE searchDetails.caseDetails ->> :searchTerm = :searchValue "
        + "and display_from < :curr_date and (display_to > :curr_date or display_to is null) and sensitivity = "
        + "'PUBLIC'",
        nativeQuery = true)
    List<Artefact> findArtefactBySearchUnverified(@Param(SEARCH_TERM_PARAM) String searchTerm,
                                                  @Param(SEARCH_VAL_PARAM) String searchVal,
                                                  @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);

    @Query(value = "select * from Artefact where court_id = :courtId",
        nativeQuery = true)
    List<Artefact> findArtefactsByCourtIdAdmin(@Param(COURT_ID_PARAM) String courtId);

}
