package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.oparesults.OpaResults;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class OpaResultsSorter {
    private static final String CURRENT_DATE_FORMAT = "dd MMMM yyyy";
    private static final String NEW_DATE_FORMAT = "yyyyMMdd";

    private static final Comparator<Map.Entry<String, List<OpaResults>>> COMPARATOR = (s1, s2) ->
        convertDateToSortValue(s2.getKey()) - convertDateToSortValue(s1.getKey());

    private OpaResultsSorter() {
    }

    /**
     * Sort by decision date in descending order.
     * @param cases Map of decision date to OPA results cases
     * @return Sorted map of OPA results cases
     */
    public static Map<String, List<OpaResults>> sort(Map<String, List<OpaResults>> cases) {
        return cases.entrySet().stream()
            .sorted(COMPARATOR)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private static int convertDateToSortValue(String date) {
        String dateInNewFormat = LocalDate.parse(date, DateTimeFormatter.ofPattern(CURRENT_DATE_FORMAT, Locale.UK))
            .format(DateTimeFormatter.ofPattern(NEW_DATE_FORMAT,Locale.UK));
        try {
            return Integer.parseInt(dateInNewFormat);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
