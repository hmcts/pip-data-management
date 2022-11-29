package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.SjpPublicListRepository;
import uk.gov.hmcts.reform.pip.data.management.models.SjpPublicList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Test service to paginate a json list.
 */
@Slf4j
@Service
public class PaginationService {

    private static final String ACCUSED = "ACCUSED";
    private static final String PROSECUTOR = "PROSECUTOR";

    private final SjpPublicListRepository sjpPublicListRepository;
    private final PublicationService publicationService;

    @Autowired
    public PaginationService(SjpPublicListRepository sjpPublicListRepository, PublicationService publicationService) {
        this.sjpPublicListRepository = sjpPublicListRepository;
        this.publicationService = publicationService;
    }

    public Page<SjpPublicList> getPaginatedSjpPublicList(UUID artefactId, Pageable pageable) throws JsonProcessingException {
        // Request the JSON Payload
        String rawJson = publicationService.getPayloadByArtefactId(artefactId);

        // Turn it in to JSON Node
        JsonNode topLevelNode = new ObjectMapper().readTree(rawJson);

        // Process the raw list into the repository
        List<SjpPublicList> modelData = processRawListData(topLevelNode);

        // Add it to the pagination object
        modelData.forEach(sjpPublicList -> {
            if(sjpPublicList != null) {
                sjpPublicListRepository.save(sjpPublicList);
            }
        });
        return sjpPublicListRepository.findAll(pageable);
    }


    private List<SjpPublicList> processRawListData(JsonNode data) {
        List<SjpPublicList> sjpCases = new ArrayList<>();

        data.get("courtLists").forEach(courtList -> {
            courtList.get("courtHouse").get("courtRoom").forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    session.get("sittings").forEach(sitting -> {
                        sitting.get("hearing").forEach(hearing -> {
                            Optional<SjpPublicList> sjpCase = constructSjpCase(hearing);
                            if (sjpCase.isPresent()) {
                                sjpCases.add(sjpCase.get());
                            }
                        });
                    });
                });
            });
        });
        return sjpCases;
    }

    private static Optional<SjpPublicList> constructSjpCase(JsonNode hearing) {
        Triple<String, String, String> parties = getCaseParties(hearing.get("party"));
        String offenceTitle = getOffenceTitle(hearing.get("offence"));

        if (StringUtils.isNotBlank(parties.getLeft())
            && StringUtils.isNotBlank(parties.getMiddle())
            && StringUtils.isNotBlank(parties.getRight())
            && StringUtils.isNotBlank(offenceTitle)) {
            return Optional.of(
                new SjpPublicList(parties.getLeft(), parties.getMiddle(), offenceTitle, parties.getRight())
            );
        }

        return Optional.empty();
    }

    private static Triple<String, String, String> getCaseParties(JsonNode partiesNode) {
        String name = null;
        String postcode = null;
        String prosecutor = null;

        for (JsonNode party : partiesNode) {
            String role = party.get("partyRole").asText();
            if (ACCUSED.equals(role)) {
                JsonNode individual = party.get("individualDetails");
                name = buildNameField(individual);
                postcode = individual.get("address").get("postCode").asText();
            } else if (PROSECUTOR.equals(role)) {
                prosecutor = party.get("organisationDetails").get("organisationName").asText();
            }
        }
        return Triple.of(name, postcode, prosecutor);
    }

    private static String getOffenceTitle(JsonNode offence) {
        StringBuilder output = new StringBuilder();
        Iterator<JsonNode> offenceIterator = offence.elements();
        while (offenceIterator.hasNext()) {
            JsonNode currentOffence = offenceIterator.next();
            if (output.length() == 0) {
                output.append(currentOffence.get("offenceTitle").asText());
            } else {
                output.append(", ").append(currentOffence.get("offenceTitle").asText());
            }
        }
        return output.toString();
    }

    private static String buildNameField(JsonNode individual) {
        return individual.get("individualForenames").textValue()
            + " "
            + individual.get("individualSurname").textValue();
    }
}
