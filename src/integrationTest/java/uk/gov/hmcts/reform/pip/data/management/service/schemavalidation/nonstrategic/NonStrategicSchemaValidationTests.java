package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.AdditionalInformationValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CaseDetailsValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CaseNameValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CaseReferenceNumberValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.DateValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.HearingTimeValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.HearingTypeValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.JudgeValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.JudgesValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.SchemaValidationTestInput;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.TimeFormatValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.TimeTrailingSpaceValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.TimeValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.TypeValidationData;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.VenueValidationData;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.CONTENT_DATE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.ARTEFACT_TYPE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COURT_ID;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.DISPLAY_FROM;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.DISPLAY_TO;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.INVALID_MESSAGE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LANGUAGE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SENSITIVITY;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SOURCE_ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.appealReferenceNumberMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.appellantMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.caseTitleMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.caseTypeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.courtRoomMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.emailMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.ftaRespondentMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.hearingLengthMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.hearingListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.hearingMethodMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.locationMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.membersMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.modeOfHearingMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.nameToBeDisplayedMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.openJusticeStatementDetailsMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.panelMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.representativeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.respondentMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.timeEstimateMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributesValidationData.venuePlatformMandatoryAttribute;

@ActiveProfiles("integration-basic")
@SpringBootTest
@SuppressWarnings("PMD.ExcessiveImports")
class NonStrategicSchemaValidationTests  extends IntegrationBasicTestBase {

    @Autowired
    ValidationService validationService;

    public static Stream<Arguments> allMandatoryAttributes() {
        return Stream.of(
                new VenueValidationData().attributeValidationTestInputs(),
                new JudgeValidationData().attributeValidationTestInputs(),
                new TimeValidationData().attributeValidationTestInputs(),
                new TypeValidationData().attributeValidationTestInputs(),
                new CaseNameValidationData().attributeValidationTestInputs(),
                new CaseNameValidationData().attributeValidationTestInputs(),
                new AdditionalInformationValidationData().attributeValidationTestInputs(),
                new HearingTypeValidationData().attributeValidationTestInputs(),
                new HearingTimeValidationData().attributeValidationTestInputs(),
                new CaseDetailsValidationData().attributeValidationTestInputs(),
                new DateValidationData().attributeValidationTestInputs(),
                new CaseReferenceNumberValidationData().attributeValidationTestInputs(),
                new JudgesValidationData().attributeValidationTestInputs(),
                venuePlatformMandatoryAttribute(),
                membersMandatoryAttribute(),
                appellantMandatoryAttribute(),
                appealReferenceNumberMandatoryAttribute(),
                caseTypeMandatoryAttribute(),
                hearingLengthMandatoryAttribute(),
                modeOfHearingMandatoryAttribute(),
                nameToBeDisplayedMandatoryAttribute(),
                emailMandatoryAttribute(),
                courtRoomMandatoryAttribute(),
                hearingMethodMandatoryAttribute(),
                respondentMandatoryAttribute(),
                timeEstimateMandatoryAttribute(),
                panelMandatoryAttribute(),
                ftaRespondentMandatoryAttribute(),
                caseTitleMandatoryAttribute(),
                representativeMandatoryAttribute(),
                locationMandatoryAttribute()
            )
            .flatMap(stream -> stream);
    }

    public static Stream<Arguments> allMandatorySheets() {
        return Stream.of(
                hearingListMandatoryAttributes(),
                openJusticeStatementDetailsMandatoryAttributes()
            )
            .flatMap(stream -> stream);
    }

    public static Stream<Arguments> allListsWithTimeTrailingSpaceAllowed() {
        return Stream.of(
                new TimeTrailingSpaceValidationData().attributeValidationTestInputs()
            )
            .flatMap(stream -> stream);
    }

    public static Stream<Arguments> timeFormateValidationScenarios() {
        List<String> invalidTimes = List.of(
            "10.30",
            "101:30pm",
            "10:300am"
        );

        List<SchemaValidationTestInput> attributeValidationTestInputs = new TimeFormatValidationData()
            .attributeValidationTestInputs()
            .map(arg -> (SchemaValidationTestInput) arg.get()[0])
            .toList();

        return attributeValidationTestInputs.stream()
            .flatMap(config -> invalidTimes.stream()
                .map(invalidTime -> Arguments.of(config, invalidTime)));
    }

    private record TestData(HeaderGroup headerGroup, JsonNode jsonNode) {}

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JsonNode.class);
    }

    private TestData loadTestData(SchemaValidationTestInput config) throws IOException {
        try (InputStream jsonInput = getClass().getClassLoader()
            .getResourceAsStream(config.getJsonFilePath())) {
            String jsonContent = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            JsonNode node = getJsonNode(jsonContent);
            HeaderGroup headerGroup = new HeaderGroup(
                PROVENANCE,
                SOURCE_ARTEFACT_ID,
                ARTEFACT_TYPE,
                SENSITIVITY,
                LANGUAGE,
                DISPLAY_FROM,
                DISPLAY_TO,
                config.getListType(),
                COURT_ID,
                CONTENT_DATE);
            return new TestData(headerGroup, node);
        }
    }

    @ParameterizedTest
    @MethodSource("allMandatoryAttributes")
    void shouldFailWhenMissingMandatoryAttribute(SchemaValidationTestInput config) throws IOException {
        TestData testData = loadTestData(config);
        if (config.getParentNode().isEmpty()) {
            ((ObjectNode) testData.jsonNode().get(0)).remove(config.getValidationField());
        } else {
            ((ObjectNode) testData.jsonNode().get(config.getParentNode()).get(0)).remove(config.getValidationField());
        }
        assertValidationFails(testData, config.getValidationField());
    }

    @ParameterizedTest
    @MethodSource("allMandatorySheets")
    void shouldFailWhenMissingSheet(SchemaValidationTestInput config) throws IOException {
        TestData testData = loadTestData(config);
        ((ObjectNode) testData.jsonNode()).remove(config.getValidationField());
        assertValidationFails(testData, config.getValidationField());
    }

    @ParameterizedTest
    @MethodSource("allListsWithTimeTrailingSpaceAllowed")
    void shouldPassWhenTimeHasTrailingSpace(SchemaValidationTestInput config) throws IOException {
        TestData testData = loadTestData(config);
        assertValidationPass(testData);
    }

    private void assertValidationFails(TestData testData, String fieldName) {
        String json = testData.jsonNode().toString();
        assertThatExceptionOfType(PayloadValidationException.class)
            .isThrownBy(() -> validationService.validateBody(json, testData.headerGroup(), false))
            .withMessageContaining(fieldName);
    }

    private void assertValidationPass(TestData testData) {
        String json = testData.jsonNode().toString();
        assertThatNoException()
            .isThrownBy(() -> validationService.validateBody(json, testData.headerGroup(), false));
    }

    @ParameterizedTest
    @MethodSource("timeFormateValidationScenarios")
    void shouldFailWhenInvalidTimeFormat(SchemaValidationTestInput listConfig, String invalidTime) throws IOException {
        TestData testData = loadTestData(listConfig);

        JsonNode node = testData.jsonNode();
        String timeFieldPattern = "\"" + listConfig.getValidationField() + "\":\"[^\"]+\"";
        String formattedJson = node.toString().replaceAll(timeFieldPattern,
            String.format("\"%s\":\"%s\"", listConfig.getValidationField(), invalidTime));

        Assertions.assertThatExceptionOfType(PayloadValidationException.class)
            .as(INVALID_MESSAGE)
            .isThrownBy(() -> validationService.validateBody(formattedJson, testData.headerGroup(), false));
    }
}
