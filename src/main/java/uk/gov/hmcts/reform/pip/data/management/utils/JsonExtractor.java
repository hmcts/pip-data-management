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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.config.SearchConfiguration;
import uk.gov.hmcts.reform.pip.data.management.config.ValidationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ValidationException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class JsonExtractor implements Extractor {

    private final Configuration jsonConfiguration;

    private final SearchConfiguration searchConfiguration;

    private final JsonSchema masterSchema;
    private final JsonSchema dailyCauseListSchema;

    @Autowired
    public JsonExtractor(SearchConfiguration searchConfiguration, ValidationConfiguration validationConfiguration) {
        this.searchConfiguration = searchConfiguration;

        jsonConfiguration = Configuration.defaultConfiguration()
            .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .addOptions(Option.SUPPRESS_EXCEPTIONS)
            .addOptions(Option.ALWAYS_RETURN_LIST);

        try (InputStream masterFile = this.getClass().getClassLoader()
            .getResourceAsStream(validationConfiguration.masterSchema);

             InputStream dailyCauseListFile = this.getClass().getClassLoader()
                 .getResourceAsStream(validationConfiguration.dailyCauseList)) {

            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            masterSchema = schemaFactory.getSchema(masterFile);
            dailyCauseListSchema = schemaFactory.getSchema(dailyCauseListFile);

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
            List<Object> objects = searchValues.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (objects.size() != 0) {
                searchTermsMap.put(key, objects);
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
    public List<String> validate(Artefact artefact, String payload) {
        try {
            List<String> errors = new ArrayList<>();

            JsonNode json = new ObjectMapper().readTree(payload);
            masterSchema.validate(json).forEach(vm ->  errors.add(vm.getMessage()));

            if (artefact.getListType() != null && artefact.getListType().equals(ListType.CIVIL_DAILY_CAUSE_LIST)) {
                dailyCauseListSchema.validate(json).forEach(vm ->  errors.add(vm.getMessage()));
            }

            return errors;

        } catch (IOException exception) {
            throw new ValidationException("Error while reading JSON Payload");
        }
    }

}
