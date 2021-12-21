package uk.gov.hmcts.reform.pip.data.management.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.config.SearchConfiguration;
import uk.gov.hmcts.reform.pip.data.management.config.ValidationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JsonExtractor implements Extractor {

    private final Configuration jsonConfiguration;

    private final SearchConfiguration searchConfiguration;

    private final JsonSchema schema;

    @Autowired
    public JsonExtractor(SearchConfiguration searchConfiguration, ValidationConfiguration validationConfiguration) {
        this.searchConfiguration = searchConfiguration;
        jsonConfiguration = Configuration.defaultConfiguration()
            .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .addOptions(Option.SUPPRESS_EXCEPTIONS)
            .addOptions(Option.ALWAYS_RETURN_LIST);
        try (InputStream schemaFile = this.getClass().getClassLoader()
            .getResourceAsStream(validationConfiguration.schemaJsonFile);) {
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            schema = schemaFactory.getSchema(schemaFile);
        } catch (Exception exception) {
            throw new ValidationException(String.join(exception.getMessage()));
        }

    }

    @Override
    public Map<String, List<Object>> extractSearchTerms(String payload) {
        Map<String, List<Object>> searchTermsMap = new ConcurrentHashMap<>();
        searchConfiguration.getSearchValues().forEach((key, value) -> {

            DocumentContext jsonPayload = JsonPath
                .using(jsonConfiguration)
                .parse(payload);

            List<Object> searchValues = jsonPayload.read(value);
            if (!searchValues.stream().allMatch(Objects::isNull)) {
                searchTermsMap.put(key, searchValues);
            }
        });

        return searchTermsMap;
    }

    @Override
    public boolean isAccepted(String payload) {
        try {
            // test if the file is json format
            new ObjectMapper().readTree(payload);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    @Override
    public List<String> validate(String payload) {
        List<String> errors = new ArrayList<>();
        try {
            JsonNode json = new ObjectMapper().readTree(payload);
            Set<ValidationMessage> validationResult = schema.validate(json);

            if (!validationResult.isEmpty()) {
                validationResult.forEach(vm ->  errors.add(vm.getMessage()));
            }
        } catch (IOException exception) {
            errors.add(exception.getMessage());
        }
        return errors;
    }
}
