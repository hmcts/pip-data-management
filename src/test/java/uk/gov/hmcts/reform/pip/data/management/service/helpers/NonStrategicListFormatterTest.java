package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CARE_STANDARDS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CST_WEEKLY_HEARING_LIST;

@ActiveProfiles("test")
class NonStrategicListFormatterTest {
    private static final String DATE_FIELD = "date";
    private static final String OTHER_FIELD = "other";
    private static final String FIELD_NAME = "field";
    private static final String TEST_VALUE = "test";
    private static final String DATE_VALUE1 = "01/01/2025";
    private static final String DATE_VALUE2 = "10/02/2025";
    private static final String RESULT_MATCHED_MESSAGE = "Result does not match";
    private static final String RESULT_EMPTY_MESSAGE = "Result is not empty";
    private static final Map<String, Function<String, String>> FORMATTER = Map.of(FIELD_NAME,
                                                                                  v -> v.toUpperCase(Locale.UK));

    @Test
    void shouldGetListTypeFormatterIfIfListTypeExists() {
        assertThat(NonStrategicListFormatter.getListTypeFormatter(CST_WEEKLY_HEARING_LIST))
            .as(RESULT_MATCHED_MESSAGE)
            .isPresent();
    }

    @Test
    void shouldNotGetListTypeFormatterIfIfListTypeDoesNotExist() {
        assertThat(NonStrategicListFormatter.getListTypeFormatter(CARE_STANDARDS_LIST))
            .as(RESULT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldFormatDateFieldIfListTypeExists() {
        List<Map<String, String>> data = List.of(
            Map.of(DATE_FIELD, DATE_VALUE1),
            Map.of(DATE_FIELD, DATE_VALUE2)
        );
        List<Map<String, String>> result = NonStrategicListFormatter.formatAllFields(
            data, CST_WEEKLY_HEARING_LIST
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
        List<Map<String, String>> result = NonStrategicListFormatter.formatAllFields(
            data, CARE_STANDARDS_LIST
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
        List<Map<String, String>> result = NonStrategicListFormatter.formatAllFields(data, CARE_STANDARDS_LIST);

        assertThat(result.get(0).get(OTHER_FIELD))
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo(DATE_VALUE1);

        assertThat(result.get(1).get(OTHER_FIELD))
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo(DATE_VALUE2);
    }

    @Test
    void shouldFormatFieldValueIfFieldInFormatter() {
        assertThat(NonStrategicListFormatter.formatField(FIELD_NAME, TEST_VALUE, FORMATTER))
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo("TEST");
    }

    @Test
    void shouldReturnOriginalFieldValueIfFieldNotInFormatter() {
        assertThat(NonStrategicListFormatter.formatField(OTHER_FIELD, TEST_VALUE, FORMATTER))
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo(TEST_VALUE);
    }
}
