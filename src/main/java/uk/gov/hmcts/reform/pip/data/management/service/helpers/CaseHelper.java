package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class CaseHelper {
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";

    private CaseHelper() {
    }

    /**
     * This method concatenate all case IDs within the linked cases.
     * @param caseInfo The case info that contains the linked cases.
     */
    public static void formatLinkedCases(JsonNode caseInfo) {
        StringBuilder formattedLinked = new StringBuilder();

        if (caseInfo.has("caseLinked")) {
            caseInfo.get("caseLinked").forEach(linkedCase -> {
                if (formattedLinked.length() != 0) {
                    formattedLinked.append(", ");
                }
                formattedLinked.append(GeneralHelper.findAndReturnNodeText(linkedCase, "caseId"));
            });
        }
        ((ObjectNode) caseInfo).put("formattedLinkedCases", formattedLinked.toString());
    }

    public static void manipulateCaseInformation(ObjectNode hearingCase) {
        hearingCase.put(
            "formattedCaseName",
            appendCaseSequenceIndicator(GeneralHelper.findAndReturnNodeText(hearingCase, "caseName"),
                                        GeneralHelper.findAndReturnNodeText(hearingCase, CASE_SEQUENCE_INDICATOR))
        );

        if (!hearingCase.has("caseType")) {
            hearingCase.put("caseType", "");
        }
    }

    public static String appendCaseSequenceIndicator(String data, String caseSequence) {
        return caseSequence.isEmpty() ? data : data + " [" + caseSequence + "]";
    }
}
