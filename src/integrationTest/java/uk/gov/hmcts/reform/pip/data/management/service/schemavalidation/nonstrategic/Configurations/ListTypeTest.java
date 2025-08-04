package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.Configurations;

import lombok.Data;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

@Data
public final class ListTypeTest {
    private final ListType listType;
    private final String jsonFilePath;
    private final String validationField;
    private final String parentNode;

    public ListTypeTest(ListType listType, String jsonFilePath, String validationField, String parentNode) {
        this.listType = listType;
        this.jsonFilePath = jsonFilePath;
        this.validationField = validationField;
        this.parentNode = parentNode == null ? "" : parentNode;
    }

    public ListTypeTest(ListType listType, String jsonFilePath, String validationField) {
        this(listType, jsonFilePath, validationField, "");
    }

}
