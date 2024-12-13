package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ProcessingException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class NonStrategicFieldHelper {
    private NonStrategicFieldHelper() {
    }

    public static String convertDateFormat(String date) {
        try {
            DateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
            Date convertedDate = originalFormat.parse(date);

            DateFormat targetFormat = new SimpleDateFormat("d MMMM yyyy", Locale.UK);
            return targetFormat.format(convertedDate);
        } catch (ParseException e) {
            throw new ProcessingException("Failed to convert date format");
        }
    }
}
