package uk.gov.hmcts.reform.pip.data.management.helpers;

import org.apache.commons.lang3.math.NumberUtils;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicFieldFormattingHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class ValidationErrorFormattingHelper {
    private ValidationErrorFormattingHelper() {
    }

    public static List<String> formatErrors(Set<String> errors) {
        List<String> formattedErrors = new ArrayList<>();
        errors.forEach(error -> {
            // Error message starts with error location component followed by error details
            String[] errorComponents = error.split(":");
            if (errorComponents.length > 0 && errorComponents[0].startsWith("/")) {
                // Error has location information. Store the location information
                formattedErrors.add(getErrorLocation(error));
            } else if (errorComponents.length == 2 && errorComponents[0].isEmpty()) {
                // Error has blank location information so use the error details instead
                formattedErrors.add(errorComponents[1].trim());
            } else {
                formattedErrors.add(error);
            }
        });
        Collections.sort(formattedErrors);
        return formattedErrors;
    }

    private static String getErrorLocation(String error) {
        StringBuilder errorLocation = new StringBuilder();
        String[] errorComponents = error.split(":");
        String[] locationComponents = errorComponents[0].substring(1).split("/");
        if (locationComponents.length == 1 && NumberUtils.isNumber(locationComponents[0])) {
            errorLocation.append("Row ")
                .append(Integer.parseInt(locationComponents[0]) + 1);
        } else if (locationComponents.length == 2) {
            if (NumberUtils.isCreatable(locationComponents[0])) {
                errorLocation.append("Row ")
                    .append(Integer.parseInt(locationComponents[0]) + 1)
                    .append(" of ")
                    .append(NonStrategicFieldFormattingHelper
                                .formatInternalFieldNameForDisplay(locationComponents[1], true))
                    .append(" column");
            } else if (NumberUtils.isCreatable(locationComponents[1])) {
                errorLocation.append("Sheet ")
                    .append(NonStrategicFieldFormattingHelper
                                .formatInternalFieldNameForDisplay(locationComponents[0], true))
                    .append(" - Row ")
                    .append(Integer.parseInt(locationComponents[1]) + 1);
            }
        } else if (locationComponents.length == 3 && NumberUtils.isCreatable(locationComponents[1])) {
            errorLocation.append("Sheet ")
                .append(NonStrategicFieldFormattingHelper
                            .formatInternalFieldNameForDisplay(locationComponents[0], true))
                .append(" - Row ")
                .append(Integer.parseInt(locationComponents[1]) + 1)
                .append(" of ")
                .append(NonStrategicFieldFormattingHelper
                            .formatInternalFieldNameForDisplay(locationComponents[2], true))
                .append(" column");
        } else {
            errorLocation.append(error);
        }
        return errorLocation.toString();
    }
}
