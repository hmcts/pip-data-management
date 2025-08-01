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
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.additionalInformationListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.appealReferenceNumberListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.appellantListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.caseDetailsListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.caseNameListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.caseNumberListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.caseReferenceNumberListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.caseTypeListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.dateListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.hearingTimeListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.hearingTypeListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.judgeListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.judgesListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.membersListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.timeListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.typeListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.venueListMandatoryAttributes;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConfiguration.venuePlatformListMandatoryAttributes;

@ActiveProfiles("integration-basic")
@SpringBootTest
public class NonStrategicSchemaValidationTests  extends IntegrationBasicTestBase {

    @Autowired
    ValidationService validationService;

    public static Stream<Arguments> allMandatoryAttributes() {
        return Stream.of(
                venueListMandatoryAttributes(),
                judgeListMandatoryAttributes(),
                timeListMandatoryAttributes(),
                typeListMandatoryAttributes(),
                caseNumberListMandatoryAttributes(),
                caseNameListMandatoryAttributes(),
                additionalInformationListMandatoryAttributes(),
                appellantListMandatoryAttributes(),
                appealReferenceNumberListMandatoryAttributes(),
                caseTypeListMandatoryAttributes(),
                hearingTypeListMandatoryAttributes(),
                hearingTimeListMandatoryAttributes(),
                caseDetailsListMandatoryAttributes(),
                dateListMandatoryAttributes(),
                caseReferenceNumberListMandatoryAttributes(),
                venuePlatformListMandatoryAttributes(),
                membersListMandatoryAttributes(),
                judgesListMandatoryAttributes()
            )
            .flatMap(stream -> stream);
    }

    private record TestData(HeaderGroup headerGroup, JsonNode jsonNode) {}

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JsonNode.class);
    }

    private TestData loadTestData(NonStrategicListTestConfiguration.ListTypeConfig config) throws IOException {
        try (InputStream jsonInput = getClass().getClassLoader()
            .getResourceAsStream(config.jsonFilePath())) {
            String jsonContent = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            JsonNode node = getJsonNode(jsonContent);
            HeaderGroup headerGroup = new HeaderGroup(
                NonStrategicListTestConfiguration.PROVENANCE,
                NonStrategicListTestConfiguration.SOURCE_ARTEFACT_ID,
                NonStrategicListTestConfiguration.ARTEFACT_TYPE,
                NonStrategicListTestConfiguration.SENSITIVITY,
                NonStrategicListTestConfiguration.LANGUAGE,
                NonStrategicListTestConfiguration.DISPLAY_FROM,
                NonStrategicListTestConfiguration.DISPLAY_TO,
                config.listType(),
                NonStrategicListTestConfiguration.COURT_ID,
                NonStrategicListTestConfiguration.CONTENT_DATE);
            return new TestData(headerGroup, node);
        }
    }

    @ParameterizedTest
    @MethodSource("allMandatoryAttributes")
    void shouldFailWhenVenueMissing(NonStrategicListTestConfiguration.ListTypeConfig config) throws IOException {
        TestData testData = loadTestData(config);
        if (config.parentNode().isEmpty()) {
            ((ObjectNode) testData.jsonNode().get(0)).remove(config.validationField());
        } else {
            ((ObjectNode) testData.jsonNode().get(config.parentNode()).get(0)).remove(config.validationField());
        }
        assertValidationFails(testData, config.validationField());
    }

    private void assertValidationFails(TestData testData, String fieldName) {
        String json = testData.jsonNode().toString();
        assertThatExceptionOfType(PayloadValidationException.class)
            .isThrownBy(() -> validationService.validateBody(json, testData.headerGroup(), false))
            .withMessageContaining(fieldName);
    }
}
