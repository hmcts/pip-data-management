package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class CaseHelperTest {
    private static final String CASE_ID = "caseId";
    private static final String CASE_NUMBER = "caseNumber";
    private static final String CASE_NAME = "caseName";
    private static final String CASE_TYPE = "caseType";
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String CASE_LINKED = "caseLinked";
    private static final String FORMATTED_CASE_NAME = "formattedCaseName";
    private static final String FORMATTED_LINKED_CASES = "formattedLinkedCases";
    private static final String TEST_DATA = "testData";
    private static final String LINKED_CASES_ERROR_MESSAGE = "Linked cases do not match";
    private static final String CASE_DATA_ERROR_MESSAGE = "Case data do not match";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testFormatMultipleLinkedCases() {
        ObjectNode caseIdNode1 = MAPPER.createObjectNode();
        caseIdNode1.put(CASE_ID, "123");

        ObjectNode caseIdNode2 = MAPPER.createObjectNode();
        caseIdNode2.put(CASE_ID, "456");

        ObjectNode caseIdNode3 = MAPPER.createObjectNode();
        caseIdNode3.put(CASE_ID, "789");

        ArrayNode caseIdArrayNode = MAPPER.createArrayNode();
        caseIdArrayNode.add(caseIdNode1);
        caseIdArrayNode.add(caseIdNode2);
        caseIdArrayNode.add(caseIdNode3);

        ObjectNode linkedCasesNode = MAPPER.createObjectNode();
        linkedCasesNode.set(CASE_LINKED, caseIdArrayNode);

        ArrayNode caseNode = MAPPER.createArrayNode();
        caseNode.add(linkedCasesNode);

        CaseHelper.formatLinkedCases(caseNode.get(0));
        assertThat(caseNode.get(0).get(FORMATTED_LINKED_CASES).asText())
            .as(LINKED_CASES_ERROR_MESSAGE)
            .isEqualTo("123, 456, 789");
    }

    @Test
    void testFormatSingleLinkedCase() {
        ObjectNode caseIdNode = MAPPER.createObjectNode();
        caseIdNode.put(CASE_ID, "999");

        ArrayNode caseIdArrayNode = MAPPER.createArrayNode();
        caseIdArrayNode.add(caseIdNode);

        ObjectNode linkedCasesNode = MAPPER.createObjectNode();
        linkedCasesNode.set(CASE_LINKED, caseIdArrayNode);

        ArrayNode caseNode = MAPPER.createArrayNode();
        caseNode.add(linkedCasesNode);

        CaseHelper.formatLinkedCases(caseNode.get(0));
        assertThat(caseNode.get(0).get(FORMATTED_LINKED_CASES).asText())
            .as(LINKED_CASES_ERROR_MESSAGE)
            .isEqualTo("999");
    }

    @Test
    void testFormatEmptyLinkedCase() {
        ObjectNode caseNumberNode = MAPPER.createObjectNode();
        caseNumberNode.put(CASE_NUMBER, "999");

        ArrayNode caseNode = MAPPER.createArrayNode();
        caseNode.add(caseNumberNode);

        CaseHelper.formatLinkedCases(caseNode.get(0));
        assertThat(caseNode.get(0).get(FORMATTED_LINKED_CASES).asText())
            .as(LINKED_CASES_ERROR_MESSAGE)
            .isEmpty();
    }

    @Test
    void testCaseNameWithoutCaseSequenceIndicator() {
        ObjectNode caseNode = MAPPER.createObjectNode();
        caseNode.put(CASE_NAME, TEST_DATA);

        CaseHelper.manipulateCaseInformation(caseNode);
        assertThat(caseNode.get(FORMATTED_CASE_NAME).asText())
            .as(CASE_DATA_ERROR_MESSAGE)
            .isEqualTo(TEST_DATA);
    }

    @Test
    void testCaseNameWithCaseSequenceIndicator() {
        ObjectNode caseNode = MAPPER.createObjectNode();
        caseNode.put(CASE_NAME, TEST_DATA);
        caseNode.put(CASE_SEQUENCE_INDICATOR, "1 of 2");

        CaseHelper.manipulateCaseInformation(caseNode);
        assertThat(caseNode.get(FORMATTED_CASE_NAME).asText())
            .as(CASE_DATA_ERROR_MESSAGE)
            .isEqualTo(TEST_DATA + " [1 of 2]");
    }

    @Test
    void testEmptyCaseType() {
        ObjectNode caseNode = MAPPER.createObjectNode();
        CaseHelper.manipulateCaseInformation(caseNode);
        assertThat(caseNode.get(CASE_TYPE).asText())
            .as("Case type does not match")
            .isEmpty();
    }

    @Test
    void testAppendCaseSequenceIndicatorWhenPresent() {
        assertThat(CaseHelper.appendCaseSequenceIndicator(TEST_DATA, "2 of 3"))
            .as(CASE_DATA_ERROR_MESSAGE)
            .isEqualTo(TEST_DATA + " [2 of 3]");
    }

    @Test
    void testAppendCaseSequenceIndicatorWhenMissing() {
        assertThat(CaseHelper.appendCaseSequenceIndicator(TEST_DATA, ""))
            .as(CASE_DATA_ERROR_MESSAGE)
            .isEqualTo(TEST_DATA);
    }
}
