package uk.gov.hmcts.reform.pip.data.management.service.publication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PublicationLocationService {
    private final ArtefactRepository artefactRepository;

    @Autowired
    public PublicationLocationService(ArtefactRepository artefactRepository) {
        this.artefactRepository = artefactRepository;
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
}
