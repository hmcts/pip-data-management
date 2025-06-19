package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.CaseInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.CaseSitting;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.DefendantInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.MagistratesStandardList;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.Offence;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"PMD.UseConcurrentHashMap"})
public final class MagistratesStandardListHelper {
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";

    private static final String CASE = "case";
    private static final String INFORMANT = "informant";
    private static final String PROSECUTING_AUTHORITY_CODE = "prosecutionAuthorityCode";
    private static final String HEARING_NUMBER = "hearingNumber";
    private static final String CASE_HEARING_CHANNEL = "caseHearingChannel";
    private static final String CASE_NUMBER = "caseNumber";
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String HEARING_TYPE = "hearingType";
    private static final String PANEL = "panel";
    private static final String CONVICTION_DATE = "convictionDate";
    private static final String ADJOURNED_DATE = "adjournedDate";

    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String GENDER = "gender";
    private static final String DOB = "dateOfBirth";
    private static final String AGE = "age";
    private static final String ADDRESS = "address";
    private static final String IN_CUSTODY = "inCustody";
    private static final String PLEA = "plea";

    private static final String OFFENCE = "offence";
    private static final String OFFENCE_TITLE = "offenceTitle";
    private static final String OFFENCE_WORDING = "offenceWording";

    private static final String FORMATTED_COURT_ROOM = "formattedSessionCourtRoom";
    private static final String TIME = "time";
    private static final String DURATION = "formattedDuration";
    private static final String DEFENDANT = "DEFENDANT";

    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String TIME_FORMAT = "h:mma";
    private static final String NEED_TO_CONFIRM = "Need to confirm";

    private MagistratesStandardListHelper() {
    }

