package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

import java.time.LocalDate;
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
    String LOCATION_ID_PARAM = "location_id";
    String CASE_NAME_PARAM = "caseName";
    String CONTENT_DATE_PARAM = "content_date";
    String LANGUAGE_PARAM = "language";
    String LIST_TYPE_PARAM = "list_type";
    String PROVENANCE_PARAM = "provenance";

    @Query(value = "SELECT * FROM Artefact WHERE location_id = :location_id AND content_date = :content_date AND "
        + "language = :language AND list_type = :list_type AND provenance = :provenance", nativeQuery = true)
    Optional<Artefact> findArtefactByUpdateLogic(@Param(LOCATION_ID_PARAM) String locationId,
                                                 @Param(CONTENT_DATE_PARAM) LocalDateTime contentDate,
                                                 @Param(LANGUAGE_PARAM) String language,
                                                 @Param(LIST_TYPE_PARAM) String listType,
                                                 @Param(PROVENANCE_PARAM) String provenance);

    @Query(value = "select * from Artefact where artefact_id = CAST(:artefact_id AS uuid) and display_from < "
        + ":curr_date and (display_to> :curr_date or display_to is null)",
        nativeQuery = true)
    Optional<Artefact> findByArtefactId(@Param(ARTEFACT_ID_PARAM) String artefactId,
                                                @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);

    @Query(value = "select * from Artefact where location_id = :location_id and display_from < "
        + ":curr_date and (display_to> :curr_date or display_to is null)",
        nativeQuery = true)
    List<Artefact> findArtefactsByLocationId(@Param(LOCATION_ID_PARAM) String locationId,
                                             @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);

    @Query(value = INITIAL_SELECT + "WHERE LOWER(searchDetails.caseDetails ->> 'caseName') LIKE LOWER"
        + "('%' || :caseName || '%') and display_from < :curr_date and (display_to > :curr_date or display_to is null)",
        nativeQuery = true)
    List<Artefact> findArtefactByCaseName(@Param(CASE_NAME_PARAM) String caseName,
                                                  @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);


    @Query(value = INITIAL_SELECT + "WHERE searchDetails.caseDetails ->> :searchTerm = :searchValue and "
        + "display_from < :curr_date and (display_to > :curr_date or display_to is null)", nativeQuery = true)
    List<Artefact> findArtefactBySearch(@Param(SEARCH_TERM_PARAM) String searchTerm,
                                                @Param(SEARCH_VAL_PARAM) String searchVal,
                                                @Param(CURRENT_DATE_PARAM) LocalDateTime currentDate);

    @Query(value = "select * from Artefact where location_id = :location_id",
        nativeQuery = true)
    List<Artefact> findArtefactsByLocationIdAdmin(@Param(LOCATION_ID_PARAM) String locationId);

    @Query(value = "select * from Artefact where artefact_id = CAST(:artefact_id AS uuid)", nativeQuery = true)
    Optional<Artefact> findArtefactByArtefactId(@Param(ARTEFACT_ID_PARAM) String artefactId);

    @Query(value = "SELECT * FROM Artefact WHERE DATE(display_from) = :curr_date", nativeQuery = true)
    List<Artefact> findArtefactsByDisplayFrom(@Param(CURRENT_DATE_PARAM) LocalDate today);

    @Query(value = "SELECT * FROM Artefact WHERE expiry_date < :curr_date", nativeQuery = true)
    List<Artefact> findOutdatedArtefacts(@Param(CURRENT_DATE_PARAM) LocalDateTime today);

    @Query(value = "SELECT * FROM Artefact WHERE location_id LIKE '%NoMatch%'", nativeQuery = true)
    List<Artefact> findAllNoMatchArtefacts();

    @Query(value = "SELECT cast(artefact_id as text), display_from, display_to, language, "
        + "provenance, sensitivity, source_artefact_id, type, content_date, location_id, list_type "
        + "FROM artefact",
        nativeQuery = true)
    List<String> getMiData();

    @Query(value = "SELECT * FROM Artefact "
        + "WHERE expiry_date >= :curr_date "
        + "and location_id = :location_id", nativeQuery = true)
    List<Artefact> findActiveArtefactsForLocation(@Param(CURRENT_DATE_PARAM) LocalDateTime today,
                                                  @Param(LOCATION_ID_PARAM) String locationId);
}
