package uk.gov.hmcts.reform.pip.data.management.helpers;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationErrorFormattingHelperTest {
    private static final String ERROR_MESSAGE = "This is a message";

    @Test
    void shouldFormatErrorWithLocationInformationForMultiSheetExcelData() {
        assertThat(ValidationErrorFormattingHelper.formatErrors(Set.of("/hearingList/0/judge", "/businessList/1")))
            .isEqualTo(List.of("Sheet 'Business List' - Row 2", "Sheet 'Hearing List' - Row 1 of 'Judge' column"));
    }

    @Test
    void shouldFormatErrorWithLocationInformationForSingleSheetExcelData() {
        assertThat(ValidationErrorFormattingHelper.formatErrors(Set.of("/0/time", "/2")))
            .isEqualTo(List.of("Row 1 of 'Time' column", "Row 3"));
    }

    @Test
    void shouldFormatErrorWithoutLocationInformation() {
        assertThat(ValidationErrorFormattingHelper.formatErrors(Set.of(": errorDetailsMessage")))
            .isEqualTo(List.of("errorDetailsMessage"));
    }

    @Test
    void shouldReturnOriginalErrorForRandomErrorString() {
        assertThat(ValidationErrorFormattingHelper.formatErrors(Set.of(ERROR_MESSAGE)))
            .isEqualTo(List.of(ERROR_MESSAGE));
    }
}
