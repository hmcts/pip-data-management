package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist.CaseInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist.Defendant;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist.Offence;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist.OpaPublicList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class OpaPublicListHelper {
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";
    private static final String CASE_URN = "caseUrn";
    private static final String SCHEDULED_HEARING_DATE = "scheduledHearingDate";
    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String INDIVIDUAL_FIRST_NAME = "individualFirstName";
    private static final String INDIVIDUAL_MIDDLE_NAME = "individualMiddleName";
    private static final String INDIVIDUAL_SURNAME = "individualSurname";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_NAME = "organisationName";
    private static final String OFFENCE = "offence";
    private static final String OFFENCE_TITLE = "offenceTitle";
    private static final String OFFENCE_SECTION = "offenceSection";
    private static final String REPORTING_RESTRICTION_DETAIL = "reportingRestrictionDetail";
    private static final String DEFENDANT = "DEFENDANT";
    private static final String DATE_FORMAT = "dd/MM/yy";
    private static final String DELIMITER = ", ";

    private OpaPublicListHelper() {
    }

    public static List<OpaPublicList> formatOpaPublicList(JsonNode jsonData) {
        List<OpaPublicList> rows = new ArrayList<>();

        jsonData.get(COURT_LISTS).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(
                    session -> session.get(SITTINGS).forEach(
                        sitting -> sitting.get(HEARING).forEach(
                            hearing -> hearing.get(CASE).forEach(hearingCase -> {
                                if (hearingCase.has("party")) {

                                    processPartyRoles(hearingCase).forEach(defendant -> {

                                        CaseInfo caseInfo = buildHearingCase(hearingCase);
                                        rows.add(new OpaPublicList(caseInfo, defendant));
                                    });
                                }
                            })
                        )
                    )
                )
            )
        );
        return rows;
    }

    private static CaseInfo buildHearingCase(JsonNode hearingCase) {
        String scheduledHearingDate = DateHelper.formatTimeStampToBst(
            GeneralHelper.findAndReturnNodeText(hearingCase, SCHEDULED_HEARING_DATE),
            Language.ENGLISH, false, false, DATE_FORMAT
        );
        return new CaseInfo(
            GeneralHelper.findAndReturnNodeText(hearingCase, CASE_URN),
            scheduledHearingDate,
            GeneralHelper.formatNodeArray(hearingCase, REPORTING_RESTRICTION_DETAIL, DELIMITER)
        );
    }

    private static List<Defendant> processPartyRoles(JsonNode hearingCase) {
        List<Defendant> defendantInfo = new ArrayList<>();
        hearingCase.get(PARTY).forEach(party -> {
            if (party.has(PARTY_ROLE) && DEFENDANT.equals(party.get(PARTY_ROLE).asText())) {
                Defendant defendant = new Defendant();
                processDefendant(party, defendant);
                defendantInfo.add(defendant);
            }
        });
        defendantInfo.forEach(d -> d.setProsecutor(OpaPressListHelper.processProsecutor(hearingCase)));
        return defendantInfo;
    }

    private static void processDefendant(JsonNode party, Defendant defendant) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);

            defendant.setName(formatDefendantName(individualDetails));
            defendant.setOffences(processOffences(individualDetails));
        } else if (party.has(ORGANISATION_DETAILS)) {
            JsonNode organisationDetails = party.get(ORGANISATION_DETAILS);
            defendant.setName(
                GeneralHelper.findAndReturnNodeText(organisationDetails, ORGANISATION_NAME)
            );
            defendant.setOffences(processOffences(organisationDetails));
        }
    }

    private static String formatDefendantName(JsonNode individualDetails) {
        String firstName = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_FIRST_NAME);
        String middleName = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_MIDDLE_NAME);
        String surname = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_SURNAME);

        return Stream.of(firstName, middleName, surname)
            .filter(n -> !StringUtils.isBlank(n))
            .collect(Collectors.joining(" "));
    }

    private static List<Offence> processOffences(JsonNode detailsNode) {
        List<Offence> offences = new ArrayList<>();
        if (detailsNode.has(OFFENCE)) {
            detailsNode.get(OFFENCE).forEach(o -> {
                Offence offence = new Offence();
                offence.setOffenceTitle(GeneralHelper.findAndReturnNodeText(o, OFFENCE_TITLE));
                offence.setOffenceSection(GeneralHelper.findAndReturnNodeText(o, OFFENCE_SECTION));
                offence.setOffenceReportingRestriction(
                    GeneralHelper.formatNodeArray(o, REPORTING_RESTRICTION_DETAIL, DELIMITER)
                );
                offences.add(offence);
            });
        }
        return offences;
    }

}
