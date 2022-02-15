package uk.gov.hmcts.reform.pip.data.management.utils;

import java.util.List;
import java.util.Map;

/**
 * Interface that contains the logic for implementing search term extractors.
 */
public interface Extractor {

    /**
     * Extracts the search terms from the payload.
     * @param payload The payload to extract the search terms from.
     * @return The map containing the search term key, and the values it has found.
     */
    Map<String, List<Object>> extractSearchTerms(String payload);

    /**
     * Determines whether the payload is a valid type for the extractor.
     * @param payload The payload to validate.
     * @return A boolean determining if it is a valid type for the extractor.
     */
    boolean isAccepted(String payload);

}
