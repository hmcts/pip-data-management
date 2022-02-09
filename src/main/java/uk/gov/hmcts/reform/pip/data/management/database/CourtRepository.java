package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.court.Court;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourtRepository extends JpaRepository<Court, Integer> {

    Optional<Court> getCourtByCourtId(Integer courtId);

    Optional<Court> getCourtByName(String courtName);

    @Query(value = "select * from court "
        + "WHERE (:regions = '' OR court.region = ANY(string_to_array(:regions, ','))) "
        + "AND (:jurisdictions = '' OR court.jurisdiction && string_to_array(:jurisdictions, ',')) "
        + "ORDER BY court.name",
        nativeQuery = true)
    List<Court> findByRegionAndJurisdictionOrderByName(@Param("regions") String regions,
                                                       @Param("jurisdictions") String jurisdictions);

}
