package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactArchivedRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.location.LocationService;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Service
@Slf4j
public class PublicationLocationService {
    private final ArtefactRepository artefactRepository;
    private final LocationService locationService;
    private final PublicationRemovalService publicationRemovalService;
    private final ArtefactArchivedRepository artefactArchivedRepository;

    @Autowired
    public PublicationLocationService(ArtefactRepository artefactRepository, LocationService locationService,
                                      PublicationRemovalService publicationRemovalService,
                                      ArtefactArchivedRepository artefactArchivedRepository) {
        this.artefactRepository = artefactRepository;
        this.locationService = locationService;
        this.publicationRemovalService = publicationRemovalService;
        this.artefactArchivedRepository = artefactArchivedRepository;
    }

    public List<LocationArtefact> countArtefactsByLocation() {
        List<LocationArtefact> artefactsPerLocations = new ArrayList<>();
        LocalDateTime currDate = LocalDateTime.now();
        List<Object[]> returnedData = artefactRepository.countArtefactsByLocation(currDate);
        for (Object[] result : returnedData) {
            artefactsPerLocations.add(
                new LocationArtefact(result[0].toString(), Integer.parseInt(result[1].toString())));
        }
        artefactsPerLocations.add(
            new LocationArtefact("noMatch", artefactRepository.countNoMatchArtefacts(currDate)));
        return artefactsPerLocations;
    }

    public LocationType getLocationType(ListType listType) {
        return listType.getListLocationLevel();
    }

    public List<Artefact> findAllNoMatchArtefacts() {
        return artefactRepository.findAllNoMatchArtefacts();
    }

    public String deleteArtefactByLocation(Integer locationId, String userId)
        throws JsonProcessingException {
        List<Artefact> activeArtefacts = artefactRepository.findActiveArtefactsForLocation(
            LocalDateTime.now(),
            locationId.toString());
        if (activeArtefacts.isEmpty()) {
            log.info(writeLog(String.format("User %s attempting to delete all artefacts for location %s. "
                                                + "No artefacts found",
                                            userId, locationId)));
            throw new ArtefactNotFoundException(String.format(
                "No artefacts found with the location ID %s",
                locationId
            ));
        }
        log.info(writeLog(String.format("User %s attempting to delete all artefacts for location %s. "
                                            + "%s artefact(s) found",
                                        userId, locationId, activeArtefacts.size())));

        publicationRemovalService.deleteArtefactByLocation(activeArtefacts, locationId, userId);
        return String.format("Total %s artefact deleted for location id %s", activeArtefacts.size(), locationId);
    }

    public String deleteAllArtefactsWithLocationNamePrefix(String prefix) {
        List<String> locationIds = locationService.getAllLocationsWithNamePrefix(prefix).stream()
            .map(Object::toString)
            .toList();

        List<Artefact> artefactsToDelete = Collections.emptyList();
        if (!locationIds.isEmpty()) {
            artefactsToDelete = artefactRepository.findAllByLocationIdIn(locationIds);
            publicationRemovalService.deleteArtefacts(artefactsToDelete);
            artefactArchivedRepository.deleteAllByLocationIdIn(locationIds);
        }
        return String.format("%s artefacts(s) deleted for location name starting with %s",
                             artefactsToDelete.size(), prefix);
    }
}
