package uk.gov.hmcts.reform.pip.data.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.CourtsAndHearings;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.LiveCaseStatusException;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.LiveCaseStatus;

import java.util.List;

@Service
public class LiveCaseStatusService {

    @Autowired
    private CourtsAndHearings courtsAndHearings;

    public List<LiveCaseStatus> handleLiveCaseRequest(int courtId) {
        List<LiveCaseStatus> result = courtsAndHearings.getLiveCaseStatus(courtId);

        if (result.isEmpty()) {
            throw new LiveCaseStatusException(String.format("No live cases found for court id: %s", courtId));
        } else {
            return result;
        }
    }
}
