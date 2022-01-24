package uk.gov.hmcts.reform.pip.data.management.service;

import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DateValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.EmptyRequiredHeaderException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;

import java.time.LocalDateTime;
import java.util.Objects;


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
        handleAlwaysRequired(headers);
        handleTypeConditionalRequired(headers);

        return headers;
    }

    private void handleAlwaysRequired(HeaderGroup headers) {
        validateRequiredHeader(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, headers.getSourceArtefactId());
        validateRequiredHeader(PublicationConfiguration.PROVENANCE_HEADER, headers.getProvenance());
        validateRequiredHeader(PublicationConfiguration.TYPE_HEADER, headers.getType());
        validateRequiredHeader(PublicationConfiguration.COURT_ID, headers.getCourtId());
    }

    /**
     * Some fields are required only conditionally, based on the TYPE (LIST, JUDGEMENTS_AND_OUTCOMES and
     * GENERAL_PUBLICATION), this method handles the different required headers validation.
     */
    private void handleTypeConditionalRequired(HeaderGroup headers) {
        switch (headers.getType()) {
            case LIST:
            case JUDGEMENTS_AND_OUTCOMES:
                handleDateValidation(false, headers);
                handleRequiredJudgementOutcomeHeaders(headers);
                break;
            case GENERAL_PUBLICATION:
                handleDateValidation(true, headers);
                break;
            default:
                throw new HeaderValidationException("Type was not of the defined values");
        }
    }

    /**
     * Container class for all date from/to logic. LIST and JUDGEMENTS_AND_OUTCOMES both require both to and from dates,
     * whereas GENERAL_PUBLICATION doesn't require any, but produces a default from date if empty.
     *
     * @param isDefaultNeeded bool to determine if a default is needed, true will set default to today and false will
     *                       mean required date
     */
    private void handleDateValidation(boolean isDefaultNeeded, HeaderGroup headers) {
        LocalDateTime displayFrom = headers.getDisplayFrom();
        LocalDateTime displayTo = headers.getDisplayTo();
        if (isDefaultNeeded) {
            headers.setDisplayFrom(checkAndReplaceDateWithDefault(displayFrom));
        } else {
            validateRequiredDates(PublicationConfiguration.DISPLAY_FROM_HEADER, displayFrom, headers.getType());
            validateRequiredDates(PublicationConfiguration.DISPLAY_TO_HEADER, displayTo, headers.getType());
        }
    }

    /**
     * Adds headers that are required for the type JUDGEMENTS_AND_OUTCOMES.
     */
    private void handleRequiredJudgementOutcomeHeaders(HeaderGroup headers) {
        validateRequiredHeader(PublicationConfiguration.LIST_TYPE, headers.getListType());
        validateRequiredHeader(PublicationConfiguration.CONTENT_DATE, headers.getContentDate());
    }

    /**
     * Null check class which produces tailored exceptions for required headers.
     *
     * @param headerName  used for the error msg
     * @param date        checked var
     * @param type        used for error msg
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
     * @param date  LocalDateTime.
     * @return LocalDateTime representing either current date/time or an existing date.
     */
    private LocalDateTime checkAndReplaceDateWithDefault(LocalDateTime date) {
        return Objects.requireNonNullElseGet(date, LocalDateTime::now);
    }

    private static boolean isNullOrEmpty(Object header) {
        return header == null || header.toString().isEmpty();
    }
}
