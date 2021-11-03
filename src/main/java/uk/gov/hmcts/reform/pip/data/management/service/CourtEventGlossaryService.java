package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.CourtsAndHearings;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.EventGlossary;

import java.util.List;

/**
 * Service to handle the retrieval of courts events glossary.
 */
@Service
@Slf4j
public class CourtEventGlossaryService {
    @Autowired
    private CourtsAndHearings courtsAndHearings;

    /**
     * Gets all court event event glossary.
     *
     * @return List of Court event glossary
     */
    public List<EventGlossary> getAllCourtEventGlossary() {
        return courtsAndHearings.getListCourtEvents();
    }
}
