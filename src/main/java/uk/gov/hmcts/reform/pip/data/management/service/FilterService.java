package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;

import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class FilterService {

    public List<Hearing> filterHearingsByName(String searchInput, List<Hearing> hearings) {
        return hearings.stream()
             .filter(hearing -> hearing.getCaseName().toLowerCase(Locale.ENGLISH)
                 .contains(searchInput.toLowerCase(Locale.ENGLISH)))
             .toList();
    }

    public Hearing findHearingByCaseNumber(String caseNumber, List<Hearing> hearings) {
        return hearings.stream()
            .filter(hearing -> hearing.getCaseNumber().equalsIgnoreCase(caseNumber))
            .findFirst().orElse(null);
    }

    public Hearing findHearingByUrn(String urn, List<Hearing> hearings) {
        return hearings.stream()
            .filter(hearing -> hearing.getUrn().equalsIgnoreCase(urn))
            .findFirst().orElse(null);
    }
}
