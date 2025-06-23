package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationMetadataRepository extends JpaRepository<LocationMetadata, UUID> {

    Optional<LocationMetadata> findByLocationId(Integer locationId);

    void deleteByLocationIdIn(List<Integer> locationId);
}
