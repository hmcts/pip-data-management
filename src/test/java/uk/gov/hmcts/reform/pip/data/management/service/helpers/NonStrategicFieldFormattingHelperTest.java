package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ProcessingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NonStrategicFieldFormattingHelperTest {
    private static final String RESULT_MATCHED_MESSAGE = "Result does not match";
    private static final String EXCEPTION_MESSAGE = "Exception does not match";

    @Test
    void shouldConvertCorrectDateFormat() {
        assertThat(NonStrategicFieldFormattingHelper.formatDateField("01/12/2024"))
            .as(RESULT_MATCHED_MESSAGE)
            .isEqualTo("1 December 2024");
    }

    @Test
    void shouldThrowExceptionForIncorrectDateFormat() {
        assertThatThrownBy(() -> NonStrategicFieldFormattingHelper.formatDateField("01-12-2024"))
            .as(EXCEPTION_MESSAGE)
            .isInstanceOf(ProcessingException.class)
            .hasMessage("Failed to convert date format");
    }
}
