package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.oparesults.Offence;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.oparesults.OpaResults;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class OpaResultsHelper {
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";

    private static final String CASE = "case";
    private static final String CASE_URN = "caseUrn";

    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String INDIVIDUAL_FORENAMES = "individualForenames";
    private static final String INDIVIDUAL_MIDDLE_NAME = "individualMiddleName";
    private static final String INDIVIDUAL_SURNAME = "individualSurname";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_NAME = "organisationName";

    private static final String OFFENCE = "offence";
    private static final String OFFENCE_TITLE = "offenceTitle";
    private static final String OFFENCE_SECTION = "offenceSection";
    private static final String DECISION = "decision";
    private static final String DECISION_DATE = "decisionDate";
    private static final String DECISION_DETAIL = "decisionDetail";
    private static final String BAIL_STATUS = "bailStatus";
    private static final String NEXT_HEARING_DATE = "nextHearingDate";
    private static final String NEXT_HEARING_LOCATION = "nextHearingLocation";
    private static final String REPORTING_RESTRICTION_DETAIL = "reportingRestrictionDetail";

    private static final String DEFENDANT = "DEFENDANT";
    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String DELIMITER = ", ";

    private OpaResultsHelper() {
    }

    public static Map<String, List<OpaResults>> processRawListData(JsonNode jsonData) {
        Map<String, List<OpaResults>> results = new ConcurrentHashMap<>();

        jsonData.get(COURT_LISTS).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(
                    session -> session.get(SITTINGS).forEach(
                        sitting -> sitting.get(HEARING).forEach(hearing ->
                            hearing.get(CASE).forEach(hearingCase -> {
                                String caseUrn = GeneralHelper.findAndReturnNodeText(hearingCase, CASE_URN);
                                hearingCase.get(PARTY).forEach(
                                    party -> processParty(party).ifPresent(p -> {
                                        p.setCaseUrn(caseUrn);
                                        results.computeIfAbsent(p.getOffences().get(0).getDecisionDate(),
                                                                x -> new ArrayList<>())
                                            .add(p);
                                    })
                                );
                            })
                        )
                    )
                )
            )
        );
        return OpaResultsSorter.sort(results);
    }

    private static Optional<OpaResults> processParty(JsonNode party) {
        if (party.has(PARTY_ROLE)
            && DEFENDANT.equals(party.get(PARTY_ROLE).asText())) {
            OpaResults opaResults = new OpaResults();
            processDefendant(party, opaResults);

            // The offence's decision date is used to group and sort the cases for the defendant. If the decision date
            // is blank the entry will be dropped
            if (StringUtils.isNotBlank(opaResults.getDefendant())
                && !opaResults.getOffences().isEmpty()
                && StringUtils.isNotBlank(opaResults.getOffences().get(0).getDecisionDate())) {
                return Optional.of(opaResults);
            }
        }
        return Optional.empty();
    }

    private static void processDefendant(JsonNode party, OpaResults opaResults) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);
            opaResults.setDefendant(formatDefendantName(individualDetails));
            opaResults.setOffences(processOffences(individualDetails));
        } else if (party.has(ORGANISATION_DETAILS)) {
            JsonNode organisationDetails = party.get(ORGANISATION_DETAILS);
            opaResults.setDefendant(GeneralHelper.findAndReturnNodeText(organisationDetails, ORGANISATION_NAME));
            opaResults.setOffences(processOffences(organisationDetails));
        }
    }

    private static String formatDefendantName(JsonNode individualDetails) {
        String forename = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_FORENAMES);
        String middleName = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_MIDDLE_NAME);
        String surname = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_SURNAME);

        String forenames = List.of(forename, middleName).stream()
            .filter(n -> !StringUtils.isBlank(n))
            .collect(Collectors.joining(" "));

        return List.of(surname, forenames).stream()
            .filter(n -> !StringUtils.isBlank(n))
            .collect(Collectors.joining(DELIMITER));
    }

    private static List<Offence> processOffences(JsonNode details) {
        List<Offence> offences = new ArrayList<>();
        if (details.has(OFFENCE)) {
            details.get(OFFENCE)
                .forEach(o -> offences.add(buildSingleOffence(o)));
        }
        return offences;
    }

    private static Offence buildSingleOffence(JsonNode offenceNode) {
        Offence offence = new Offence();
        offence.setOffenceTitle(GeneralHelper.findAndReturnNodeText(offenceNode, OFFENCE_TITLE));
        offence.setOffenceSection(GeneralHelper.findAndReturnNodeText(offenceNode, OFFENCE_SECTION));

        buildOffenceDecision(offenceNode, offence);
        offence.setBailStatus(GeneralHelper.findAndReturnNodeText(offenceNode, BAIL_STATUS));

        String nextHearingDate = "";
        if (offenceNode.has(NEXT_HEARING_DATE)) {
            nextHearingDate = DateHelper.formatTimeStampToBst(offenceNode.get(NEXT_HEARING_DATE).asText(),
                                                              Language.ENGLISH, false, false, DATE_FORMAT);
        }
        offence.setNextHearingDate(nextHearingDate);
        offence.setNextHearingLocation(GeneralHelper.findAndReturnNodeText(offenceNode, NEXT_HEARING_LOCATION));
        offence.setReportingRestrictions(
            GeneralHelper.formatNodeArray(offenceNode, REPORTING_RESTRICTION_DETAIL, DELIMITER)
        );

        return offence;
    }

    private static void buildOffenceDecision(JsonNode offenceNode, Offence offence) {
        if (offenceNode.has(DECISION)) {
            JsonNode decision = offenceNode.get(DECISION);
            String decisionDate = "";
            if (decision.has(DECISION_DATE)) {
                decisionDate = DateHelper.formatTimeStampToBst(decision.get(DECISION_DATE).asText(), Language.ENGLISH,
                                                               false, false, DATE_FORMAT);
            }
            offence.setDecisionDate(decisionDate);
            offence.setDecisionDetail(GeneralHelper.findAndReturnNodeText(decision, DECISION_DETAIL));
        } else {
            offence.setDecisionDate("");
            offence.setDecisionDetail("");
        }
    }
}
