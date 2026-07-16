package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ArtefactSearchRepository extends JpaRepository<ArtefactSearch, UUID> {

    List<ArtefactSearch> findByArtefactId(UUID artefactId);

    void deleteByArtefactId(UUID artefactId);

    @Query(value = "SELECT DISTINCT ars.* FROM artefact_search ars "
        + "INNER JOIN artefact a ON ars.artefact_id = a.artefact_id "
        + "WHERE LOWER(ars.case_number) = LOWER(:caseNumber) "
        + "AND a.display_from < :curr_date "
        + "AND (a.display_to > :curr_date OR a.display_to IS NULL)",
        nativeQuery = true)
    List<ArtefactSearch> findByCaseNumberIgnoreCase(@Param("caseNumber") String caseNumber,
                                                    @Param("curr_date") LocalDateTime currentDate);

    @Query(value = "SELECT DISTINCT ars.* FROM artefact_search ars "
        + "INNER JOIN artefact a ON ars.artefact_id = a.artefact_id "
        + "WHERE LOWER(ars.case_name) = LOWER(CONCAT(:caseName)) "
        + "AND a.display_from < :curr_date "
        + "AND (a.display_to > :curr_date OR a.display_to IS NULL)",
        nativeQuery = true)
    List<ArtefactSearch> findByCaseNameIgnoreCase(@Param("caseName") String caseName,
                                                                 @Param("curr_date") LocalDateTime currentDate);

    @Query(value = "SELECT DISTINCT ars.* FROM artefact_search ars "
        + "INNER JOIN artefact a ON ars.artefact_id = a.artefact_id "
        + "WHERE LOWER(ars.case_name) LIKE LOWER(CONCAT('%', :caseName, '%')) "
        + "AND a.display_from < :curr_date "
        + "AND (a.display_to > :curr_date OR a.display_to IS NULL) "
        + "LIMIT 50",
        nativeQuery = true)
    List<ArtefactSearch> findTop50ByCaseNameContainingIgnoreCase(@Param("caseName") String caseName,
                                                                 @Param("curr_date") LocalDateTime currentDate);
}
