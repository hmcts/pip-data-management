package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.court.NewCourt;

import java.util.*;

@Repository
public interface CourtRepository extends JpaRepository<NewCourt, Long> {

    Optional<NewCourt> getNewCourtByCourtId(UUID courtId);

    Optional<NewCourt> getNewCourtByCourtName(String courtName);

    @Query(value = "select * from court INNER JOIN court_reference " +
        "ON court.court_id = court_reference.court_id " +
        "WHERE court.region IN :regions AND court.jurisdiction && string_to_array(:jurisdictions, ',') " +
        "ORDER BY court.court_name",
        nativeQuery = true)
    List<NewCourt> findByRegionAndJurisdictionOrderByName(@Param("regions") List<String> regions,
                                                          @Param("jurisdictions") String jurisdictions);
}
