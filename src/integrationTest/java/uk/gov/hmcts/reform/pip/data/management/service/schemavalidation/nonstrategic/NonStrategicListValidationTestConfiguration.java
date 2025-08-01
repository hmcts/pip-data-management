package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.PayloadValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.HeaderGroup;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationBasicTestBase;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@ActiveProfiles("integration-basic")
@SpringBootTest
public class NonStrategicListValidationTestConfiguration extends IntegrationBasicTestBase {

    @Autowired
    ValidationService validationService;

    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().plusDays(1);
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String PROVENANCE = "provenance";
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String COURT_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String PARENT_JSON_FILE_PATH =
        "data/non-strategic/administrative-court-daily-cause-list/"
            + "administrativeCourtDailyCauseList.json";

    private TestData loadTestData(ListType listType, String jsonListFile) throws IOException {
        String jsonFile = listType.name() + ".json";
        try (InputStream jsonInput = getClass().getClassLoader()
            .getResourceAsStream(jsonFile)) {
            String jsonContent = new String(jsonInput.readAllBytes(), StandardCharsets.UTF_8);
            JsonNode node = getJsonNode(jsonContent);
            HeaderGroup headerGroup = new HeaderGroup(PROVENANCE, SOURCE_ARTEFACT_ID, ARTEFACT_TYPE, SENSITIVITY, LANGUAGE,
                                                      DISPLAY_FROM, DISPLAY_TO,
                                                      listType, COURT_ID,
                                                      CONTENT_DATE);
            return new TestData(headerGroup, node);
        }
    }

    // Test record to hold test data
    private record TestData(HeaderGroup headerGroup, JsonNode jsonNode) {}

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JsonNode.class);
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {
        "BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST",
        "BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST"
    }, mode = EnumSource.Mode.INCLUDE)
    void shouldFailWhenVenueMissing(ListType listType) throws IOException {
        TestData testData = loadTestData(listType);
        ((ObjectNode) testData.jsonNode().get(0)).remove("venue");

        assertValidationFails(testData, "venue");
    }

    private void assertValidationFails(TestData testData, String fieldName) {
        String json = testData.jsonNode().toString();
        assertThatExceptionOfType(PayloadValidationException.class)
            .isThrownBy(() -> validationService.validateBody(json, testData.headerGroup(), false))
            .withMessageContaining(fieldName);
    }
}
