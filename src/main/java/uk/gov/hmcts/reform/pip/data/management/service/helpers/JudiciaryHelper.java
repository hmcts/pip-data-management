package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JudiciaryHelper {
    private static final String JUDICIARY = "judiciary";

    private JudiciaryHelper() {
    }

    public static String findAndManipulateJudiciary(JsonNode judiciaryNode) {
        AtomicReference<StringBuilder> presidingJudiciary = new AtomicReference<>(new StringBuilder());
        List<String> judiciaries = new ArrayList<>();

        if (judiciaryNode.has(JUDICIARY)) {
            judiciaryNode.get(JUDICIARY).forEach(judiciary -> {
                String johKnownAs = GeneralHelper.findAndReturnNodeText(judiciary, "johKnownAs");
                if (Boolean.TRUE.toString().equals(GeneralHelper.findAndReturnNodeText(judiciary, "isPresiding"))) {
                    presidingJudiciary.set(new StringBuilder(johKnownAs));
                } else {
                    judiciaries.add(johKnownAs);
                }
            });
        }

        judiciaries.add(0, String.valueOf(presidingJudiciary.get()));
        return GeneralHelper.convertToDelimitedString(judiciaries, ", ");
    }

    public static String findAndManipulateJudiciaryForCrime(JsonNode judiciaryNode) {
        AtomicReference<StringBuilder> presidingJudiciary = new AtomicReference<>(new StringBuilder());
        List<String> judiciaries = new ArrayList<>();

        if (judiciaryNode.has(JUDICIARY)) {
            judiciaryNode.get(JUDICIARY).forEach(judiciary -> {
                String johTitle = GeneralHelper.findAndReturnNodeText(judiciary, "johTitle");
                String johNameSurname = GeneralHelper.findAndReturnNodeText(judiciary, "johNameSurname");
                String judgeName = Stream.of(johTitle, johNameSurname)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(" "));
                if (Boolean.TRUE.toString().equals(GeneralHelper.findAndReturnNodeText(judiciary, "isPresiding"))) {
                    presidingJudiciary.set(new StringBuilder(judgeName));
                } else {
                    judiciaries.add(judgeName);
                }
            });
        }

        judiciaries.add(0, String.valueOf(presidingJudiciary.get()));
        return GeneralHelper.convertToDelimitedString(judiciaries, ", ");
    }
}
