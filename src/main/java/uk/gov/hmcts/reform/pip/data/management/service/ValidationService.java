package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.SeverityLevel;
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
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that guides the validation process.
 */
@Service
public class ValidationService {

    private final JsonSchema masterSchema;

    private final TelemetryClient telemetry = new TelemetryClient();

    Map<ListType, JsonSchema> validationSchemas = new ConcurrentHashMap<>();

    @Autowired
    public ValidationService(ValidationConfiguration validationConfiguration) {
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

        try (InputStream masterFile = this.getClass().getClassLoader()
            .getResourceAsStream(validationConfiguration.getMasterSchema())) {
            masterSchema = schemaFactory.getSchema(masterFile);
        } catch (Exception exception) {
            throw new PayloadValidationException(String.join(exception.getMessage()));
        }

        validationConfiguration.getValidationSchemas().forEach((key, value) -> {
            try (InputStream schemaFile = this.getClass().getClassLoader()
                .getResourceAsStream(value)) {

                validationSchemas.put(
                    ListType.valueOf(key),
                    schemaFactory.getSchema(schemaFile)
                );
            } catch (Exception exception) {
                throw new PayloadValidationException(String.join(exception.getMessage()));
            }
        });
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
        handleStringValidation(headers);
        handleDateValidation(headers);
        handleDefaultSensitivity(headers);

        return headers;
    }

    /**
     * By default, spring does not validate empty strings using the required flags in the @RequestHeader. Therefore
     * this check is required to validate that they are not empty
     * @param headers The headers to validate.
     */
    private void handleStringValidation(HeaderGroup headers) {
        validateRequiredHeader(PublicationConfiguration.PROVENANCE_HEADER, headers.getProvenance());
        validateRequiredHeader(PublicationConfiguration.COURT_ID, headers.getCourtId());
    }

    /**
     * Container class for all date from/to logic. LIST and JUDGEMENTS_AND_OUTCOMES both require both to and from dates,
     * whereas GENERAL_PUBLICATION doesn't require any, but produces a default from date if empty.
     *
     * @param headers The group of headers to validate
     */
    private void handleDateValidation(HeaderGroup headers) {
        LocalDateTime displayFrom = headers.getDisplayFrom();
        LocalDateTime displayTo = headers.getDisplayTo();
        if (headers.getType().equals(ArtefactType.GENERAL_PUBLICATION)) {
            headers.setDisplayFrom(checkAndReplaceDateWithDefault(displayFrom));
        } else {
            validateRequiredDates(PublicationConfiguration.DISPLAY_FROM_HEADER, displayFrom, headers.getType());
            validateRequiredDates(PublicationConfiguration.DISPLAY_TO_HEADER, displayTo, headers.getType());
        }
    }

    /**
     * Sets the default sensitivity to PUBLIC, if no sensitivity has been provided.
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
    public void validateBody(String jsonPayload, HeaderGroup headers) {
        Map<String, String> propertiesMap = headers.getAppInsightsHeaderMap();
        try {
            List<String> errors = new ArrayList<>();

            JsonNode json = new ObjectMapper().readTree(jsonPayload);
            masterSchema.validate(json).forEach(vm ->  errors.add(vm.getMessage()));

            validationSchemas.forEach((type, schema) -> {
                if (type.equals(headers.getListType())) {
                    schema.validate(json).forEach(vm ->  errors.add(vm.getMessage()));
                }
            });

            if (!errors.isEmpty()) {
                propertiesMap.put("ERROR", errors.toString());
                telemetry.trackTrace(jsonPayload, SeverityLevel.Error, propertiesMap);
                throw new PayloadValidationException(String.join(", ", errors));
            }

        } catch (IOException exception) {
            propertiesMap.put("ERROR", exception.getMessage());
            telemetry.trackTrace(jsonPayload, SeverityLevel.Error, propertiesMap);
            throw new PayloadValidationException("Error while parsing JSON Payload");
        }
    }

    /**
     * Empty check for headers.
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
