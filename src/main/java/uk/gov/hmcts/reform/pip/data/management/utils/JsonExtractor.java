package uk.gov.hmcts.reform.pip.data.management.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.config.SearchConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Component
@Slf4j
public class JsonExtractor implements Extractor {

    private final Configuration jsonConfiguration;

    private final SearchConfiguration searchConfiguration;

    private final ObjectMapper objectMapper;

    @Autowired
    public JsonExtractor(SearchConfiguration searchConfiguration) {
        this.searchConfiguration = searchConfiguration;

        jsonConfiguration = Configuration.defaultConfiguration()
            .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .addOptions(Option.SUPPRESS_EXCEPTIONS)
            .addOptions(Option.ALWAYS_RETURN_LIST);

        objectMapper = new ObjectMapper();

    }

    @Override
    public Map<String, List<Object>> extractSearchTerms(String payload) {
        Map<String, List<Object>> searchTermsMap = new ConcurrentHashMap<>();

        extractUsingJPath(searchTermsMap, payload);
        extractPartiesUsingJPath(searchTermsMap, payload);

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

    private void extractPartiesUsingJPath(Map<String, List<Object>> searchTermsMap, String payload) {
        String hearingsPath = searchConfiguration.getPartySearchConfig().getHearingsPath();

        DocumentContext jsonPayload = JsonPath
            .using(jsonConfiguration)
            .parse(payload);

        List<Object> hearings = jsonPayload.read(hearingsPath);
        List<Object> parties = new ArrayList<>();
        hearings.forEach(hearing -> {

            try {
                DocumentContext hearingsPayload = JsonPath
                    .using(jsonConfiguration)
                    .parse(objectMapper.writeValueAsString(hearing));

                String casesPath = searchConfiguration.getPartySearchConfig().getCasesPath();
                String partiesIndividualDetailsPath = searchConfiguration.getPartySearchConfig()
                    .getPartiesIndividualDetailsPath();
                String partiesOrganisationPath = searchConfiguration.getPartySearchConfig().getPartiesOrgNamePath();

                List<Object> caseValues = hearingsPayload.read(casesPath);
                List<Object> partyIndividualDetailsValues = hearingsPayload.read(partiesIndividualDetailsPath);
                List<Object> partyOrganisationValues = hearingsPayload.read(partiesOrganisationPath);

                JSONObject partiesJson = new JSONObject();
                partiesJson.put("cases", caseValues);
                partiesJson.put("organisations", partyOrganisationValues);
                partiesJson.put("individuals", constructIndividualParties(partyIndividualDetailsValues));
                parties.add(partiesJson);
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

    private JSONArray constructIndividualParties(List<Object> partyIndividualDetailsValues) {
        JSONArray individualJsonArray = new JSONArray();
        partyIndividualDetailsValues.forEach(i -> {
            if (i != null) {
                Object forename = ((LinkedHashMap) i).get("individualForenames");
                Object middleName = ((LinkedHashMap) i).get("individualMiddleName");
                Object surname = ((LinkedHashMap) i).get("individualSurname");

                Map<String, Object> individuals = new ConcurrentHashMap<>();
                if (forename != null) {
                    individuals.put("forename", forename);
                }
                if (middleName != null) {
                    individuals.put("middleName", middleName);
                }
                if (surname != null) {
                    individuals.put("surname", surname);
                }

                individualJsonArray.add(individuals);
            }
        });
        return individualJsonArray;
    }

}
