package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.helpers.listmanipulation;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist.OpaPressList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.OpaPressListSorter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class OpaPressListSorterTest {
    private static final String MESSAGE = "Keys not sorted correctly";

    @Test
    void testSortingOfDatesInDescendingOrder() {
        Map<String, List<OpaPressList>> input = new ConcurrentHashMap<>();
        input.putAll(Map.of(
            "10/01/2020", List.of(new OpaPressList()),
            "Any text", List.of(new OpaPressList()),
            "11/12/2019", List.of(new OpaPressList()),
            "12/01/2020", List.of(new OpaPressList()),
            "09/02/2020", List.of(new OpaPressList()),
            "08/01/2021", List.of(new OpaPressList())
        ));

        assertThat(OpaPressListSorter.sort(input))
            .as(MESSAGE)
            .isEqualTo(Map.of(
                "08/01/2021", List.of(new OpaPressList()),
                "09/02/2020", List.of(new OpaPressList()),
                "12/01/2020", List.of(new OpaPressList()),
                "10/01/2020", List.of(new OpaPressList()),
                "11/12/2019", List.of(new OpaPressList()),
                "Any text", List.of(new OpaPressList())
            ));
    }
}
