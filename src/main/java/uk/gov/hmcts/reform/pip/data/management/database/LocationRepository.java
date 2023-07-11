package uk.gov.hmcts.reform.pip.data.management.database;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {

    Optional<Location> getLocationByLocationId(Integer locationId);

    Optional<Location> getLocationByName(String locationName);

    Optional<Location> getLocationByWelshName(String locationName);

    List<Location> findAllByNameStartingWithIgnoreCase(String prefix);

    void deleteByLocationIdIn(List<Integer> locationId);

    @Query(value = "select * from location "
        + "WHERE (:regions = '' OR location.region && string_to_array(:regions, ',')) "
        + "AND (:jurisdictions = '' OR location.jurisdiction && string_to_array(:jurisdictions, ',')) "
        + "ORDER BY location.name",
        nativeQuery = true)
    List<Location> findByRegionAndJurisdictionOrderByName(@Param("regions") String regions,
                                                          @Param("jurisdictions") String jurisdictions);

    @Query(value = "select * from location "
        + "WHERE (:regions = '' OR location.welsh_region && string_to_array(:regions, ',')) "
        + "AND (:jurisdictions = '' OR location.welsh_jurisdiction && string_to_array(:jurisdictions, ',')) "
        + "ORDER BY location.name",
        nativeQuery = true)
    List<Location> findByWelshRegionAndJurisdictionOrderByName(@Param("regions") String regions,
                                                          @Param("jurisdictions") String jurisdictions);

    @Query(value = "select location.* from location_reference "
        + "inner join location on location_reference.location_id = location.location_id "
        + "WHERE (location_reference.provenance = :provenance) "
        + "AND (location_reference.provenance_location_id = :provenanceLocationId) "
        + "AND (location_reference.provenance_location_type = :provenanceLocationType)",
        nativeQuery = true)
    Optional<Location> findByLocationIdByProvenance(@Param("provenance") String provenance,
                                                    @Param("provenanceLocationId") String provenanceLocationId,
                                                    @Param("provenanceLocationType") String provenanceLocationType);

    @Modifying
    @Transactional
    @Query(value = "REFRESH MATERIALIZED VIEW sdp_mat_view_location", nativeQuery = true)
    void refreshLocationView();

}
