package uk.gov.hmcts.reform.pip.data.management.service.artefact;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ArtefactService {

    private final ArtefactRepository artefactRepository;
    private final AccountManagementService accountManagementService;
    private final AzureBlobService azureBlobService;

    @Autowired
    public ArtefactService(ArtefactRepository artefactRepository,
                           AccountManagementService accountManagementService, AzureBlobService azureBlobService) {
        this.artefactRepository = artefactRepository;
        this.accountManagementService = accountManagementService;
        this.azureBlobService = azureBlobService;
    }

    public Artefact getMetadataByArtefactId(UUID artefactId) {
        return artefactRepository.findArtefactByArtefactId(artefactId.toString())
            .orElseThrow(() -> new ArtefactNotFoundException(String.format(
                "No artefact found with the ID: %s",
                artefactId
            )));
    }

    /**
     * Takes in artefact id and returns the metadata for the artefact.
     *
     * @param artefactId represents the artefact id which is then used to get an artefact to populate the inputs
     *                   for the blob request.
     * @param userId     represents the user ID of the user who is making the request
     * @return The metadata for the found artefact.
     */
    public Artefact getMetadataByArtefactId(UUID artefactId, UUID userId) {

        LocalDateTime currentDate = LocalDateTime.now();

        Optional<Artefact> artefact = artefactRepository.findByArtefactId(
            artefactId.toString(),
            currentDate
        );

        if (artefact.isPresent() && isAuthorised(artefact.get(), userId)) {
            return artefact.get();
        }

        throw new ArtefactNotFoundException(String.format("No artefact found with the ID: %s", artefactId));
    }

    /**
     * Takes in artefact id and returns the payload within the matching blob in string format.
     *
     * @param artefactId represents the artefact id which is then used to get an artefact to populate the inputs
     *                   for the blob request.
     * @param userId     represents the user ID of the user who is making the request
     * @return The data within the blob in string format.
     */
    public String getPayloadByArtefactId(UUID artefactId, UUID userId) {
        Artefact artefact = getMetadataByArtefactId(artefactId, userId);

        return azureBlobService.getBlobData(ArtefactHelper.getUuidFromUrl(artefact.getPayload()));
    }

    /**
     * Takes in artefact id and returns the payload within the matching blob in string format. This is used for admin
     * requests
     *
     * @param artefactId represents the artefact id which is then used to get an artefact to populate the inputs
     *                   for the blob request.
     * @return The data within the blob in string format.
     */
    public String getPayloadByArtefactId(UUID artefactId) {
        Artefact artefact = getMetadataByArtefactId(artefactId);

        return azureBlobService.getBlobData(ArtefactHelper.getUuidFromUrl(artefact.getPayload()));
    }

    /**
     * Retrieves a flat file for an artefact.
     *
     * @param artefactId The artefact ID to retrieve the flat file from.
     * @param userId     represents the user ID of the user who is making the request
     * @return The flat file resource.
     */
    public Resource getFlatFileByArtefactID(UUID artefactId, UUID userId) {
        Artefact artefact = getMetadataByArtefactId(artefactId, userId);

        return azureBlobService.getBlobFile(ArtefactHelper.getUuidFromUrl(artefact.getPayload()));
    }

    /**
     * Retrieves a flat file for an artefact. This is used for admin requests
     *
     * @param artefactId The artefact ID to retrieve the flat file from.
     * @return The flat file resource.
     */
    public Resource getFlatFileByArtefactID(UUID artefactId) {
        Artefact artefact = getMetadataByArtefactId(artefactId);

        return azureBlobService.getBlobFile(ArtefactHelper.getUuidFromUrl(artefact.getPayload()));
    }

    public boolean isAuthorised(Artefact artefact, UUID userId) {
        if (artefact.getSensitivity().equals(Sensitivity.PUBLIC)) {
            return true;
        } else if (userId == null) {
            return false;
        } else {
            return accountManagementService.getIsAuthorised(userId, artefact.getListType(), artefact.getSensitivity());
        }
    }

    /**
     * Returns what is essentially a CSV file with the count of artefacts in a given location.
     *
     * @return string representing the csv file.
     */
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

    public List<Artefact> findAllNoMatchArtefacts() {
        return artefactRepository.findAllNoMatchArtefacts();
    }

    public LocationType getLocationType(ListType listType) {
        return listType.getListLocationLevel();
    }

}
