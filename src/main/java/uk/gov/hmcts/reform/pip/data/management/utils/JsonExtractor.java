package uk.gov.hmcts.reform.pip.data.management.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.config.SearchConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Component
@Slf4j
public class JsonExtractor implements Extractor {

    private final Configuration jsonConfiguration;

    private final SearchConfiguration searchConfiguration;

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

        extractUsingJPath(searchTermsMap, payload);
        extractUsingAdvancedJPath(searchTermsMap, payload);

        return searchTermsMap;
    }

    private void extractUsingJPath(Map<String, List<Object>> searchTermsMap, String payload) {
        searchConfiguration.getSearchValues().forEach((key, value) -> {
            DocumentContext jsonPayload = JsonPath
                .using(jsonConfiguration)
                .parse(payload);

            List<Object> searchValues = jsonPayload.read(value);
            List<Object> objects = searchValues.stream().filter(Objects::nonNull).toList();
            if (!objects.isEmpty()) {
                searchTermsMap.put(key, objects);
            }
        });
    }

    private void extractUsingAdvancedJPath(Map<String, List<Object>> searchTermsMap, String payload) {
        String hearingsPath = searchConfiguration.getAdvancedSearchConfig().getHearingsPath();

        DocumentContext jsonPayload = JsonPath
            .using(jsonConfiguration)
            .parse(payload);

        List<Object> hearings = jsonPayload.read(hearingsPath);


        List<Object> parties = new ArrayList<>();
        hearings.forEach(hearing -> {

            try {
                DocumentContext hearingsPayload = JsonPath
                    .using(jsonConfiguration)
                    .parse(new ObjectMapper().writeValueAsString(hearing));

                String casesPath = searchConfiguration.getAdvancedSearchConfig().getCasesPath();
                String partiesSurnamePath = searchConfiguration.getAdvancedSearchConfig().getPartiesSurnamePath();
                String partiesOrganisationPath = searchConfiguration.getAdvancedSearchConfig().getPartiesOrgNamePath();

                List<Object> caseValues = hearingsPayload.read(casesPath);
                List<Object> partySurnameValues = hearingsPayload.read(partiesSurnamePath);
                List<Object> partyOrganisationValues = hearingsPayload.read(partiesOrganisationPath);

                JSONObject json = new JSONObject();
                json.put("cases", caseValues);
                json.put("parties", Stream.concat(partySurnameValues.stream(), partyOrganisationValues.stream()).toList());

                parties.add(json);
            } catch (JsonProcessingException e) {
                log.warn(writeLog("Failed to extract parties from JSON payload"));
            }

        });

        searchTermsMap.put("parties", parties);
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

}
