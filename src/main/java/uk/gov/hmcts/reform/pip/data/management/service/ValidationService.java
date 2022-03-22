package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.config.ValidationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.DateValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.EmptyRequiredHeaderException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class that guides the validation process.
 */
@Service
public class ValidationService {

    private final JsonSchema masterSchema;
    private final JsonSchema dailyCauseListSchema;
    private final JsonSchema sjpPublicListSchema;
    private final JsonSchema sjpPressListSchema;

    @Autowired
    public ValidationService(ValidationConfiguration validationConfiguration) {
        try (InputStream masterFile = this.getClass().getClassLoader()
            .getResourceAsStream(validationConfiguration.getMasterSchema());

             InputStream dailyCauseListFile = this.getClass().getClassLoader()
                 .getResourceAsStream(validationConfiguration.getDailyCauseList());

             InputStream sjpPublicListFile = this.getClass().getClassLoader()
                .getResourceAsStream(validationConfiguration.getSjpPublicList());

             InputStream sjpPressListFile = this.getClass().getClassLoader()
                 .getResourceAsStream(validationConfiguration.getSjpPressList())) {

            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            masterSchema = schemaFactory.getSchema(masterFile);
            dailyCauseListSchema = schemaFactory.getSchema(dailyCauseListFile);
            sjpPublicListSchema = schemaFactory.getSchema(sjpPublicListFile);
            sjpPressListSchema = schemaFactory.getSchema(sjpPressListFile);


        } catch (Exception exception) {
            throw new PayloadValidationException(String.join(exception.getMessage()));
        }

    }

    /**
     * Method that validates the headers of the inbound request.
     *
     * @param headers - a hashmap of all the headers taken in by the endpoint. Importantly, this may contain nulls (i.e.
     *                cannot be replaced with a ConcurrentHashMap.
     * @return Map(String, Object) - an amended version of the headers. If changes (i.e. conditional defaults)
     *      are created, ensure the logic affects the headers map within this class.
     */
    public HeaderGroup validateHeaders(HeaderGroup headers) {
        handleDateValidation(headers);
        handleSjpCourt(headers);
        handleDefaultSensitivity(headers);

        return headers;
    }

    /**
     * Container method for all date from/to logic. LIST and JUDGEMENTS_AND_OUTCOMES both require both to and from dates,
     * whereas GENERAL_PUBLICATION doesn't require any, but produces a default from date if empty.
     *
     * @param headers - The header group to update.
     */
    private void handleDateValidation(HeaderGroup headers) {
        LocalDateTime displayFrom = headers.getDisplayFrom();
        LocalDateTime displayTo = headers.getDisplayTo();
        if (!headers.getType().equals(ArtefactType.GENERAL_PUBLICATION)) {
            validateRequiredDates(PublicationConfiguration.DISPLAY_FROM_HEADER, displayFrom, headers.getType());
            validateRequiredDates(PublicationConfiguration.DISPLAY_TO_HEADER, displayTo, headers.getType());
        } else if (headers.getDisplayFrom() == null) {
            headers.setDisplayFrom(LocalDateTime.now());
        }
    }

    /**
     * Sets court id to 0 if list type is SJP to conform to our handling of an SJP.
     * @param headers headers to check against.
     */
    private void handleSjpCourt(HeaderGroup headers) {
        if (headers.getListType().isSjp()) {
            headers.setCourtId("0");
        }
    }

    /**
     * Sets the default sensitivity to PUBLIC, if no sensitivity has been provided
     * @param headers headers to check and update.
     */
    private void handleDefaultSensitivity(HeaderGroup headers) {
        if (headers.getSensitivity() == null) {
            headers.setSensitivity(Sensitivity.PUBLIC);
        }
    }

    /**
     * Null check class which produces tailored exceptions for required headers.
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
     * Validates a Multipart File body.
     *
     * @param file The file to validate.
     */
    public void validateBody(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FlatFileException("Empty file provided, please provide a valid file");
        }
    }

    /**
     * Validates a JSON body.
     *
     * @param jsonPayload The JSON body to validate.
     */
    public void validateBody(String jsonPayload, ListType listType) {
        try {
            List<String> errors = new ArrayList<>();

            JsonNode json = new ObjectMapper().readTree(jsonPayload);
            masterSchema.validate(json).forEach(vm ->  errors.add(vm.getMessage()));

            if (listType != null) {
                switch (listType) {
                    case CIVIL_DAILY_CAUSE_LIST:
                        dailyCauseListSchema.validate(json).forEach(vm ->  errors.add(vm.getMessage()));
                        break;
                    case SJP_PUBLIC_LIST:
                        sjpPublicListSchema.validate(json).forEach(vm ->  errors.add(vm.getMessage()));
                        break;
                    case SJP_PRESS_LIST:
                        sjpPressListSchema.validate(json).forEach(vm ->  errors.add(vm.getMessage()));
                        break;
                    default:
                        break;
                }
            }

            if (!errors.isEmpty()) {
                throw new PayloadValidationException(String.join(", ", errors));
            }

        } catch (IOException exception) {
            throw new PayloadValidationException("Error while parsing JSON Payload");
        }
    }
}
