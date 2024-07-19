package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist.OpaPressList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class OpaPressListSorter {
    private static final Comparator<Map.Entry<String, List<OpaPressList>>> COMPARATOR = (s1, s2) ->
        convertDateToSortValue(s2.getKey()) - convertDateToSortValue(s1.getKey());

    private OpaPressListSorter() {
    }

    /**
     * Sort by plea date in descending order.
     * @param cases Map of plea date to OPA press list cases
     * @return Sorted map of OPA press list cases
     */
    public static Map<String, List<OpaPressList>> sort(Map<String, List<OpaPressList>> cases) {
        return cases.entrySet().stream()
            .sorted(COMPARATOR)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private static int convertDateToSortValue(String date) {
        String[] components = date.split("/");
        Collections.reverse(Arrays.asList(components));
        try {
            String sortValueStr = Arrays.stream(components)
                .collect(Collectors.joining());
            return Integer.parseInt(sortValueStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
