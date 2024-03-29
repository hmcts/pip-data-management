package uk.gov.hmcts.reform.pip.data.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.CourtsAndHearings;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.CaseEventGlossary;

import java.util.List;

/**
 * Service to handle the retrieval of case event glossary list.
 */
@Service
public class CaseEventGlossaryService {
    private final CourtsAndHearings courtsAndHearings;

    @Autowired
    public CaseEventGlossaryService(CourtsAndHearings courtsAndHearings) {
        this.courtsAndHearings = courtsAndHearings;
    }

    /**
     * Gets all court event glossary.
     *
     * @return List of Court event glossary
     */
    public List<CaseEventGlossary> getAllCaseEventGlossary() {
        return courtsAndHearings.getListCaseEventGlossary();
    }
}
