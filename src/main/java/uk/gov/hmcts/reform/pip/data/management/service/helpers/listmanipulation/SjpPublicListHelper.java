package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.SjpPublicList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SjpPublicListHelper {
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String INDIVIDUAL_FORENAMES = "individualForenames";
    private static final String INDIVIDUAL_SURNAME = "individualSurname";

    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_ADDRESS = "organisationAddress";
    private static final String ORGANISATION_NAME = "organisationName";

    private static final String ADDRESS = "address";
    private static final String POSTCODE = "postCode";

    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";

    private static final String OFFENCE = "offence";
    private static final String OFFENCE_TITLE = "offenceTitle";

    private static final String ACCUSED = "ACCUSED";
    private static final String PROSECUTOR = "PROSECUTOR";

    private SjpPublicListHelper() {
    }

    public static Optional<SjpPublicList> constructSjpCase(JsonNode hearing) {
        Triple<String, String, String> parties = getCaseParties(hearing.get(PARTY));
        String offenceTitle = getOffenceTitle(hearing.get(OFFENCE));

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
            String role = party.get(PARTY_ROLE).asText();
            if (ACCUSED.equals(role)) {
                name = getAccusedName(party);
                postcode = getAccusedPostcode(party);
            } else if (PROSECUTOR.equals(role)) {
                prosecutor = party.get(ORGANISATION_DETAILS).get(ORGANISATION_NAME).asText();
            }
        }
        return Triple.of(name, postcode, prosecutor);
    }

    private static String getAccusedName(JsonNode party) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            return buildNameField(party.get(INDIVIDUAL_DETAILS));
        }
        return PartyRoleHelper.createOrganisationDetails(party);
    }

    private static String buildNameField(JsonNode individual) {
        String forename = GeneralHelper.findAndReturnNodeText(individual, INDIVIDUAL_FORENAMES);
        String surname = GeneralHelper.findAndReturnNodeText(individual, INDIVIDUAL_SURNAME);

        return Stream.of(forename, surname)
            .filter(str -> !str.isEmpty())
            .collect(Collectors.joining(" "));
    }

    private static String getAccusedPostcode(JsonNode party) {
        if (party.has(INDIVIDUAL_DETAILS) && party.get(INDIVIDUAL_DETAILS).has(ADDRESS)) {
            return GeneralHelper.findAndReturnNodeText(party.get(INDIVIDUAL_DETAILS).get(ADDRESS), POSTCODE);
        } else if (party.has(ORGANISATION_DETAILS)
            && party.get(ORGANISATION_DETAILS).has(ORGANISATION_ADDRESS)) {
            return GeneralHelper.findAndReturnNodeText(party.get(ORGANISATION_DETAILS).get(ORGANISATION_ADDRESS),
                                                       POSTCODE);
        }
        return "";
    }

    private static String getOffenceTitle(JsonNode offence) {
        StringBuilder output = new StringBuilder();
        Iterator<JsonNode> offenceIterator = offence.elements();
        while (offenceIterator.hasNext()) {
            JsonNode currentOffence = offenceIterator.next();
            if (output.length() == 0) {
                output.append(currentOffence.get(OFFENCE_TITLE).asText());
            } else {
                output.append(", ").append(currentOffence.get(OFFENCE_TITLE).asText());
            }
        }
        return output.toString();
    }
}
