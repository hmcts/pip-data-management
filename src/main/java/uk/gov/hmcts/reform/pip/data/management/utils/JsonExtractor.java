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

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JsonExtractor implements Extractor {

    Configuration jsonConfiguration;

    SearchConfiguration searchConfiguration;

    @Autowired
    public JsonExtractor(SearchConfiguration searchConfiguration) {
        this.searchConfiguration = searchConfiguration;

        jsonConfiguration = Configuration.defaultConfiguration()
            .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .addOptions(Option.SUPPRESS_EXCEPTIONS)
            .addOptions(Option.ALWAYS_RETURN_LIST);
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
            // validate json file with JSON schema
            return true;
        } catch (IOException exception) {
            return false;
        }
    }


    public List<String> validate2(String payload) {
        List<String> errors = new ArrayList<>();
        try {
            JsonNode json = new ObjectMapper().readTree(payload);
            ObjectMapper om = new ObjectMapper().setDateFormat(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH));
            InputStream schemaFile = this.getClass().getClassLoader()
                .getResourceAsStream("schema3-draft.json");
            InputStream inputFile = this.getClass().getClassLoader()
                .getResourceAsStream("test.json");
            JsonNode jsonMap = om.readTree(inputFile);
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            JsonSchema schema = schemaFactory.getSchema(schemaFile);
            Set<ValidationMessage> validationResult = schema.validate(jsonMap);

            if (!validationResult.isEmpty()) {
                validationResult.forEach(vm ->  errors.add(vm.getMessage()));
            }
        } catch (IOException exception) {
            errors.add(exception.getMessage());
        }
        return errors;
    }

    @Override
    public List<String> validate(String payload) {
        try {
            //List<String> errors = new ArrayList<>();
            ObjectMapper om = new ObjectMapper().setDateFormat(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH));
            InputStream schemaFile = this.getClass().getClassLoader()
                .getResourceAsStream("schema3-draft.json");
            InputStream inputFile = this.getClass().getClassLoader()
                .getResourceAsStream("test.json");
            JsonNode jsonMap = om.readTree(inputFile);
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            JsonSchema schema = schemaFactory.getSchema(schemaFile);
            Set<ValidationMessage> validationResult = schema.validate(jsonMap);

            return null;
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }
}