    /**
     * Process raw JSON for Magistrates standard list to generate cases sorted by the plea date.
     * @param jsonData JSON data for the list
     * @return a map of court room/judiciary to Magistrates standard list cases
     */
    public static Map<String, List<MagistratesStandardList>> processRawListData(JsonNode jsonData, Language language) {
        Map<String, List<MagistratesStandardList>> listData = new LinkedHashMap<>();

        jsonData.get(COURT_LISTS).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(
                    session -> session.get(SITTINGS).forEach(sitting -> {
                        processSittingInfo(courtRoom, session, sitting, language);
                        List<MagistratesStandardList> cases = new ArrayList<>();
                        sitting.get(HEARING).forEach(hearing ->
                            hearing.get(CASE).forEach(hearingCase -> {
                                if (hearingCase.has(PARTY)) {
                                    CaseInfo caseInfo = buildHearingCase(hearingCase, sitting, hearing);
                                    hearingCase.get(PARTY).forEach(
                                        party -> processParty(party, sitting, caseInfo, cases)
                                    );
                                }
                            })
                        );
                        listData.computeIfAbsent(session.get(FORMATTED_COURT_ROOM).asText(), x -> new ArrayList<>())
                            .addAll(cases);
                    })
                )
            )
        );
        return listData;
    }

    private static void processSittingInfo(JsonNode courtRoom, JsonNode session, JsonNode sitting,
                                                  Language language) {
        SittingHelper.manipulatedSittingForCrime(courtRoom, session, sitting, FORMATTED_COURT_ROOM);
        SittingHelper.findAndConcatenateHearingPlatform(sitting, session);
        DateHelper.calculateDuration(sitting, language);
        DateHelper.formatStartTime(sitting, TIME_FORMAT);
    }

    private static CaseInfo buildHearingCase(JsonNode hearingCase, JsonNode sitting, JsonNode hearing) {
        CaseInfo caseInfo = new CaseInfo();

        caseInfo.setProsecutingAuthorityCode(getProsecutingAuthorityCode(hearingCase));
        caseInfo.setHearingNumber(GeneralHelper.findAndReturnNodeText(hearingCase, HEARING_NUMBER));
        caseInfo.setAttendanceMethod(GeneralHelper.findAndReturnNodeText(sitting, CASE_HEARING_CHANNEL));
        caseInfo.setCaseNumber(GeneralHelper.findAndReturnNodeText(hearingCase, CASE_NUMBER));
        caseInfo.setCaseSequenceIndicator(GeneralHelper.findAndReturnNodeText(hearingCase, CASE_SEQUENCE_INDICATOR));
        caseInfo.setAsn(NEED_TO_CONFIRM);
        caseInfo.setHearingType(GeneralHelper.findAndReturnNodeText(hearing, HEARING_TYPE));
        caseInfo.setPanel(GeneralHelper.findAndReturnNodeText(hearingCase, PANEL));
        caseInfo.setConvictionDate(formatDate(GeneralHelper.findAndReturnNodeText(hearingCase, CONVICTION_DATE)));
        caseInfo.setAdjournedDate(formatDate(GeneralHelper.findAndReturnNodeText(hearingCase, ADJOURNED_DATE)));

        return caseInfo;
    }

    private static String getProsecutingAuthorityCode(JsonNode hearingCase) {
        if (hearingCase.has(INFORMANT)) {
            return GeneralHelper.findAndReturnNodeText(hearingCase.get(INFORMANT), PROSECUTING_AUTHORITY_CODE);
        }
        return "";
    }

    private static String formatDate(String dateStr) {
        return StringUtils.isBlank(dateStr)
            ? ""
            : DateHelper.formatTimeStampToBst(dateStr, Language.ENGLISH, false, false, DATE_FORMAT);
    }

    private static void processParty(JsonNode party, JsonNode sitting, CaseInfo caseInfo,
                                     List<MagistratesStandardList> cases) {
        if (party.has(PARTY_ROLE)
            && party.has(INDIVIDUAL_DETAILS)
            && DEFENDANT.equals(party.get(PARTY_ROLE).asText())) {
            CaseSitting caseSitting = buildCaseSitting(sitting);
            caseSitting.setDefendantInfo(buildDefendantInfo(party));
            caseSitting.setCaseInfo(caseInfo);
            caseSitting.setOffences(processOffences(party));

            String defendantHeading = formatDefendantHeading(party.get(INDIVIDUAL_DETAILS),
                                                             caseSitting.getDefendantInfo().getName());
            addDefendantCase(cases, defendantHeading, caseSitting);
        }
    }

    private static CaseSitting buildCaseSitting(JsonNode sitting) {
        CaseSitting caseSitting = new CaseSitting();
        caseSitting.setSittingStartTime(GeneralHelper.findAndReturnNodeText(sitting, TIME));
        caseSitting.setSittingDuration(GeneralHelper.findAndReturnNodeText(sitting, DURATION));
        return caseSitting;
    }

    private static DefendantInfo buildDefendantInfo(JsonNode party) {
        DefendantInfo defendantInfo = new DefendantInfo();
        defendantInfo.setName(PartyRoleHelper.createIndividualDetails(party));

        JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);
        defendantInfo.setDob(GeneralHelper.findAndReturnNodeText(individualDetails, DOB));
        defendantInfo.setAge(GeneralHelper.findAndReturnNodeText(individualDetails, AGE));
        defendantInfo.setAddress(formatDefendantAddress(individualDetails));
        defendantInfo.setPlea(GeneralHelper.findAndReturnNodeText(individualDetails, PLEA));
        defendantInfo.setPleaDate(NEED_TO_CONFIRM);
        return defendantInfo;
    }

    private static String formatDefendantHeading(JsonNode individualDetails, String formattedName) {
        String gender = GeneralHelper.findAndReturnNodeText(individualDetails,GENDER);
        return formattedName
            + (gender.isEmpty() ? "" : " (" + gender + ")")
            + (isInCustody(individualDetails) ? "*" : "");

    }

    private static String formatDefendantAddress(JsonNode individualDetails) {
        return individualDetails.has(ADDRESS)
            ? CrimeListHelper.formatDefendantAddress(individualDetails.get(ADDRESS))
            : "";
    }

    private static boolean isInCustody(JsonNode individualDetails) {
        return individualDetails.has(IN_CUSTODY) && individualDetails.get(IN_CUSTODY).asBoolean();
    }

    private static List<Offence> processOffences(JsonNode party) {
        List<Offence> offences = new ArrayList<>();
        if (party.has(OFFENCE)) {
            party.get(OFFENCE).forEach(o -> {
                Offence offence = new Offence();
                offence.setOffenceTitle(GeneralHelper.findAndReturnNodeText(o, OFFENCE_TITLE));
                offence.setOffenceWording(GeneralHelper.findAndReturnNodeText(o, OFFENCE_WORDING));
                offences.add(offence);
            });
        }
        return offences;
    }

    private static void addDefendantCase(List<MagistratesStandardList> cases, String defendantHeading,
                                         CaseSitting caseSitting) {
        // Check if a case with the same defendant heading has already been stored. If so append the new case to it,
        // or else create a new case and add to the list of cases
        Optional<MagistratesStandardList> commonCase = fetchCommonDefendantCase(cases, defendantHeading);

        if (commonCase.isPresent()) {
            commonCase.get().getCaseSittings().add(caseSitting);
        } else {
            List<CaseSitting> caseSittings = new ArrayList<>();
            caseSittings.add(caseSitting);
            cases.add(new MagistratesStandardList(defendantHeading, caseSittings));
        }
    }

    private static Optional<MagistratesStandardList> fetchCommonDefendantCase(List<MagistratesStandardList> cases,
                                                                              String defendantHeading) {
        for (MagistratesStandardList c : cases) {
            if (c.getDefendantHeading().equals(defendantHeading)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }
}
