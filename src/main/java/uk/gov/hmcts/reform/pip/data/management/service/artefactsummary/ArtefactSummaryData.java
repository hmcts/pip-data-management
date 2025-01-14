package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface ArtefactSummaryData {

    /**
     * Interface method that retrieve the data required to generate summary from the payload.
     */
    default Map<String, List<Map<String, String>>> get(JsonNode payload) throws JsonProcessingException {
        return Collections.emptyMap();
    }
}
