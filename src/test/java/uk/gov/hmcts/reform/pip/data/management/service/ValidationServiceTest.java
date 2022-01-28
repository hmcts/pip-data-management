package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationServiceTest {
    ValidationService validationService = new ValidationService();

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
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, EMPTY_FIELD, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                                  DISPLAY_FROM, DISPLAY_TO
        );
        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });

        assertEquals(
            "x-source-artefact-id is mandatory however an empty value is provided",
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationEmptyProvenance() {
        HeaderGroup headerGroup = new HeaderGroup(EMPTY_FIELD, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                                  DISPLAY_FROM, DISPLAY_TO
        );
        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });

        assertEquals(
            "x-provenance is mandatory however an empty value is provided",
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationEmptySourceArtefactIdAndEmptyProvenanceOnlyFirstIsShown() {
        HeaderGroup headerGroup = new HeaderGroup(EMPTY_FIELD, EMPTY_FIELD, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                                  DISPLAY_FROM, DISPLAY_TO
        );
        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });

        assertEquals(
            "x-source-artefact-id is mandatory however an empty value is provided",
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationJudgementTypeAndEmptyDateTo() {

        HeaderGroup headerGroup = new HeaderGroup(
            PROVENANCE,
            SOURCE_ARTEFACT_ID,
            ArtefactType.JUDGEMENT,
            SENSITIVITY,
            LANGUAGE,
            DISPLAY_FROM,
            null
        );
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-to Field is required for artefact type JUDGEMENT",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationListTypeAndEmptyDateTo() {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ArtefactType.LIST, SENSITIVITY,
                                                  LANGUAGE,
                                                  DISPLAY_FROM, null
        );
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-to Field is required for artefact type LIST",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationOutcomeTypeAndEmptyDateTo() {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ArtefactType.OUTCOME, SENSITIVITY,
                                                  LANGUAGE,
                                                  DISPLAY_FROM, null
        );
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-to Field is required for artefact type OUTCOME",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationListTypeAndEmptyDateFrom() {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ArtefactType.LIST, SENSITIVITY,
                                                  LANGUAGE, null, DISPLAY_TO
        );
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-from Field is required for artefact type LIST",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationJudgementTypeAndEmptyDateFrom() {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ArtefactType.JUDGEMENT, SENSITIVITY,
                                                  LANGUAGE, null, DISPLAY_TO
        );
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-from Field is required for artefact type JUDGEMENT",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationOutcomeTypeAndEmptyDateFrom() {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ArtefactType.OUTCOME, SENSITIVITY,
                                                  LANGUAGE, null, DISPLAY_TO
        );
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-from Field is required for artefact type OUTCOME",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );

    }

    @Test
    void testValidationOfStatusUpdateTypeWithNoDateFrom() {
        HeaderGroup headerGroup = new HeaderGroup(
            PROVENANCE,
            SOURCE_ARTEFACT_ID,
            ArtefactType.STATUS_UPDATES,
            SENSITIVITY,
            LANGUAGE,
            null,
            DISPLAY_TO
        );

        assertNotNull(
            validationService.validateHeaders(headerGroup).getDisplayFrom(),
            NOT_NULL_MESSAGE
        );

    }

    @Test
    void testValidationOfStatusUpdateTypeWithNoDateTo() {
        HeaderGroup headerGroup = new HeaderGroup(PROVENANCE,
                                                  SOURCE_ARTEFACT_ID,
                                                  ArtefactType.STATUS_UPDATES,
                                                  SENSITIVITY,
                                                  LANGUAGE,
                                                 DISPLAY_FROM,
                                                  null
        );

        assertNull(
            validationService.validateHeaders(headerGroup).getDisplayTo(),
            "The date to header should remain null"
        );
    }
}
