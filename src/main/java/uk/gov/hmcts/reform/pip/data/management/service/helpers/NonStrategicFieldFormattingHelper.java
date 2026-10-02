package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.google.common.base.CaseFormat;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ProcessingException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class NonStrategicFieldFormattingHelper {
    private NonStrategicFieldFormattingHelper() {
    }

    public static String formatDateField(String date) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
            LocalDate parsedDate = LocalDate.parse(date, inputFormatter);
            return parsedDate.format(outputFormatter);
        } catch (DateTimeParseException e) {
            throw new ProcessingException("Failed to convert date format");
        }
    }

    public static String formatTimeField(String time) {
        return time == null ? time : time.replace('.', ':');
    }

    public static String formatFieldInLowerCamelCaseFormat(String value) {
        String upperUnderscoreValue = value.toUpperCase(Locale.ENGLISH)
            .replaceAll(" ", "_")
            .replaceAll("[()]", "");
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, upperUnderscoreValue);
    }
}
