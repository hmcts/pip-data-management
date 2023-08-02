package uk.gov.hmcts.reform.pip.data.management.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@SuppressWarnings("PMD.TooManyMethods")
class ValidationServiceTest {

    @Autowired
    ValidationService validationService;

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
    private static final String VALIDATION_EXPECTED_MESSAGE =
        "The expected exception does not contain the correct message";
    private static final String NOT_NULL_MESSAGE = "The returned value is null, but was not expected to be.";
    private static final String UNKNOWN_EXCEPTION = "Unknown exception when opening the paylaod file";

    private HeaderGroup headerGroup;

    @BeforeEach
    void setup() {
        headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                      DISPLAY_FROM, DISPLAY_TO, LIST_TYPE, COURT_ID, CONTENT_DATE);
    }

    @Test
    void testCreationOfPublicationEmptyProvenance() {
        headerGroup.setProvenance("");

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-provenance is mandatory however an empty value is provided",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationNullProvenance() {
        headerGroup.setProvenance(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-provenance is mandatory however an empty value is provided",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationEmptyCourtId() {
        headerGroup.setCourtId("");

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-court-id is mandatory however an empty value is provided",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationNullCourtId() {
        headerGroup.setCourtId(null);

        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                validationService.validateHeaders(headerGroup);
            });
        assertEquals("x-court-id is mandatory however an empty value is provided",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
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
    void testDefaultSensitivity() {
        headerGroup.setSensitivity(null);

        assertEquals(SENSITIVITY, validationService.validateHeaders(headerGroup).getSensitivity(),
                     "Sensitivity should match");
    }

    @Test
    void testDefaultSensitivityIsNotOverwritten() {
        headerGroup.setSensitivity(Sensitivity.PRIVATE);

        assertEquals(Sensitivity.PRIVATE, validationService.validateHeaders(headerGroup).getSensitivity(),
                     "Sensitivity should match");
    }

    @Test
    void testEmptyFileThrows() {
        MultipartFile file = new MockMultipartFile("test", (byte[]) null);
        FlatFileException ex = assertThrows(FlatFileException.class, () -> {
            validationService.validateBody(file);
        });

        assertEquals("Empty file provided, please provide a valid file", ex.getMessage(),
                     VALIDATION_EXPECTED_MESSAGE);
    }

    @Test
    void testValidateMasterSchemaWithErrors() {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/badJsonPayload.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            assertThrows(PayloadValidationException.class, () ->
                             validationService.validateBody(text, headerGroup),
                               "Valid JSON string marked as not valid");
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    @Test
    void testExceptionWhenValidatingPayload() {
        PayloadValidationException payloadValidationException = assertThrows(PayloadValidationException.class, () ->
            validationService.validateBody("abcd", headerGroup), "Validation exception not thrown "
                                                                                 + "when value not JSON");

        assertEquals("Error while parsing JSON Payload", payloadValidationException.getMessage(),
                     "JSON Payload message does not match expected exception");

    }

    @Test
    void testValidateMasterSchemaWithoutErrors() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/jsonPayload.json")) {
            headerGroup.setListType(ListType.SJP_PRESS_REGISTER);
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            assertDoesNotThrow(() -> validationService.validateBody(text, headerGroup),
                               "Valid master schema marked as invalid");
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testValidateWithoutErrorsForValidArtefact(ListType listType, String resource) throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader().getResourceAsStream(resource)) {
            headerGroup.setListType(listType);
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, headerGroup),
                               String.format("Valid %s marked as invalid", listType));
        }
    }

    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(ListType.SJP_PUBLIC_LIST,
                         "mocks/sjp-public-list/sjpPublicList.json"),
            Arguments.of(ListType.SJP_PRESS_LIST,
                         "mocks/sjp-press-list/sjpPressList.json"),
            Arguments.of(ListType.SJP_DELTA_PRESS_LIST,
                         "mocks/sjp-press-list/sjpPressList.json"),
            Arguments.of(ListType.CROWN_DAILY_LIST,
                         "mocks/crown-daily-list/crownDailyList.json"),
            Arguments.of(ListType.CROWN_FIRM_LIST,
                         "mocks/crown_firm_list/crownFirmList.json"),
            Arguments.of(ListType.CROWN_WARNED_LIST,
                         "mocks/crown-warned-list/crownWarnedList.json"),
            Arguments.of(ListType.MAGISTRATES_PUBLIC_LIST,
                         "mocks/magistrates_public_list/magistratesPublicList.json"),
            Arguments.of(ListType.MAGISTRATES_STANDARD_LIST,
                         "mocks/magistrates-standard-list/magistratesStandardList.json"),
            Arguments.of(ListType.CIVIL_DAILY_CAUSE_LIST,
                         "mocks/civil-daily-cause-list/civilDailyCauseList.json"),
            Arguments.of(ListType.FAMILY_DAILY_CAUSE_LIST,
                         "mocks/family-daily-cause-list/familyDailyCauseList.json"),
            Arguments.of(ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST,
                         "mocks/civil-and-family-cause-list/civilAndFamilyDailyCauseList.json"),
            Arguments.of(ListType.COP_DAILY_CAUSE_LIST,
                         "mocks/cop-daily-cause-list/copDailyCauseList.json"),
            Arguments.of(ListType.ET_FORTNIGHTLY_PRESS_LIST,
                         "mocks/et-fortnightly-press-list/etFortnightlyPressList.json"),
            Arguments.of(ListType.ET_DAILY_LIST,
                         "mocks/et-daily-list/etDailyList.json"),
            Arguments.of(ListType.SSCS_DAILY_LIST,
                         "mocks/sscs-daily-list/sscsDailyList.json"),
            Arguments.of(ListType.SSCS_DAILY_LIST_ADDITIONAL_HEARINGS,
                         "mocks/sscs-daily-list/sscsDailyList.json"),
            Arguments.of(ListType.IAC_DAILY_LIST,
                         "mocks/iac-daily-list/iacDailyList.json"),
            Arguments.of(ListType.CARE_STANDARDS_LIST,
                         "mocks/care-standards-list/careStandardsList.json"),
            Arguments.of(ListType.PRIMARY_HEALTH_LIST,
                         "mocks/primary-health-list/primaryHealthList.json")
        );
    }
}
