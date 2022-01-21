package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.EmptyRequiredHeaderException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
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
    private static final String COURT_ID = "123";
    private static final ListType LIST_TYPE = ListType.DL;
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String EMPTY_FIELD = "";
    private static final String VALIDATION_EXPECTED_MESSAGE =
        "The expected exception does not contain the correct message";
    private static final String NOT_NULL_MESSAGE = "The returned value is null, but was not expected to be.";
    private static final String REQUIRED_HEADER_EXCEPTION_MESSAGE = " is mandatory however an empty value is provided";

    private Map<String, Object> headerMap;

    @BeforeEach
    void setup() {
        headerMap = new HashMap<>();
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE);
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID);
        headerMap.put(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY);
        headerMap.put(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO);
        headerMap.put(PublicationConfiguration.COURT_ID, COURT_ID);
        headerMap.put(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE);
        headerMap.put(PublicationConfiguration.LIST_TYPE, LIST_TYPE);
    }

    @Test
    void testCreationOfPublicationEmptySourceArtefactId() {
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, EMPTY_FIELD);

        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });

        assertEquals(
            "x-source-artefact-id" + REQUIRED_HEADER_EXCEPTION_MESSAGE,
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationEmptyProvenance() {
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, EMPTY_FIELD);

        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });

        assertEquals(
            "x-provenance" + REQUIRED_HEADER_EXCEPTION_MESSAGE,
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationEmptySourceArtefactIdAndEmptyProvenanceOnlyFirstIsShown() {
        headerMap.put(PublicationConfiguration.PROVENANCE_HEADER, EMPTY_FIELD);
        headerMap.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, EMPTY_FIELD);

        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });

        assertEquals(
            "x-source-artefact-id" + REQUIRED_HEADER_EXCEPTION_MESSAGE,
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationJudgementTypeAndEmptyDateTo() {
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });
        assertEquals("x-display-to Field is required for artefact type JUDGEMENTS_AND_OUTCOMES",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationListTypeAndEmptyDateTo() {
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST);
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
    void testCreationOfPublicationListTypeAndEmptyDateFrom() {
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, null);

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
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });
        assertEquals("x-display-from Field is required for artefact type JUDGEMENTS_AND_OUTCOMES",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationOutcomeTypeAndEmptyDateFrom() {
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerMap.put(PublicationConfiguration.DISPLAY_FROM_HEADER, null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerMap);
            });
        assertEquals("x-display-from Field is required for artefact type JUDGEMENTS_AND_OUTCOMES",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testValidationOfGeneralPublicationTypeWithNoDateFrom() {
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.GENERAL_PUBLICATION);
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
    void testValidationOfGeneralPublicationTypeWithNoDateTo() {
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.GENERAL_PUBLICATION);
        headerMap.put(PublicationConfiguration.DISPLAY_TO_HEADER, null);

        int initialLength = headerMap.size();
        assertNull(
            validationService.validateHeaders(headerMap).get(PublicationConfiguration.DISPLAY_TO_HEADER),
            "The date to header should remain null"
        );
        int afterLength = validationService.validateHeaders(headerMap).size();
        assertEquals(initialLength, afterLength, DIFFERENT_SIZE_MAPS);
    }

    @Test
    void testEmptyContentDateForListThrows() {
        headerMap.put(PublicationConfiguration.CONTENT_DATE, EMPTY_FIELD);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerMap);
        });

        assertEquals(PublicationConfiguration.CONTENT_DATE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testNullContentDateForListThrows() {
        headerMap.put(PublicationConfiguration.CONTENT_DATE, null);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerMap);
        });

        assertEquals(PublicationConfiguration.CONTENT_DATE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testEmptyListTypeForListThrows() {
        headerMap.put(PublicationConfiguration.LIST_TYPE, EMPTY_FIELD);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerMap);
        });

        assertEquals(PublicationConfiguration.LIST_TYPE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testNullListTypeForListThrows() {
        headerMap.put(PublicationConfiguration.LIST_TYPE, null);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerMap);
        });

        assertEquals(PublicationConfiguration.LIST_TYPE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testEmptyContentDateForJudgementOutcomeThrows() {
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerMap.put(PublicationConfiguration.CONTENT_DATE, EMPTY_FIELD);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerMap);
        });

        assertEquals(PublicationConfiguration.CONTENT_DATE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testNullContentDateForJudgementOutcomeThrows() {
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerMap.put(PublicationConfiguration.CONTENT_DATE, null);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerMap);
        });

        assertEquals(PublicationConfiguration.CONTENT_DATE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testEmptyListTypeForJudgementOutcomeThrows() {
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerMap.put(PublicationConfiguration.LIST_TYPE, EMPTY_FIELD);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerMap);
        });

        assertEquals(PublicationConfiguration.LIST_TYPE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testNullListTypeForJudgementOutcomeThrows() {
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerMap.put(PublicationConfiguration.LIST_TYPE, null);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerMap);
        });

        assertEquals(PublicationConfiguration.LIST_TYPE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testListTypeAndContentDateNotNeededForGeneralPublication() {
        headerMap.put(PublicationConfiguration.TYPE_HEADER, ArtefactType.GENERAL_PUBLICATION);
        headerMap.put(PublicationConfiguration.LIST_TYPE, null);
        headerMap.put(PublicationConfiguration.CONTENT_DATE, null);

        assertEquals(headerMap.size(), validationService.validateHeaders(headerMap).size(),
                     DIFFERENT_SIZE_MAPS);
    }
}
