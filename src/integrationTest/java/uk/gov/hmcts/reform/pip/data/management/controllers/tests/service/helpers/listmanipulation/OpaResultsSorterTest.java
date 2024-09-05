package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.helpers.listmanipulation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.oparesults.OpaResults;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.OpaResultsSorter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class OpaResultsSorterTest {
    private static final String MESSAGE = "Keys not sorted correctly";

    @Test
    void testSortingOfDatesInDescendingOrder() {
        Map<String, List<OpaResults>> input = new ConcurrentHashMap<>();
        List<OpaResults> values = List.of(new OpaResults());

        input.putAll(Map.of(
            "10 January 2020", values,
            "11 December 2019", values,
            "11 November 2022", values,
            "12 January 2020", values,
            "09 February 2020", values,
            "08 January 2021", values
        ));

        assertThat(OpaResultsSorter.sort(input))
            .as(MESSAGE)
            .extracting(r -> r.keySet())
            .isEqualTo(Set.of(
                "11 November 2022",
                "08 January 2021",
                "09 February 2020",
                "12 January 2020",
                "10 January 2020",
                "11 December 2019"
            ));
    }
}
