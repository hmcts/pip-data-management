package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractSchemaValidationTestDataProvider {
    protected abstract Map<ListType, String> getListTypeJsonFile();

    protected abstract Map<ListType, List<String>> getListTypeJsonFileParentNodes();

    protected abstract String getAttributeToValidate();

    public Stream<Arguments> attributeValidationTestInputs() {
        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(
            getListTypeJsonFile(),
            getListTypeJsonFileParentNodes(),
            getAttributeToValidate()
        ).stream().map(Arguments::of);
    }
}
