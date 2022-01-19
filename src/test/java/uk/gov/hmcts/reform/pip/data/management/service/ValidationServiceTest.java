package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("PMD.UseConcurrentHashMap")
class ValidationServiceTest {
    ValidationService validationService = new ValidationService();

    private static final String DIFFERENT_SIZE_MAPS = "Hashmap has grown in the method when it should have remained "
        + "the same size. Ensure you are overwriting rather than appending a new variable.";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String PROVENANCE = "provenance";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String EMPTY_FIELD = "";
    private static final String VALIDATION_EXPECTED_MESSAGE =
        "The expected exception does not contain the correct message";
    private static final String NOT_NULL_MESSAGE = "The returned value is null, but was not expected to be.";


    @Test
    void testCreationOfPublicationEmptySourceArtefactId() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, EMPTY_FIELD);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);

        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });

        assertEquals(
            "x-source-artefact-id is mandatory however an empty value is provided",
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationEmptyProvenance() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, EMPTY_FIELD);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);

        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });

        assertEquals(
            "x-provenance is mandatory however an empty value is provided",
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationEmptySourceArtefactIdAndEmptyProvenanceOnlyFirstIsShown() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, EMPTY_FIELD);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, EMPTY_FIELD);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });

        assertEquals(
            "x-source-artefact-id is mandatory however an empty value is provided",
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationJudgementTypeAndEmptyDateTo() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.JUDGEMENT);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, null);
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });
        assertEquals("x-display-to Field is required for artefact type JUDGEMENT",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationListTypeAndEmptyDateTo() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, null);
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });
        assertEquals("x-display-to Field is required for artefact type LIST",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationOutcomeTypeAndEmptyDateTo() {

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.OUTCOME);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, null);
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });
        assertEquals("x-display-to Field is required for artefact type OUTCOME",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationListTypeAndEmptyDateFrom() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, null);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });
        assertEquals("x-display-from Field is required for artefact type LIST",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationJudgementTypeAndEmptyDateFrom() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.JUDGEMENT);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, null);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });
        assertEquals("x-display-from Field is required for artefact type JUDGEMENT",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationOutcomeTypeAndEmptyDateFrom() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.OUTCOME);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, null);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });
        assertEquals("x-display-from Field is required for artefact type OUTCOME",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testValidationOfStatusUpdateTypeWithNoDateFrom() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.STATUS_UPDATES);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, null);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, null);

        int initialLength = headerMap.size();

        assertNotNull(
            validationService.validateHeaders(headerMap).get(PublicationConfiguration.DISPLAY_FROM_HEADER),
            NOT_NULL_MESSAGE
        );

        int afterLength = validationService.validateHeaders(headerMap).size();
        assertEquals(initialLength, afterLength, DIFFERENT_SIZE_MAPS);
    }

    @Test
    void testValidationOfStatusUpdateTypeWithNoDateTo() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.STATUS_UPDATES);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, null);

        int initialLength = headerMap.size();
        assertNull(
            validationService.validateHeaders(headerMap).get(PublicationConfiguration.DISPLAY_TO_HEADER),
            "The date to header should remain null"
        );
        int afterLength = validationService.validateHeaders(headerMap).size();
        assertEquals(initialLength, afterLength, DIFFERENT_SIZE_MAPS);
    }
}
