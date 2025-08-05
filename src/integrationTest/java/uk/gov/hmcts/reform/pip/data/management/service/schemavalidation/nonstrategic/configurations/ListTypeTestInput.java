package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import lombok.Data;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ListTypeTestInput {
    private ListType listType;
    private String jsonFilePath;
    private String validationField;
    private String parentNode;

    public ListTypeTestInput(ListType listType, String jsonFilePath, String validationField, String parentNode) {
        this.listType = listType;
        this.jsonFilePath = jsonFilePath;
        this.validationField = validationField;
        this.parentNode = parentNode == null ? "" : parentNode;
    }

    public ListTypeTestInput(ListType listType, String jsonFilePath, String validationField) {
        this(listType, jsonFilePath, validationField, "");
    }

    public static List<ListTypeTestInput> generateListTypeTestInputsForAttribute(
        Map<ListType, String> listTypeToJsonFile,
        Map<ListType, List<String>> listTypeWithParentNodes,
        String attributeToValidate) {
        List<ListTypeTestInput> result = new ArrayList<>();

        // Process all list types with default (no parent node) configuration
        listTypeToJsonFile.forEach((listType, jsonPath) -> {
            if (!listTypeWithParentNodes.containsKey(listType)) {
                result.add(new ListTypeTestInput(listType, jsonPath, attributeToValidate));
            }
        });

        // Process list types with parent nodes
        listTypeWithParentNodes.forEach((listType, parentNodes) -> {
            String jsonPath = listTypeToJsonFile.get(listType);
            parentNodes.forEach(parentNode -> {
                result.add(new ListTypeTestInput(listType, jsonPath, attributeToValidate, parentNode));
            });
        });

        return result;
    }

}
