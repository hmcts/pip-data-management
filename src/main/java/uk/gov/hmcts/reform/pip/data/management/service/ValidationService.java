package uk.gov.hmcts.reform.pip.data.management.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DateValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.EmptyRequiredHeaderException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ValidationService {

    private List<String> requiredHeaders;
    private Map<String, Object> headers;

    private void setupRequiredHeaders() {
        headers = new HashMap<>();
        requiredHeaders = new ArrayList<>();
        requiredHeaders.add(PublicationConfiguration.PROVENANCE_HEADER);
        requiredHeaders.add(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER);
        requiredHeaders.add(PublicationConfiguration.TYPE_HEADER);
        requiredHeaders.add(PublicationConfiguration.COURT_ID);
    }

    /**
     * Class that guides the validation process.
     *
     * @param headers - a hashmap of all the headers taken in by the endpoint. Importantly, this may contain nulls (i.e.
     *                cannot be replaced with a ConcurrentHashMap.
     * @return Map(String, Object) - an amended version of the headers. If changes (i.e. conditional defaults)
     *      are created, ensure the logic affects the headers map within this class.
     */
    public Map<String, Object> validateHeaders(Map<String, Object> headers) {
        setupRequiredHeaders();
        this.headers = headers;
        handleTypeConditionalRequired();

        headers.keySet().forEach(header -> {
            if (requiredHeaders.contains(header)) {
                validateRequiredHeader(header, headers.get(header));
            }
        });

        return this.headers;
    }

    /**
     * Some fields are required only conditionally, based on the TYPE (LIST, JUDGEMENTS_AND_OUTCOMES and
     * GENERAL_PUBLICATION), this method handles the different required headers validation.
     */
    private void handleTypeConditionalRequired() {
        switch ((ArtefactType) headers.get(PublicationConfiguration.TYPE_HEADER)) {
            case LIST:
            case JUDGEMENTS_AND_OUTCOMES:
                handleDateValidation(false);
                handleRequiredJudgementOutcomeHeaders();
                break;
            case GENERAL_PUBLICATION:
                handleDateValidation(true);
                break;
        }
    }

    /**
     * Container class for all date from/to logic. LIST and JUDGEMENTS_AND_OUTCOMES both require both to and from dates,
     * whereas GENERAL_PUBLICATION doesn't require any, but produces a default from date if empty.
     *
     * @param isDefaultNeeded bool to determine if a default is needed, true will set default to today and false will
     *                       mean required date
     */
    private void handleDateValidation(boolean isDefaultNeeded) {
        LocalDateTime displayFrom = (LocalDateTime) headers.get(PublicationConfiguration.DISPLAY_FROM_HEADER);
        LocalDateTime displayTo = (LocalDateTime) headers.get(PublicationConfiguration.DISPLAY_TO_HEADER);
        ArtefactType type = (ArtefactType) headers.get(PublicationConfiguration.TYPE_HEADER);
        if (isDefaultNeeded) {
            headers.put(PublicationConfiguration.DISPLAY_FROM_HEADER, checkAndReplaceDateWithDefault(displayFrom));
        } else {
            validateRequiredDates(PublicationConfiguration.DISPLAY_FROM_HEADER, displayFrom, type);
            validateRequiredDates(PublicationConfiguration.DISPLAY_TO_HEADER, displayTo, type);
        }
    }

    /**
     * Adds headers that are required for the type JUDGEMENTS_AND_OUTCOMES.
     */
    private void handleRequiredJudgementOutcomeHeaders() {
        validateRequiredHeader(PublicationConfiguration.LIST_TYPE, headers.get(PublicationConfiguration.LIST_TYPE));
        validateRequiredHeader(PublicationConfiguration.CONTENT_DATE,
                               headers.get(PublicationConfiguration.CONTENT_DATE));
    }

    /**
     * Null check class which produces tailored exceptions for required headers.
     *
     * @param headerName - used for the error msg
     * @param date       - checked var
     * @param type       - used for error msg
     */
    private void validateRequiredDates(String headerName, Object date, Object type) {
        if (date == null) {
            throw new DateValidationException(String.format("%s Field is required for artefact type %s", headerName,
                                                            type
            ));
        }
    }

    /**
     * Empty check for required headers.
     *
     * @param headerName - for error msg.
     * @param header     - checked var.
     */
    private void validateRequiredHeader(String headerName, Object header) {
        if (isNullOrEmpty(header)) {
            throw new EmptyRequiredHeaderException(String.format(
                "%s is mandatory however an empty value is provided",
                headerName
            ));
        }
    }

    /**
     * Null check class for creating default date objects if null.
     *
     * @param date - LocalDateTime.
     * @return LocalDateTime representing either current date/time or an existing date.
     */
    private LocalDateTime checkAndReplaceDateWithDefault(Object date) {
        if (date == null) {
            return LocalDateTime.now();
        } else {
            return (LocalDateTime) date;
        }
    }

    private static boolean isNullOrEmpty(Object header) {
        return header == null || header.toString().isEmpty();
    }
}
