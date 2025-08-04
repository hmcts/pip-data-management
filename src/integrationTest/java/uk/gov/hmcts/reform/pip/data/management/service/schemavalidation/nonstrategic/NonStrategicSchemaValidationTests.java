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
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.ListTypeTestInput;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
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
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.AdditionalInformationAttribute.additionalInformationMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CaseDetailsAttribute.caseDetailsMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CaseNameAttribute.caseNameMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CaseNumberAttribute.caseNumberMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CaseReferenceNumberAttribute.caseReferenceNumberMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.HearingTimeAttribute.hearingTimeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.HearingTypeAttribute.hearingTypeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.JudgeTestAttribute.judgeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.appealReferenceNumberMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.appellantMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.caseTitleMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.caseTypeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.courtRoomMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.dateMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.emailMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.ftaRespondentMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.hearingLengthMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.hearingListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.hearingMethodMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.judgesMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.locationMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.membersMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.modeOfHearingMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.nameToBeDisplayedMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.openJusticeStatementDetailsMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.panelMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.representativeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.respondentMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.timeEstimateMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.CommonMiscAttributes.venuePlatformMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.TimeFormatValidation.getListTypesWithTimeFormatValidation;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.TimeTestAttribute.timeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.TypeTestAttribute.typeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations.VenueTestAttribute.venueMandatoryAttribute;

@ActiveProfiles("integration-basic")
@SpringBootTest
@SuppressWarnings("PMD.ExcessiveImports")
class NonStrategicSchemaValidationTests  extends IntegrationBasicTestBase {

    @Autowired
    ValidationService validationService;

    public static Stream<Arguments> allMandatoryAttributes() {
        return Stream.of(
                venueMandatoryAttribute(),
                judgeMandatoryAttribute(),
                timeMandatoryAttribute(),
                typeMandatoryAttribute(),
                caseNumberMandatoryAttribute(),
                caseNameMandatoryAttribute(),
                additionalInformationMandatoryAttribute(),
                appellantMandatoryAttribute(),
                appealReferenceNumberMandatoryAttribute(),
                caseTypeMandatoryAttribute(),
                hearingTypeMandatoryAttribute(),
                hearingTimeMandatoryAttribute(),
                caseDetailsMandatoryAttribute(),
                dateMandatoryAttribute(),
                caseReferenceNumberMandatoryAttribute(),
                venuePlatformMandatoryAttribute(),
                membersMandatoryAttribute(),
                judgesMandatoryAttribute(),
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

    public static Stream<Arguments> timeFormateValidationScenarios() {
        List<String> invalidTimes = List.of(
            "10.30",
            "101:30pm",
            "10:300am"
        );

        return getListTypesWithTimeFormatValidation().stream()
            .flatMap(listConfig -> invalidTimes.stream()
                .map(invalidTime -> Arguments.of(listConfig, invalidTime)));
    }

    private record TestData(HeaderGroup headerGroup, JsonNode jsonNode) {}

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JsonNode.class);
    }

    private TestData loadTestData(ListTypeTestInput config) throws IOException {
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
    void shouldFailWhenMissingMandatoryAttribute(ListTypeTestInput config) throws IOException {
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
    void shouldFailWhenMissingSheet(ListTypeTestInput config) throws IOException {
        TestData testData = loadTestData(config);
        ((ObjectNode) testData.jsonNode()).remove(config.getValidationField());
        assertValidationFails(testData, config.getValidationField());
    }

    private void assertValidationFails(TestData testData, String fieldName) {
        String json = testData.jsonNode().toString();
        assertThatExceptionOfType(PayloadValidationException.class)
            .isThrownBy(() -> validationService.validateBody(json, testData.headerGroup(), false))
            .withMessageContaining(fieldName);
    }

    @ParameterizedTest
    @MethodSource("timeFormateValidationScenarios")
    void shouldFailWhenInvalidTimeFormat(ListTypeTestInput listConfig, String invalidTime) throws IOException {
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
