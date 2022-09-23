package uk.gov.hmcts.reform.pip.data.management.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTest;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.FlatFileException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTest.class})
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
        FlatFileException ex = assertThrows(FlatFileException.class, () -> {
            validationService.validateBody(new MockMultipartFile("test", (byte[]) null));
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
                             validationService.validateBody(text, ListType.MAGISTRATES_PUBLIC_LIST),
                               "Valid JSON string marked as not valid");
        } catch (IOException exception) {
            fail(UNKNOWN_EXCEPTION);
        }
    }

    @Test
    void testExceptionWhenValidatingPayload() {
        PayloadValidationException payloadValidationException = assertThrows(PayloadValidationException.class, () ->
            validationService.validateBody("abcd", ListType.CIVIL_DAILY_CAUSE_LIST), "Validation exception not thrown "
                                                                                 + "when value not JSON");

        assertEquals("Error while parsing JSON Payload", payloadValidationException.getMessage(),
                     "JSON Payload message does not match expected exception");

    }

    @Test
    void testValidateMasterSchemaWithoutErrors() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/jsonPayload.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.CROWN_WARNED_LIST),
                               "Valid master schema marked as invalid");
        }
    }

    @Test
    void testValidateWithoutErrorsWhenArtefactIsCivilDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/civil-daily-cause-list/civilDailyCauseList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.CIVIL_DAILY_CAUSE_LIST),
                               "Valid daily cause list marked as invalid");
        }
    }

    @Test
    void testValidateWithoutErrorsWhenArtefactIsFamilyDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/family-daily-cause-list/familyDailyCauseList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.FAMILY_DAILY_CAUSE_LIST),
                               "Valid daily cause list marked as invalid");
        }
    }

    @Test
    void testValidateWithoutErrorsWhenArtefactIsCivilAndFamilyDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream(
                "mocks/civil-and-family-cause-list/civilAndFamilyDailyCauseList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST),
                               "Valid civil and family daily cause list marked as invalid");
        }
    }

    @Test
    void testValidateWithoutErrorsWhenArtefactIsSjpPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/sjp-public-list/sjpPublicList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.SJP_PUBLIC_LIST),
                               "Valid sjp public list marked as invalid");
        }
    }

    @Test
    void testValidateWithoutErrorsWhenArtefactIsSjpPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/sjp-press-list/sjpPressList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.SJP_PRESS_LIST),
                               "Valid sjp press list marked as valid");
        }
    }

    @Test
    void testValidateWithoutErrorsWhenArtefactIsSscsDailyList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/sscs-daily-list/sscsDailyList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.SSCS_DAILY_LIST),
                               "Valid sscs daily list marked as valid");
        }
    }

    @Test
    void testValidateWithoutErrorsWhenArtefactIsCopDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/cop-daily-cause-list/copDailyCauseList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.COP_DAILY_CAUSE_LIST),
                               "Valid cop daily cause list marked as valid");
        }
    }

    @Test
    void testValidateWithoutErrorsWhenArtefactIsCrimeDailyCauseList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/crown-daily-list/crownDailyList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(
                () -> validationService.validateBody(text, ListType.CROWN_DAILY_LIST),
                "Valid crown daily list marked as valid"
            );
        }
    }

    @Test
    void testValidateWithoutErrorsWhenArtefactIsMagsPublicList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/magistrates_public_list/magistratesPublicList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.MAGISTRATES_PUBLIC_LIST),
                               "Valid sscs daily list marked as valid");
        }
    }

    @Test
    void testValidateWithoutErrorsWhenArtefactIsCrownFirmList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/crown_firm_list/crownFirmList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(
                () -> validationService.validateBody(text, ListType.CROWN_FIRM_LIST),
                "Valid crown firm list marked as valid"
            );
        }
    }

    @Test
    void testValidateWithoutErrorWhenArtefactIsMagistratesStandardList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/magistrates-standard-list/magistratesStandardList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.MAGISTRATES_STANDARD_LIST),
                               "Valid magistrates standard list marked as valid");
        }
    }

    @Test
    void testValidateWithoutErrorWhenArtefactIsEtFortnightlyPressList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/et-fortnightly-press-list/etFortnightlyPressList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(
                () -> validationService.validateBody(text, ListType.ET_FORTNIGHTLY_PRESS_LIST),
                "Valid et fortnightly press list marked as valid"
            );
        }
    }

    @Test
    void testValidateWithoutErrorWhenArtefactIsIacDailyList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/iac-daily-list/iacDailyList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.IAC_DAILY_LIST),
                               "Valid iac daily list marked as valid");
        }
    }

    @Test
    void testValidateWithoutErrorWhenArtefactIsCareStandardsList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/care-standards-list/careStandardsList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.CARE_STANDARDS_LIST),
                               "Valid care standards list marked as valid");
        }
    }

    @Test
    void testValidateWithoutErrorWhenArtefactIsPrimaryHealthList() throws IOException {
        try (InputStream jsonInput = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/primary-health-list/primaryHealthList.json")) {
            String text = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);

            assertDoesNotThrow(() -> validationService.validateBody(text, ListType.PRIMARY_HEALTH_LIST),
                               "Valid primary health list marked as valid");
        }
    }
}
