package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;

import java.util.Optional;

@Repository
public interface LocationMetadataRepository extends JpaRepository<LocationMetadata, Long>  {
    Optional<LocationMetadata> findByLocationId(Integer locationId);
}
