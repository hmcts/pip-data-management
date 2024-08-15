package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PartyRoleHelper {
    private static final String APPLICANT = "applicant";
    private static final String APPLICANT_REPRESENTATIVE = "applicantRepresentative";
    private static final String RESPONDENT = "respondent";
    private static final String RESPONDENT_REPRESENTATIVE = "respondentRepresentative";
    private static final String CLAIMANT = "claimant";
    private static final String CLAIMANT_REPRESENTATIVE = "claimantRepresentative";
    private static final String PROSECUTING_AUTHORITY = "prosecutingAuthority";
    private static final String DELIMITER = ", ";
    private static final String DEFENDANT = "defendant";
    private static final String DEFENDANT_REPRESENTATIVE = "defendantRepresentative";
    private static final String PARTY_ROLE = "partyRole";

    private static final String PARTY = "party";
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String INDIVIDUAL_FORENAMES = "individualForenames";
    private static final String INDIVIDUAL_MIDDLE_NAME = "individualMiddleName";
    private static final String INDIVIDUAL_SURNAME = "individualSurname";
    private static final String TITLE = "title";
    private static final String ORGANISATION_DETAILS = "organisationDetails";

    private PartyRoleHelper() {
    }

    public static void findAndManipulatePartyInformation(JsonNode node, boolean initialised) {
        StringBuilder applicant = new StringBuilder();
        StringBuilder applicantRepresentative = new StringBuilder();
        StringBuilder respondent = new StringBuilder();
        StringBuilder respondentRepresentative = new StringBuilder();
        StringBuilder claimant = new StringBuilder();
        StringBuilder claimantRepresentative = new StringBuilder();

        node.get(PARTY).forEach(party -> {
            if (!GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE).isEmpty()) {
                switch (PartyRoleMapper.convertPartyRole(party.get(PARTY_ROLE).asText())) {
                    case "APPLICANT_PETITIONER" ->
                        formatPartyDetails(party, applicant, initialised);
                    case "APPLICANT_PETITIONER_REPRESENTATIVE" ->
                        formatPartyDetails(party, applicantRepresentative, initialised);
                    case "RESPONDENT" ->
                        formatPartyDetails(party, respondent, initialised);
                    case "RESPONDENT_REPRESENTATIVE" ->
                        formatPartyDetails(party, respondentRepresentative, initialised);
                    case "CLAIMANT_PETITIONER" ->
                        formatPartyDetails(party, claimant, initialised);
                    case "CLAIMANT_PETITIONER_REPRESENTATIVE" ->
                        formatPartyDetails(party, claimantRepresentative, initialised);
                    default -> { }
                }
            }
        });

        ObjectNode nodeObj = (ObjectNode) node;
        nodeObj.put(APPLICANT,
                       GeneralHelper.trimAnyCharacterFromStringEnd(applicant.toString()));
        nodeObj.put(APPLICANT_REPRESENTATIVE,
                       GeneralHelper.trimAnyCharacterFromStringEnd(applicantRepresentative.toString()));
        nodeObj.put(RESPONDENT,
                       GeneralHelper.trimAnyCharacterFromStringEnd(respondent.toString()));
        nodeObj.put(RESPONDENT_REPRESENTATIVE,
                       GeneralHelper.trimAnyCharacterFromStringEnd(respondentRepresentative.toString()));
        nodeObj.put(CLAIMANT,
                       GeneralHelper.trimAnyCharacterFromStringEnd(claimant.toString()));
        nodeObj.put(CLAIMANT_REPRESENTATIVE,
                       GeneralHelper.trimAnyCharacterFromStringEnd(claimantRepresentative.toString()));
        nodeObj.put(PROSECUTING_AUTHORITY,
                       GeneralHelper.trimAnyCharacterFromStringEnd(respondent.toString()));
    }

    private static void formatPartyDetails(JsonNode party, StringBuilder builder, boolean initialised) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            String details = createIndividualDetails(party, initialised);
            formatPartyDetails(builder, details);
        } else if (party.has(ORGANISATION_DETAILS)) {
            String details = createOrganisationDetails(party);
            formatPartyDetails(builder, details);
        }
    }

    public static void formatPartyDetails(StringBuilder builder, String details) {
        String result = details + GeneralHelper.stringDelimiter(details, ", ");
        builder.insert(0, result);
    }

    public static String createIndividualDetails(JsonNode party, boolean initialised) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);
            String title = GeneralHelper.findAndReturnNodeText(individualDetails, TITLE);
            String forename = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_FORENAMES);
            String middleName = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_MIDDLE_NAME);
            String surname = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_SURNAME);

            if (initialised) {
                String forenameInitial = forename.isEmpty() ? "" : forename.substring(0, 1);
                return Stream.of(title, forenameInitial, surname)
                    .filter(str -> !str.isEmpty())
                    .collect(Collectors.joining(" "));
            } else {
                return Stream.of(title, forename, middleName, surname)
                    .filter(str -> !str.isEmpty())
                    .collect(Collectors.joining(" "));
            }
        }
        return "";
    }

    public static String createIndividualDetails(JsonNode party) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);
            String forenames = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_FORENAMES);
            String surname = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_SURNAME);

            return Stream.of(surname, forenames)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(", "));
        }
        return "";
    }

    public static void handleParties(JsonNode node) {
        List<String> defendants = new ArrayList<>();
        List<String> defendantRepresentatives = new ArrayList<>();
        List<String> prosecutingAuthorities = new ArrayList<>();

        if (node.has(PARTY)) {
            node.get(PARTY).forEach(party -> {
                if (!GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE).isEmpty()) {
                    switch (party.get(PARTY_ROLE).asText()) {
                        case "DEFENDANT" ->
                            defendants.add(createIndividualDetails(party));
                        case "DEFENDANT_REPRESENTATIVE" ->
                            defendantRepresentatives.add(createOrganisationDetails(party));
                        case "PROSECUTING_AUTHORITY" ->
                            prosecutingAuthorities.add(createOrganisationDetails(party));
                        default -> { }
                    }
                }
            });
        }

        ObjectNode nodeObj = (ObjectNode) node;
        nodeObj.put(DEFENDANT, String.join(DELIMITER, defendants));
        nodeObj.put(DEFENDANT_REPRESENTATIVE, String.join(DELIMITER, defendantRepresentatives));
        nodeObj.put(PROSECUTING_AUTHORITY, String.join(DELIMITER, prosecutingAuthorities));
    }

    public static String createOrganisationDetails(JsonNode party) {
        if (party.has(ORGANISATION_DETAILS)) {
            JsonNode organisationDetails = party.get(ORGANISATION_DETAILS);
            return GeneralHelper.findAndReturnNodeText(organisationDetails, "organisationName");
        }
        return "";
    }
}
