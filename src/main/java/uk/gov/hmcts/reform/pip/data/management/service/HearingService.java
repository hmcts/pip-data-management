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

    /**
     * Returns all hearings for a court ID.
     * @param courtId int for court id to search by
     * @return List of hearings related to that court ID
     */
    public List<Hearing> getHearings(int courtId) {
        if (courtsAndHearings.getListHearings(courtId).isEmpty()) {
            throw new HearingNotFoundException(String.format("No hearings found for court id: %s", courtId));
        } else {
            return courtsAndHearings.getListHearings(courtId);
        }
    }
}
