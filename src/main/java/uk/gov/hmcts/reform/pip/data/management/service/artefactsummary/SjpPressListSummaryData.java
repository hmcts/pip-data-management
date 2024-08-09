package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SjpPressListSummaryData implements ArtefactSummaryData {
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_ADDRESS = "organisationAddress";
    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";
    private static final String CASE = "case";
    private static final String OFFENCE = "offence";

    private static final String ACCUSED_VALUE = "ACCUSED";
    private static final String PROSECUTOR_VALUE = "PROSECUTOR";

    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        payload.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(hearing -> {
                            Map<String, String> fields = ImmutableMap.<String, String>builder()
                                .putAll(buildPartyFields(hearing.get(PARTY)))
                                .put("Case reference", processCaseUrns(hearing.get(CASE)))
                                .put("Offence", processOffences(hearing.get(OFFENCE)))
                                .build();
                            summaryCases.add(fields);
                        })
                    )
                )
            )
        );
        return Collections.singletonMap(null, summaryCases);
    }

    private String processOffences(JsonNode offences) {
        List<String> offenceTitles = new ArrayList<>();
        offences.forEach(offence -> offenceTitles.add(offence.get("offenceTitle").asText()));
        return GeneralHelper.convertToDelimitedString(offenceTitles, ", ");
    }

    private String processCaseUrns(JsonNode cases) {
        List<String> caseUrns = new ArrayList<>();
        cases.forEach(hearingCase -> caseUrns.add(hearingCase.get("caseUrn").asText()));
        return GeneralHelper.convertToDelimitedString(caseUrns, ", ");
    }

    private Map<String, String> buildPartyFields(JsonNode parties) {
        AtomicReference<String> accused = new AtomicReference<>("");
        AtomicReference<String> postcode = new AtomicReference<>("");
        AtomicReference<String> prosecutor = new AtomicReference<>("");

        parties.forEach(party -> {
            if (!GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE).isEmpty()) {
                if (ACCUSED_VALUE.equals(party.get(PARTY_ROLE).asText())) {
                    accused.set(getAccusedName(party));
                    postcode.set(getAccusedPostcode(party));
                } else if (PROSECUTOR_VALUE.equals(party.get(PARTY_ROLE).asText())) {
                    prosecutor.set(PartyRoleHelper.createOrganisationDetails(party));
                }
            }
        });

        return ImmutableMap.of(
            "Name", accused.get(),
            "Prosecutor", prosecutor.get(),
            "Postcode", postcode.get()
        );
    }

    private String getAccusedName(JsonNode party) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            return PartyRoleHelper.createIndividualDetails(party, false);
        }
        return PartyRoleHelper.createOrganisationDetails(party);
    }

    private String getAccusedPostcode(JsonNode party) {
        if (party.has(INDIVIDUAL_DETAILS) && party.get(INDIVIDUAL_DETAILS).has("address")) {
            return GeneralHelper.findAndReturnNodeText(
                party.get(INDIVIDUAL_DETAILS).get("address"),
                "postCode"
            );
        } else if (party.has(ORGANISATION_DETAILS)
            && party.get(ORGANISATION_DETAILS).has(ORGANISATION_ADDRESS)) {
            return GeneralHelper.findAndReturnNodeText(
                party.get(ORGANISATION_DETAILS).get(ORGANISATION_ADDRESS),
                "postCode"
            );
        }
        return "";
    }
}
