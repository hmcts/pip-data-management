package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.ListTypeTest;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.CONTENT_DATE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.AdditionalInformationTestConfigurations.additionalInformationMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.CaseDetailsTestConfigurations.caseDetailsMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.CaseNameTestConfigurations.caseNameMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.CaseNumberTestConfigurations.caseNumberMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.CaseReferenceNumberTestConfigurations.caseReferenceNumberMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.HearingTimeTestConfigurations.hearingTimeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.HearingTypeTestConfigurations.hearingTypeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.JudgeTestConfigurations.judgeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.appealReferenceNumberMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.appellantMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.caseTitleMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.caseTypeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.courtRoomMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.dateMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.emailMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.ftaRespondentMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.hearingLengthMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.hearingListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.hearingMethodMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.judgesMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.locationMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.membersMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.modeOfHearingMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.nameToBeDisplayedMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.openJusticeStatementDetailsMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.panelMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.representativeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.respondentMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.timeEstimateMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.NonStrategicListTestConfiguration.venuePlatformMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.TimeTestConfigurations.timeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.TypeTestConfigurations.typeMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations.VenueTestConfigurations.venueMandatoryAttribute;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.ARTEFACT_TYPE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.COURT_ID;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.DISPLAY_FROM;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.DISPLAY_TO;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.LANGUAGE;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SENSITIVITY;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.SOURCE_ARTEFACT_ID;

@ActiveProfiles("integration-basic")
@SpringBootTest
public class NonStrategicSchemaValidationTests  extends IntegrationBasicTestBase {

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

    private record TestData(HeaderGroup headerGroup, JsonNode jsonNode) {}

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JsonNode.class);
    }

    private TestData loadTestData(ListTypeTest config) throws IOException {
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
    void shouldFailWhenMissingMandatoryAttribute(ListTypeTest config) throws IOException {
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
    void shouldFailWhenMissingSheet(ListTypeTest config) throws IOException {
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
}
