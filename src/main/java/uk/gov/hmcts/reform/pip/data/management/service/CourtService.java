package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.CourtsAndHearings;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CourtNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.CourtMethods;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service to handle the retrieval and filtering of courts.
 */
@Service
@Slf4j
public class CourtService {

    @Autowired
    private CourtsAndHearings courtsAndHearings;

    @Autowired
    private FilterService filterService;

    /**
     * Gets all courts.
     *
     * @return List of Courts
     */
    public List<Court> getAllCourts() {
        return alphabetiseCourts(courtsAndHearings.getListCourts());
    }

    /**
     * Handles request to search for a court by the court name.
     *
     * @param input the court name to search for
     * @return Court of the found court
     * @throws CourtNotFoundException when no court was found with the given search input
     */
    public Court handleSearchCourt(String input) {
        List<Court> foundCourt = filterService.filterCourts(input, CourtMethods.NAME.methodName);
        if (foundCourt.isEmpty()) {
            throw new CourtNotFoundException(String.format("No court found with the search: %s", input));
        } else {
            return filterService.filterCourts(input, CourtMethods.NAME.methodName).get(0);
        }
    }

    /**
     * Handles filtering the courts based on filters such as location/jurisdiction and searches the values against them
     * narrowing down the list each pass on the filter.
     *
     * @param filters list of filters to use e.g. location/jurisdiction - must be a field in the Court object, new
     *                filters are to be added in models/CourtMethods
     * @param values  the search values to search against each filter
     * @return List of Court objects, can return empty List
     */
    public List<Court> handleFilterRequest(List<String> filters, List<String> values) {
        List<Court> filteredList = getAllCourts();

        for (String filter : filters) {
            List<List<Court>> valuesList = new ArrayList<>();
            for (String searchValue : values) {
                valuesList.add(
                    values.indexOf(searchValue),
                    filterService.filterCourts(
                        searchValue,
                        CourtMethods.valueOf(filter.toUpperCase(Locale.ROOT)).methodName,
                        Optional.of(filteredList)
                    )
                );
            }
            filteredList = valuesList.stream().flatMap(List::stream).collect(Collectors.toList());
        }
        return alphabetiseCourts(new ArrayList<>(new HashSet<>(filteredList)));
    }

    /**
     * Alphabetises a court list based on the court name.
     * @param list court list to be alphabetised
     * @return sorted list
     */
    private List<Court> alphabetiseCourts(List<Court> list) {
        return list.stream().sorted(Comparator.comparing(Court::getName)).collect(Collectors.toList());
    }
}
