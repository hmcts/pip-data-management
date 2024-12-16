package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;

public interface NonStrategicArtefactSummaryData extends ArtefactSummaryData {
    /**
     * Interface method that retrieve the data required to generate summary from the payload for non-strategic
     * publishing.
     */
    Map<String, List<Map<String, String>>> get(JsonNode payload, ListType listType);
}
