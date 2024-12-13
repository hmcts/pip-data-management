package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NonStrategicFieldFormatterTest {
    private static final String DATE_FIELD = "date";
    private static final String OTHER_FIELD = "other";
    private static final String DATE_VALUE1 = "01/01/2025";
    private static final String DATE_VALUE2 = "10/02/2025";
    private static final String RESULT_MATCHED_MESSAGE = "Result does not match";

    @Test
    void shouldFormatDateFieldIfListTypeExists() {
        List<Map<String, String>> data = List.of(
            Map.of(DATE_FIELD, DATE_VALUE1),
            Map.of(DATE_FIELD, DATE_VALUE2)
        );
        List<Map<String, String>> result = NonStrategicFieldFormatter.formatFields(
            data, ListType.CST_WEEKLY_HEARING_LIST
        );

        assertThat(result.get(0).get(DATE_FIELD))
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo("1 January 2025");

        assertThat(result.get(1).get(DATE_FIELD))
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo("10 February 2025");
    }

    @Test
    void shouldNotFormatDateFieldIfListTypeDoesNotExist() {
        List<Map<String, String>> data = List.of(
            Map.of(DATE_FIELD, DATE_VALUE1),
            Map.of(DATE_FIELD, DATE_VALUE2)
        );
        List<Map<String, String>> result = NonStrategicFieldFormatter.formatFields(
            data, ListType.CARE_STANDARDS_LIST
        );

        assertThat(result.get(0).get(DATE_FIELD))
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo(DATE_VALUE1);

        assertThat(result.get(1).get(DATE_FIELD))
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo(DATE_VALUE2);
    }

    @Test
    void shouldNotFormatOtherField() {
        List<Map<String, String>> data = List.of(
            Map.of(OTHER_FIELD, DATE_VALUE1),
            Map.of(OTHER_FIELD, DATE_VALUE2)
        );
        List<Map<String, String>> result = NonStrategicFieldFormatter.formatFields(
            data, ListType.CARE_STANDARDS_LIST
        );

        assertThat(result.get(0).get(OTHER_FIELD))
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo(DATE_VALUE1);

        assertThat(result.get(1).get(OTHER_FIELD))
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo(DATE_VALUE2);
    }
}
