package uk.gov.hmcts.reform.pip.data.management.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that determines the extractor to use for the payload.
 */
@Component
public class PayloadExtractor {

    private final List<? extends Extractor> extractors;

    @Autowired
    public PayloadExtractor(List<? extends Extractor> extractors) {
        this.extractors = extractors;
    }

    /**
     * Method that determines which extractor to use, and extracts the search terms.
     * @return The map of search terms.
     */
    public Map<String, List<Object>> extractSearchTerms(String payload) {
        for (Extractor extractor : extractors) {
            if (extractor.isAccepted(payload)) {
                return extractor.extractSearchTerms(payload);
            }
        }
        return new HashMap<>();
    }

}
