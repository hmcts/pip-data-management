package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.EmptyRequiredHeaderException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
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
    private static final String COURT_ID = "123";
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String EMPTY_FIELD = "";
    private static final String VALIDATION_EXPECTED_MESSAGE =
        "The expected exception does not contain the correct message";
    private static final String NOT_NULL_MESSAGE = "The returned value is null, but was not expected to be.";
    private static final String REQUIRED_HEADER_EXCEPTION_MESSAGE = " is mandatory however an empty value is provided";

    private HeaderGroup headerGroup;

    @BeforeEach
    void setup() {
        headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                      DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, COURT_ID, CONTENT_DATE);
    }

    @Test
    void testCreationOfPublicationEmptySourceArtefactId() {
        headerGroup.setSourceArtefactId(EMPTY_FIELD);
        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });

        assertEquals(
            "x-source-artefact-id" + REQUIRED_HEADER_EXCEPTION_MESSAGE,
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationEmptyProvenance() {
        headerGroup.setProvenance(EMPTY_FIELD);
        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });

        assertEquals(
            "x-provenance" + REQUIRED_HEADER_EXCEPTION_MESSAGE,
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationEmptySourceArtefactIdAndEmptyProvenanceOnlyFirstIsShown() {
        headerGroup.setProvenance(EMPTY_FIELD);
        headerGroup.setSourceArtefactId(EMPTY_FIELD);

        HeaderValidationException emptyRequestHeaderException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });

        assertEquals(
            "x-source-artefact-id" + REQUIRED_HEADER_EXCEPTION_MESSAGE,
            emptyRequestHeaderException.getMessage(),
            VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationJudgementTypeAndEmptyDateTo() {
        headerGroup.setType(ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerGroup.setDisplayTo(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-to Field is required for artefact type JUDGEMENTS_AND_OUTCOMES",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationListTypeAndEmptyDateTo() {
        headerGroup.setType(ArtefactType.LIST);
        headerGroup.setDisplayTo(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-to Field is required for artefact type LIST",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationListTypeAndEmptyDateFrom() {
        headerGroup.setDisplayFrom(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-from Field is required for artefact type LIST",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationJudgementAndOutcomeTypeAndEmptyDateFrom() {
        headerGroup.setType(ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerGroup.setDisplayFrom(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-display-from Field is required for artefact type JUDGEMENTS_AND_OUTCOMES",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testValidationOfGeneralPublicationTypeWithNoDateFrom() {
        headerGroup.setType(ArtefactType.GENERAL_PUBLICATION);
        headerGroup.setDisplayFrom(null);
        headerGroup.setDisplayTo(null);

        assertNotNull(
            validationService.validateHeaders(headerGroup).getDisplayFrom(),
            NOT_NULL_MESSAGE
        );
    }

    @Test
    void testValidationOfGeneralPublicationTypeWithNoDateTo() {
        headerGroup.setType(ArtefactType.GENERAL_PUBLICATION);
        headerGroup.setDisplayTo(null);

        assertNull(
            validationService.validateHeaders(headerGroup).getDisplayTo(),
            "The date to header should remain null"
        );
    }

    @Test
    void testNullContentDateForListThrows() {
        headerGroup.setContentDate(null);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerGroup);
        });

        assertEquals(PublicationConfiguration.CONTENT_DATE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testNullListTypeForListThrows() {
        headerGroup.setListType(null);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerGroup);
        });

        assertEquals(PublicationConfiguration.LIST_TYPE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testNullContentDateForJudgementOutcomeThrows() {
        headerGroup.setType(ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerGroup.setContentDate(null);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerGroup);
        });

        assertEquals(PublicationConfiguration.CONTENT_DATE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testNullListTypeForJudgementOutcomeThrows() {
        headerGroup.setType(ArtefactType.JUDGEMENTS_AND_OUTCOMES);
        headerGroup.setListType(null);

        EmptyRequiredHeaderException ex = assertThrows(EmptyRequiredHeaderException.class, () -> {
            validationService.validateHeaders(headerGroup);
        });

        assertEquals(PublicationConfiguration.LIST_TYPE + REQUIRED_HEADER_EXCEPTION_MESSAGE,
                     ex.getMessage(), VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testListTypeAndContentDateNotNeededForGeneralPublication() {
        headerGroup.setType(ArtefactType.GENERAL_PUBLICATION);
        headerGroup.setListType(null);
        headerGroup.setContentDate(null);

        assertEquals(headerGroup, validationService.validateHeaders(headerGroup), "Header groups should match");
    }

    @Test
    void testSjpSetsCourtId() {
        headerGroup.setListType(ListType.SJP);
        headerGroup.setCourtId("1");

        assertEquals("0", validationService.validateHeaders(headerGroup).getCourtId(), "Court Id should match");
    }

    @Test
    void testEmptyFileThrows() {
        FlatFileException ex = assertThrows(FlatFileException.class, () -> {
            validationService.validateBody(new MockMultipartFile("test", (byte[]) null));
        });

        assertEquals("Empty file provided, please provide a valid file", ex.getMessage(),
                     VALIDATION_EXPECTED_MESSAGE);
    }
}
