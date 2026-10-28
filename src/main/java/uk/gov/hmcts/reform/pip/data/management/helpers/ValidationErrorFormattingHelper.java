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

    public static List<String> format(Set<String> errors) {
        List<String> formattedErrors = new ArrayList<>();
        errors.forEach(error -> {
            String[] errorComponents = error.split(":");
            StringBuilder errorLocation = new StringBuilder();
            if (errorComponents.length > 0 && errorComponents[0].startsWith("/")) {
                String[] locationComponents = errorComponents[0].substring(1).split("/");
                if (locationComponents.length == 2) {
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
                    errorLocation.append(errorComponents[0]);
                }
                formattedErrors.add(errorLocation.toString());
            } else {
                formattedErrors.add(error);
            }
        });
        Collections.sort(formattedErrors);
        return formattedErrors;
    }
}
