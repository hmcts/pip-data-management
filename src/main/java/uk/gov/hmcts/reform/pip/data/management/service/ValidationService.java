package uk.gov.hmcts.reform.pip.data.management.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    private final List<String> requiredHeaders;
    private Map<String, Object> headers;

    @Autowired
    public ValidationService() {
        headers = new HashMap<>();
        requiredHeaders = new ArrayList<>();
        requiredHeaders.add(PublicationConfiguration.PROVENANCE_HEADER);
        requiredHeaders.add(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER);
        requiredHeaders.add(PublicationConfiguration.TYPE_HEADER);
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
        this.headers = headers;

        headers.keySet().forEach(header -> {
            if (requiredHeaders.contains(header)) {
                validateRequiredHeader(header, headers.get(header));
            }
        });

        handleDateValidation();

        return this.headers;
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
        if (header.toString().isEmpty()) {
            throw new EmptyRequiredHeaderException(String.format(
                "%s is mandatory however an empty value is provided",
                headerName
            ));
        }
    }

    /**
     * Container class for all date from/to logic. OUTCOME, LIST, JUDGEMENT all require both to and from dates,
     * whereas STATUS_UPDATES doesn't require any, but produces a default from date if empty.
     */
    private void handleDateValidation() {
        LocalDateTime displayFrom = (LocalDateTime) headers.get(PublicationConfiguration.DISPLAY_FROM_HEADER);
        LocalDateTime displayTo = (LocalDateTime) headers.get(PublicationConfiguration.DISPLAY_TO_HEADER);
        ArtefactType type = (ArtefactType) headers.get(PublicationConfiguration.TYPE_HEADER);
        if (type.equals(ArtefactType.STATUS_UPDATES)) {
            headers.put(PublicationConfiguration.DISPLAY_FROM_HEADER, checkAndReplaceDateWithDefault(displayFrom));
        } else {
            validateRequiredDates(PublicationConfiguration.DISPLAY_FROM_HEADER, displayFrom, type);
            validateRequiredDates(PublicationConfiguration.DISPLAY_TO_HEADER, displayTo, type);
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
}
