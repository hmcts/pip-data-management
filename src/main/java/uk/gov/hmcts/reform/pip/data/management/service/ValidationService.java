package uk.gov.hmcts.reform.pip.data.management.service;


import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DateValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.EmptyRequiredHeaderException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;

import java.time.LocalDateTime;


@Service
public class ValidationService {

    /**
     * Class that guides the validation process.
     *
     * @param headers - a hashmap of all the headers taken in by the endpoint. Importantly, this may contain nulls (i.e.
     *                cannot be replaced with a ConcurrentHashMap.
     * @return Map(String, Object) - an amended version of the headers. If changes (i.e. conditional defaults)
     *      are created, ensure the logic affects the headers map within this class.
     */
    public HeaderGroup validateHeaders(HeaderGroup headers) {
        validateRequiredHeader(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, headers.getSourceArtefactId());
        validateRequiredHeader(PublicationConfiguration.TYPE_HEADER, headers.getType());
        validateRequiredHeader(PublicationConfiguration.PROVENANCE_HEADER, headers.getProvenance());

        return handleDateValidation(headers);
    }

    /**
     * Null check class which produces tailored exceptions for required headers.
     *
     * @param headerName - used for the error msg
     * @param date       - checked var
     * @param type       - used for error msg
     */
    private void validateRequiredDates(String headerName, LocalDateTime date, ArtefactType type) {
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
    private HeaderGroup handleDateValidation(HeaderGroup headerGroup) {
        LocalDateTime displayFrom = headerGroup.getDisplayFrom();
        LocalDateTime displayTo = headerGroup.getDisplayTo();
        ArtefactType type = headerGroup.getType();
        if (type.equals(ArtefactType.STATUS_UPDATES)) {
            headerGroup.setDisplayFrom(checkAndReplaceDateWithDefault(displayFrom));
        } else {
            validateRequiredDates(PublicationConfiguration.DISPLAY_FROM_HEADER, displayFrom, type);
            validateRequiredDates(PublicationConfiguration.DISPLAY_TO_HEADER, displayTo, type);
        }
        return headerGroup;
    }

    /**
     * Null check class for creating default date objects if null.
     *
     * @param date - LocalDateTime.
     * @return LocalDateTime representing either current date/time or an existing date.
     */
    private LocalDateTime checkAndReplaceDateWithDefault(LocalDateTime date) {
        if (date == null) {
            return LocalDateTime.now();
        } else {
            return date;
        }
    }
}
