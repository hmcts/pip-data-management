package uk.gov.hmcts.reform.pip.data.management.service.csvprocessing;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public interface CsvData {
    /**
     * Interface method that retrieves the headers required to generate CSV.
     */
    default List<String> getHeaders(Map<String, Object> languageResource) {
        return List.of();
    }

    /**
     * Interface method that retrieves the rows required to generate CSV.
     */
    default List<List<String>> getRows(JsonNode json, Map<String, String> metadata,
                                       Map<String, Object> languageResource) {
        return List.of();
    }
}
