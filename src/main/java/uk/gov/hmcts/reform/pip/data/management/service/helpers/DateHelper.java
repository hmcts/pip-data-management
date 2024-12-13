package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ProcessingException;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("PMD.TooManyMethods")
public final class DateHelper {

    private static final int ONE = 1;
    private static final String EUROPE_LONDON = "Europe/London";
    private static final int MINUTES_PER_HOUR = 60;
    private static final int HOURS_PER_DAY = 24;

    private DateHelper() {
        throw new UnsupportedOperationException();
    }

    public static String formatTimeStampToBst(String timestamp, Language language, boolean isTimeOnly,
                                              boolean isBothDateAndTime) {
        return formatTimeStampToBst(timestamp, language, isTimeOnly, isBothDateAndTime, "dd MMMM yyyy");
    }

    public static String formatTimeStampToBst(String timestamp, Language language, boolean isTimeOnly,
                                              boolean isBothDateAndTime, String dateFormat) {
        ZonedDateTime zonedDateTime = convertStringToBst(timestamp);
        String pattern = getDateTimeFormat(zonedDateTime, isTimeOnly, isBothDateAndTime, language,
                                                      dateFormat);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern, Locale.UK);
        return dtf.format(zonedDateTime);
    }

    private static String getDateTimeFormat(ZonedDateTime zonedDateTime, boolean isTimeOnly,
                                            boolean isBothDateAndTime, Language language,
                                            String dateFormat) {
        if (isTimeOnly) {
            return (zonedDateTime.getMinute() == 0) ? "ha" : "h:mma";
        } else if (isBothDateAndTime) {
            return (language == Language.ENGLISH) ? dateFormat + " 'at' HH:mm" : dateFormat + " 'am' HH:mm";
        }
        return dateFormat;
    }

    public static String formatLocalDateTimeToBst(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.UK));
    }

    static String formatDurationInDays(int days, Language language) {
        String format;
        if (language == Language.ENGLISH) {
            format = days > ONE ? "days" : "day";
        } else {
            format = "dydd";
        }
        return days + " " + format;
    }

    static String formatDuration(int hours, int minutes, Language language) {
        if (hours > 0 && minutes > 0) {
            return formatTime(hours, minutes, language);
        } else if (hours > 0 && minutes == 0) {
            return formatHour(hours, language);
        } else if (hours == 0 && minutes > 0) {
            return formatMinute(minutes, language);
        }
        return "";
    }

    private static String formatTime(int hours, int minutes, Language language) {
        return formatHour(hours, language) + " " + formatMinute(minutes, language);
    }

    private static String formatHour(int hours, Language language) {
        String format;
        if (language == Language.ENGLISH) {
            format = hours > ONE ? "hours" : "hour";
        } else {
            format = "awr";
        }
        return hours + " " + format;
    }

    private static String formatMinute(int mins, Language language) {
        String format;
        if (language == Language.ENGLISH) {
            format = mins > ONE ? "mins" : "min";
        } else {
            format = "munud";
        }
        return mins + " " + format;
    }

    static ZonedDateTime convertStringToBst(String timestamp) {
        Instant unZonedDateTime = Instant.parse(timestamp);
        ZoneId zone = ZoneId.of(EUROPE_LONDON);
        return unZonedDateTime.atZone(zone);
    }

    public static void calculateDuration(JsonNode sitting, Language language) {
        calculateDuration(sitting, language, false);
    }

    public static void calculateDuration(JsonNode sitting, Language language, boolean dayCalculation) {
        ZonedDateTime sittingStart = convertStringToBst(sitting.get("sittingStart").asText());
        ZonedDateTime sittingEnd = convertStringToBst(sitting.get("sittingEnd").asText());

        double durationAsHours = 0;
        double durationAsMinutes = Duration.between(sittingStart, sittingEnd).toMinutes();

        if (durationAsMinutes >= MINUTES_PER_HOUR) {
            durationAsHours = Math.floor(durationAsMinutes / MINUTES_PER_HOUR);
            durationAsMinutes = durationAsMinutes - (durationAsHours * MINUTES_PER_HOUR);
        }

        String formattedDuration;
        if (dayCalculation && durationAsHours >= HOURS_PER_DAY) {
            formattedDuration = formatDurationInDays((int) Math.floor(durationAsHours / HOURS_PER_DAY), language);
        } else {
            formattedDuration = formatDuration(
                (int) durationAsHours,
                (int) durationAsMinutes, language
            );
        }

        ((ObjectNode) sitting).put("formattedDuration", formattedDuration);
    }

    public static void formatStartTime(JsonNode sitting, String format) {
        ZonedDateTime sittingStart = convertStringToBst(sitting.get("sittingStart").asText());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format).withLocale(Locale.UK);

        if (sittingStart.getMinute() == 0) {
            dtf = DateTimeFormatter.ofPattern("ha").withLocale(Locale.UK);
        }

        String time = dtf.format(sittingStart);
        ((ObjectNode) sitting).put("time", time);
    }
}
