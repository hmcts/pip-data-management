package uk.gov.hmcts.reform.pip.data.management.service;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ReflectionException;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilterService {

    @Autowired
    private CourtService courtService;

    /**
     * Overloaded method to get courts based on a search term, to be used when filtering from all courts.
     *
     * @param searchInput the search term to search against, must be exact match
     * @param methodName  the method name to search against, must exist in the Court class.
     * @return List of Courts
     */
    public List<Court> filterCourts(String searchInput, String methodName) {
        return filterCourts(searchInput, methodName, Optional.empty());
    }

    /**
     * Overloaded method to get courts based on search term, to be used against a filtered list to filter further,
     * e.g. filtering jurisdiction from a list already filtered by location.
     *
     * @param searchInput the search term to search against, must be exact match
     * @param methodName  the method name to search against, must exist in the Court class.
     * @param reducedList the pre-filtered list to provide additional filtering against
     * @return List of Courts
     */
    public List<Court> filterCourts(String searchInput, String methodName, Optional<List<Court>> reducedList) {
        List<Court> courts = reducedList.orElse(courtService.getAllCourts());

        courts = courts.stream()
            .filter(court -> {
                try {
                    return !Strings.isNullOrEmpty(String.valueOf(court.getClass().getMethod(methodName).invoke(court)))
                        && String.valueOf(court.getClass().getMethod(methodName).invoke(court)).equalsIgnoreCase(
                        searchInput);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    log.error("Failed to complete reflection on: '{}' for method: {} due to: {}",
                              searchInput, methodName, e.getMessage());
                    throw new ReflectionException(e.getMessage());
                }
            }).collect(Collectors.toList());
        return courts;
    }

    public List<Hearing> filterHearingsByName(String searchInput, List<Hearing> hearings) {
        return hearings.stream()
             .filter(hearing -> hearing.getCaseName().toLowerCase(Locale.ENGLISH)
                 .contains(searchInput.toLowerCase(Locale.ENGLISH)))
             .collect(Collectors.toList());
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
