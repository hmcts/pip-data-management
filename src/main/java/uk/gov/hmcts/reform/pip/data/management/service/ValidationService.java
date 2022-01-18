package uk.gov.hmcts.reform.pip.data.management.service;

import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DateValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.EmptyRequiredHeaderException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ValidationService {

    private static final ArrayList<String> requiredHeaders = new ArrayList<>();
    private Map<String, Object> headers;

    public ValidationService() {
        headers = new HashMap<>();
        requiredHeaders.add(PublicationConfiguration.PROVENANCE_HEADER);
        requiredHeaders.add(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER);
        requiredHeaders.add(PublicationConfiguration.TYPE_HEADER);
    }

    public Map<String, Object> validateHeaders(Map<String, Object> headers) {
        this.headers = headers;
        headers.keySet().forEach(header -> {
            if (requiredHeaders.contains(header)) {
                validateRequiredHeader(header, headers.get(header));
        }});

        handleDateValidation(headers);
        
        return this.headers;
    }
    
    private void validateRequiredDates(String headerName, Object date, Object type) {
        if (date == null) {
            throw new DateValidationException(String.format("%s Field is required for artefact type %s", headerName,
                                                            type));
        }
    }

    private void validateRequiredHeader(String headerName, Object header) {
        if(header.toString().isEmpty()){
            throw new EmptyRequiredHeaderException(String.format("%s is mandatory however an empty value is provided",
                                                                 headerName));
        }
    }
    
    private LocalDateTime handleDateValidation(Map<String, Object> headers) {
        if (!headers.get(PublicationConfiguration.TYPE_HEADER).equals(ArtefactType.STATUS_UPDATES)){
            validateRequiredDates(PublicationConfiguration.DISPLAY_FROM_HEADER,
                                  headers.get(PublicationConfiguration.DISPLAY_FROM_HEADER),
                                  headers.get(PublicationConfiguration.TYPE_HEADER));

            validateRequiredDates(PublicationConfiguration.DISPLAY_TO_HEADER,
                                  headers.get(PublicationConfiguration.DISPLAY_TO_HEADER),
                                  headers.get(PublicationConfiguration.TYPE_HEADER));
        } else {
            if (headers.get(PublicationConfiguration.DISPLAY_FROM_HEADER) == null) {
                return setDefault(headers.get(PublicationConfiguration.DISPLAY_FROM_HEADER));
            }
        }
        return (LocalDateTime) headers.get(PublicationConfiguration.DISPLAY_FROM_HEADER);
    }
    
    private LocalDateTime setDefault(Object date){
        //set default logic here
        return LocalDateTime.now();
    }

}
