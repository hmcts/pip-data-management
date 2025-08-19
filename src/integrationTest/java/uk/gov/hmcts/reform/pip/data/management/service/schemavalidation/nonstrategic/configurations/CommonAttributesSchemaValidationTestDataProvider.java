package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CommonAttributesSchemaValidationTestDataProvider extends AbstractSchemaValidationTestDataProvider {
    private final Map<ListType, String> listTypeJsonFile;
    private final Map<ListType, List<String>> parentNodes;
    private final String mandatoryAttribute;

    public CommonAttributesSchemaValidationTestDataProvider(Map<ListType, String> listTypeJsonFile,
            Map<ListType, List<String>> parentNodes, String mandatoryAttribute) {
        super();
        this.listTypeJsonFile = listTypeJsonFile;
        this.parentNodes = parentNodes;
        this.mandatoryAttribute = mandatoryAttribute;
    }

    public CommonAttributesSchemaValidationTestDataProvider(Map<ListType, String> listTypeJsonFile,
                                                            String mandatoryAttribute) {
        this(listTypeJsonFile, new EnumMap<>(ListType.class), mandatoryAttribute);
    }

    @Override
    protected Map<ListType, String> getListTypeJsonFile() {
        return listTypeJsonFile;
    }

    @Override
    protected Map<ListType, List<String>> getListTypeJsonFileParentNodes() {
        return parentNodes;
    }

    @Override
    protected String getAttributeToValidate() {
        return mandatoryAttribute;
    }
}
