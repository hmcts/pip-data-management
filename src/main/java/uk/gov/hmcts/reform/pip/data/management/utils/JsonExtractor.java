package uk.gov.hmcts.reform.pip.data.management.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.config.SearchConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
            new ObjectMapper().readTree(payload);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }
}
