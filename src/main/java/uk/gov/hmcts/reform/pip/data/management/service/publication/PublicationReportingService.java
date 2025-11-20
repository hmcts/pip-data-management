package uk.gov.hmcts.reform.pip.data.management.service.publication;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactArchivedRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.helpers.NoMatchArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.NoMatchArtefact;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PublicationReportingService {
    private final ArtefactRepository artefactRepository;
    private final ArtefactArchivedRepository artefactArchivedRepository;
    private final LocationRepository locationRepository;
    private final PublicationServicesService publicationServicesService;

    @Autowired
    public PublicationReportingService(ArtefactRepository artefactRepository,
                                       ArtefactArchivedRepository artefactArchivedRepository,
                                       LocationRepository locationRepository,
                                       PublicationServicesService publicationServicesService) {
        this.artefactRepository = artefactRepository;
        this.artefactArchivedRepository = artefactArchivedRepository;
        this.locationRepository = locationRepository;
        this.publicationServicesService = publicationServicesService;
    }

    /**
     * Retrieve artefact data for MI reporting.
     * @return MI artefact data as a list of PublicationMiData objects
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public List<PublicationMiData> getMiData() {
        LocalDateTime publicationReceivedDate = LocalDate.now()
            .minusDays(31)
            .atStartOfDay();
        List<PublicationMiData> publicationMiData =
            artefactRepository.getMiData(publicationReceivedDate);
        List<PublicationMiData> archivedPublicationMiData =
            artefactArchivedRepository.getArchivedMiData(publicationReceivedDate);
        if (!archivedPublicationMiData.isEmpty()) {
            publicationMiData.addAll(archivedPublicationMiData);
        }

        Map<Integer, String> location = locationRepository.findAll()
            .stream().collect(Collectors.toMap(Location::getLocationId, Location::getName));

        for (PublicationMiData miData : publicationMiData) {
            if (NumberUtils.isParsable(miData.getLocationId())) {
                try {
                    miData.setLocationName(
                        location.getOrDefault(Integer.parseInt(miData.getLocationId()), null));
                } catch (NumberFormatException e) {
                    // To catch where location ID is a number, but not a valid integer
                }
            }
        }

        return publicationMiData;
    }

    /**
     * Find artefacts with NoMatch location and send them for reporting.
     */
    public void reportNoMatchArtefacts() {
        findNoMatchArtefactsForReporting(artefactRepository.findAllNoMatchArtefacts());
    }

    /**
     * Receives a list of no match artefacts, checks it's not empty and create a map of location id to Provenance.
     * Send this on to publication services.
     *
     * @param artefactList A list of no match artefacts
     */
    private void findNoMatchArtefactsForReporting(List<Artefact> artefactList) {
        if (!artefactList.isEmpty()) {
            List<NoMatchArtefact> noMatchArtefactList = new ArrayList<>();

            artefactList.forEach(artefact -> noMatchArtefactList.add(
                new NoMatchArtefact(artefact.getArtefactId(), artefact.getProvenance(),
                                    NoMatchArtefactHelper.getLocationIdForNoMatch(artefact.getLocationId()))
            ));

            publicationServicesService.sendNoMatchArtefactsForReporting(noMatchArtefactList);
        }
    }
}
