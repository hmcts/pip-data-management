package uk.gov.hmcts.reform.pip.data.management.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.LocationMetadataRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LocationNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;

@Service
public class LocationMetadataService {
    private final LocationMetadataRepository locationMetadataRepository;

    public LocationMetadataService(LocationMetadataRepository locationMetadataRepository) {
        this.locationMetadataRepository = locationMetadataRepository;
    }

    public LocationMetadata getLocationMetadataByLocationId(Integer locationId) {
        return locationMetadataRepository.findByLocationId(locationId)
            .orElseThrow(() -> new LocationNotFoundException(
                String.format("No location metadata found with location id: %s", locationId)
            ));
    }
}
