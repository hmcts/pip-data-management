package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.data.management.service.location.LocationService;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Service
@Slf4j
public class PublicationLocationService {
    private final ArtefactRepository artefactRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final PublicationDeleteService publicationDeleteService;
    private final AccountManagementService accountManagementService;
    private final PublicationServicesService publicationServicesService;

    @Autowired
    public PublicationLocationService(ArtefactRepository artefactRepository, LocationRepository locationRepository,
                                      LocationService locationService, PublicationDeleteService artefactDeleteService,
                                      AccountManagementService accountManagementService,
                                      PublicationServicesService publicationServicesService) {
        this.artefactRepository = artefactRepository;
        this.locationRepository = locationRepository;
        this.locationService = locationService;
        this.publicationDeleteService = artefactDeleteService;
        this.accountManagementService = accountManagementService;
        this.publicationServicesService = publicationServicesService;
    }

    public List<LocationArtefact> countArtefactsByLocation() {
        List<LocationArtefact> artefactsPerLocations = new ArrayList<>();
        List<Object[]> returnedData = artefactRepository.countArtefactsByLocation();
        for (Object[] result : returnedData) {
            artefactsPerLocations.add(
                new LocationArtefact(result[0].toString(), Integer.parseInt(result[1].toString())));
        }
        artefactsPerLocations.add(new LocationArtefact("noMatch", artefactRepository.countNoMatchArtefacts()));
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

        activeArtefacts.forEach(artefact -> {
            publicationDeleteService.handleArtefactDeletion(artefact);
            log.info(writeLog(
                String.format("Artefact deleted by %s, with artefact id: %s",
                              userId, artefact.getArtefactId())
            ));
        });
        Optional<Location> location = locationRepository.getLocationByLocationId(locationId);
        notifySystemAdminAboutLocationPublicationDeletion(
            userId, String.format("Total %s artefact(s) for location %s", activeArtefacts.size(),
                                  location.isPresent() ? location.get().getName() : "")
        );
        return String.format("Total %s artefact deleted for location id %s", activeArtefacts.size(), locationId);
    }

    public String deleteAllArtefactsWithLocationNamePrefix(String prefix) {
        List<String> locationIds = locationService.getAllLocationsWithNamePrefix(prefix).stream()
            .map(Object::toString)
            .toList();

        List<Artefact> artefactsToDelete = Collections.emptyList();
        if (!locationIds.isEmpty()) {
            artefactsToDelete = artefactRepository.findAllByLocationIdIn(locationIds);
            artefactsToDelete.forEach(publicationDeleteService::handleArtefactDeletion);
        }
        return String.format("%s artefacts(s) deleted for location name starting with %s",
                             artefactsToDelete.size(), prefix);
    }

    private void notifySystemAdminAboutLocationPublicationDeletion(String userId, String additionalDetails)
        throws JsonProcessingException {
        PiUser userInfo = accountManagementService.getUserById(userId);
        List<String> systemAdminsAad = accountManagementService.getAllAccounts("PI_AAD", "SYSTEM_ADMIN");
        List<String> systemAdminsSso = accountManagementService.getAllAccounts("SSO", "SYSTEM_ADMIN");
        List<String> systemAdmins = Stream.concat(systemAdminsAad.stream(), systemAdminsSso.stream()).toList();
        publicationServicesService.sendSystemAdminEmail(systemAdmins, userInfo.getEmail(), ActionResult.SUCCEEDED,
                                                        additionalDetails, ChangeType.DELETE_LOCATION_ARTEFACT);
    }


}
