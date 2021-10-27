package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.CourtsAndHearings;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;

import java.util.List;

/**
 * Service to handle retrieving hearings for a court.
 */
@Service
@Slf4j
public class HearingService {

    @Autowired
    private CourtsAndHearings courtsAndHearings;

    @Autowired
    private FilterService filterService;

    /**
     * Returns all hearings for a court ID.
     * @param courtId int for court id to search by
     * @return List of hearings related to that court ID
     */
    public List<Hearing> getHearings(int courtId) {
        List<Hearing> returnedHearing = courtsAndHearings.getListHearings(courtId);
        if (returnedHearing.isEmpty()) {
            throw new HearingNotFoundException(String.format("No hearings found for court id: %s", courtId));
        } else {
            return returnedHearing;
        }
    }

    public List<Hearing> getHearingByName(String caseName) {
        List<Hearing> returnedHearings = filterService.filterHearingsByName(caseName,
                                                                            courtsAndHearings.getListHearings());
        if (returnedHearings.isEmpty()) {
            throw new HearingNotFoundException(String.format("No hearings found containing the case name: %s",
                                                             caseName));
        } else {
            return returnedHearings;
        }
    }

    public Hearing getHearingByCaseNumber(String caseNumber) {
        Hearing matchedHearing = filterService.findHearingByCaseNumber(caseNumber, courtsAndHearings.getListHearings());
        if (matchedHearing == null) {
            throw new HearingNotFoundException(String.format("No hearing found for case number: %s", caseNumber));
        } else {
            return matchedHearing;
        }
    }

    public Hearing getHearingByUrn(String urn) {
        Hearing matchedHearing = filterService.findHearingByUrn(urn, courtsAndHearings.getListHearings());
        if (matchedHearing == null) {
            throw new HearingNotFoundException(String.format("No hearing found for urn number: %s", urn));
        } else {
            return matchedHearing;
        }
    }
}
