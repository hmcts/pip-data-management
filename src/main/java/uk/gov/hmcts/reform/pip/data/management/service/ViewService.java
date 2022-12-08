package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

/**
 * Service class which handles dealing with views.
 */
@Service
@Slf4j
public class ViewService {

    @Autowired
    ArtefactRepository artefactRepository;

    @Autowired
    LocationRepository locationRepository;

    /**
     * Service method which refreshes the view.
     */
    public void refreshView() {
        log.info(writeLog("Refreshing Artefact and Location view"));
        artefactRepository.refreshArtefactView();
        locationRepository.refreshLocationView();
    }

}
