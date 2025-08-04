package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import lombok.Data;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

@Data
public final class ListTypeTestInput {
    private final ListType listType;
    private final String jsonFilePath;
    private final String validationField;
    private final String parentNode;

    public ListTypeTestInput(ListType listType, String jsonFilePath, String validationField, String parentNode) {
        this.listType = listType;
        this.jsonFilePath = jsonFilePath;
        this.validationField = validationField;
        this.parentNode = parentNode == null ? "" : parentNode;
    }

    public ListTypeTestInput(ListType listType, String jsonFilePath, String validationField) {
        this(listType, jsonFilePath, validationField, "");
    }

}
