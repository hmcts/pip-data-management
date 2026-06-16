package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactSearchRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ListSearchConfigRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactSearchExtractionException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ArtefactSearchService {

    private final ArtefactSearchRepository artefactSearchRepository;
    private final ListSearchConfigRepository listSearchConfigRepository;
    private final ObjectMapper objectMapper;

    public ArtefactSearchService(ArtefactSearchRepository artefactSearchRepository,
                                 ListSearchConfigRepository listSearchConfigRepository,
                                 ObjectMapper objectMapper) {
        this.artefactSearchRepository = artefactSearchRepository;
        this.listSearchConfigRepository = listSearchConfigRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Method that retrieves the list of ArtefactSearch rows associated with a given artefact ID from the database.
     * @param artefactId The artefact ID for which the search rows need to be retrieved.
     * @return The list of ArtefactSearch rows associated with the given artefact ID.
     */
    @Transactional(readOnly = true)
    public List<ArtefactSearch> findByArtefactId(UUID artefactId) {
        List<ArtefactSearch> artefactSearchRows = artefactSearchRepository.findByArtefactId(artefactId);
        if (artefactSearchRows.isEmpty()) {
            throw new NotFoundException(
                String.format("Artefact search rows for %s do not exist", artefactId)
            );
        }
        return artefactSearchRows;
    }

    /**
     * Method that deletes the ArtefactSearch rows associated with a given artefact ID from the database.
     * @param artefactId The artefact ID for which the search rows need to be deleted.
     */
    @Transactional
    public void deleteByArtefactId(UUID artefactId) {
        List<ArtefactSearch> artefactSearchRows = artefactSearchRepository.findByArtefactId(artefactId);
        if (artefactSearchRows.isEmpty()) {
            throw new NotFoundException(
                String.format("Artefact search rows for %s do not exist", artefactId)
            );
        }
        artefactSearchRepository.deleteByArtefactId(artefactId);
    }

    /**
     * This method extracts the searchable fields from the payload based on the list search config
     * and stores them in the artefact_search table.
     * @param artefact The artefact for which the search rows need to be extracted and stored.
     * @param payload The payload from which the searchable fields need to be extracted.
     */
    @Transactional
    public void artefactSearchStore(Artefact artefact, String payload) {
        if (artefact.getArtefactId() == null || artefact.getListType() == null) {
            return;
        }

        // Extract before deleting from the table.
        // If error is thrown, existing rows are preserved.
        List<ArtefactSearch> artefactRows = extractSearchCases(artefact, payload);

        artefactSearchRepository.deleteByArtefactId(artefact.getArtefactId());

        if (!artefactRows.isEmpty()) {
            artefactSearchRepository.saveAll(artefactRows);
        }
    }


    private List<ArtefactSearch> extractSearchCases(Artefact artefact, String payload) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }

        return listSearchConfigRepository.findByListType(artefact.getListType())
            .filter(ArtefactSearchService::containsSearchableFields)
            .map(searchConfig -> parseJson(
                payload,
                artefact.getArtefactId(),
                searchConfig.getCaseNumberFieldName(),
                searchConfig.getCaseNameFieldName()
            ))
            .orElseGet(List::of);
    }


    private List<ArtefactSearch> parseJson(String payload, UUID artefactId,
                                                 String caseNumberField, String caseNameField) {
        JsonNode node;
        try {
            node = objectMapper.readTree(payload);
        } catch (JsonProcessingException error) {
            throw new ArtefactSearchExtractionException(error.getMessage());
        }

        return traverse(node, artefactId, caseNumberField, caseNameField);
    }


    private List<ArtefactSearch> traverse(JsonNode node, UUID artefactId,
                                              String caseNumberField, String caseNameField) {
        if (node == null) {
            return List.of();
        }

        List<ArtefactSearch> result = new ArrayList<>();

        if (node.isObject()) {
            String caseNumber = extractField(node, caseNumberField);
            String caseName = extractField(node, caseNameField);

            if (caseNumber != null || caseName != null) {
                result.add(ArtefactSearch.builder()
                               .artefactId(artefactId)
                               .caseNumber(caseNumber)
                               .caseName(caseName)
                               .build());
            }
        }

        node.forEach(child ->
                         result.addAll(traverse(child, artefactId, caseNumberField, caseNameField)));
        return result;
    }


    private static boolean containsSearchableFields(ListSearchConfig config) {
        return config.getCaseNumberFieldName() != null && !config.getCaseNumberFieldName().isBlank()
            || config.getCaseNameFieldName() != null && !config.getCaseNameFieldName().isBlank();
    }


    private String extractField(JsonNode node, String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return null;
        }
        String text = node.path(fieldName).asText(null);
        return (text == null || text.isBlank()) ? null : text;
    }
}



