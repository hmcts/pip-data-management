package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownfirmpddalist.CrownFirmPddaList;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownfirmpddalist.HearingInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownfirmpddalist.SittingInfo;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"PMD.CognitiveComplexity", "PMD.AvoidDeeplyNestedIfStmts", "PMD.LiteralsFirstInComparisons"})
public final class CrownFirmPddaListHelper {

    private static final String FIRM_LIST = "FirmList";
    private static final String COURT_LIST = "CourtLists";
    private static final String COURT_HOUSE = "CourtHouse";
    private static final String COURT_HOUSE_NAME = "CourtHouseName";
    private static final String COURT_HOUSE_TELEPHONE = "CourtHouseTelephone";
    private static final String COURT_ROOM_NUMBER = "CourtRoomNumber";
    private static final String SITTINGS = "Sittings";
    private static final String HEARINGS = "Hearings";
    private static final String CASE_NUMBER_CATH = "CaseNumberCaTH";
    private static final String HEARING_DETAILS = "HearingDetails";
    private static final String HEARING_DESCRIPTION = "HearingDescription";
    private static final String LIST_NOTE = "ListNote";
    private static final String PERSONAL_DETAILS = "PersonalDetails";
    private static final String PROSECUTION = "Prosecution";
    private static final String DEFENDANT = "Defendants";
    private static final String SITTING_DATE = "SittingDate";
    private static final String SITTING_AT = "SittingAt";
    private static final String COUNSEL = "Counsel";
    private static final String SOLICITOR = "Solicitor";
    private static final String PARTY = "Party";
    private static final String PROSECUTING_AUTHORITY = "ProsecutingAuthority";

    private CrownFirmPddaListHelper() {
    }

    public static List<CrownFirmPddaList> crownFirmPddaListFormatted(JsonNode artefact) {
        List<CrownFirmPddaList> results = new ArrayList<>();

        artefact.get(FIRM_LIST).get(COURT_LIST).forEach(courtList -> {
            CrownFirmPddaList result = new CrownFirmPddaList();
            result.setSittingDate(formatSittingDate(courtList, SITTING_DATE));
            result.setCourtName(courtList.get(COURT_HOUSE).get(COURT_HOUSE_NAME).asText());
            result.setCourtAddress(CrimeListHelper.formatAddress(courtList.get(COURT_HOUSE)));
            result.setCourtPhone(GeneralHelper.findAndReturnNodeText(courtList.get(COURT_HOUSE),
                                                                     COURT_HOUSE_TELEPHONE));
            List<SittingInfo> sittings = new ArrayList<>();

            courtList.get(SITTINGS).forEach(sitting -> {
                SittingInfo sittingInfo = new SittingInfo();
                sittingInfo.setCourtRoomNumber(sitting.get(COURT_ROOM_NUMBER).asText());
                sittingInfo.setSittingAt(formatSittingTime(sitting, SITTING_AT));
                sittingInfo.setJudgeName(formatJudgeName(sitting.get("Judiciary")));
                List<HearingInfo> hearings = new ArrayList<>();
                sitting.get(HEARINGS).forEach(hearing -> {
                    HearingInfo hearingInfo = new HearingInfo();
                    hearingInfo.setCaseNumber(hearing.get(CASE_NUMBER_CATH).asText());
                    hearingInfo.setDefendantName(formatDefendantName(hearing));
                    hearingInfo.setHearingType(hearing.get(HEARING_DETAILS).get(HEARING_DESCRIPTION).asText());
                    hearingInfo.setRepresentativeName(formatRepresentativeName(hearing));
                    hearingInfo.setProsecutingAuthority(GeneralHelper.findAndReturnNodeText(hearing.get(PROSECUTION),
                        PROSECUTING_AUTHORITY));
                    hearingInfo.setListNote(GeneralHelper.findAndReturnNodeText(hearing, LIST_NOTE));

                    hearings.add(hearingInfo);
                });
                sittingInfo.setHearings(hearings);
                sittings.add(sittingInfo);
            });
            result.setSittings(sittings);
            results.add(result);
        });

        return results;
    }

    private static String formatSittingTime(JsonNode sitting, String nodeName) {
        String time = GeneralHelper.findAndReturnNodeText(sitting, nodeName);
        return DateHelper.convertTimeFormat(time, "HH:mm:ss");
    }

    private static String formatSittingDate(JsonNode courtList, String nodeName) {
        String date = GeneralHelper.findAndReturnNodeText(courtList, nodeName);
        return DateHelper.convertDateFormat(date, "yyyy-MM-dd");
    }

    private static String formatDefendantName(JsonNode hearing) {
        if (hearing.has(DEFENDANT)) {
            List<String> names = new ArrayList<>();
            hearing.get(DEFENDANT).forEach(defendant -> {
                JsonNode nameDetails =  defendant.get(PERSONAL_DETAILS);
                names.add(useMaskedNameIfRequested(nameDetails));
            });
            return GeneralHelper.convertToDelimitedString(names, ", ");
        }
        return "";
    }

    private static String formatJudgeName(JsonNode judiciary) {
        List<String> names = new ArrayList<>();

        names.add(formatIndividualName(judiciary.get("Judge")));

        if (judiciary.has("Justice")) {
            judiciary.get("Justice").forEach(justice -> {
                names.add(formatIndividualName(justice));
            });
        }

        return GeneralHelper.convertToDelimitedString(names, ", ");
    }

    private static String formatIndividualName(JsonNode individual) {
        if (individual.has("CitizenNameRequestedName")) {
            return GeneralHelper.findAndReturnNodeText(individual, "CitizenNameRequestedName");
        }

        List<String> name = new ArrayList<>();
        name.add(GeneralHelper.findAndReturnNodeText(individual, "CitizenNameTitle"));
        name.add(GeneralHelper.findAndReturnNodeText(individual, "CitizenNameForename"));
        name.add(GeneralHelper.findAndReturnNodeText(individual, "CitizenNameSurname"));
        name.add(GeneralHelper.findAndReturnNodeText(individual, "CitizenNameSuffix"));

        return GeneralHelper.convertToDelimitedString(name, " ");
    }

    private static String formatRepresentativeName(JsonNode hearing) {
        List<String> names = new ArrayList<>();
        if (hearing.has(DEFENDANT)) {
            hearing.get(DEFENDANT).forEach(defendant -> {
                if (defendant.has(COUNSEL)) {
                    defendant.get(COUNSEL).forEach(counsel -> {
                        if (counsel.has(SOLICITOR)) {
                            counsel.get(SOLICITOR).forEach(solicitor -> {
                                if (solicitor.has(PARTY)) {
                                    if (solicitor.get(PARTY).has("Person")) {
                                        names.add(useMaskedNameIfRequested(solicitor.get(PARTY).get("Person")));
                                    }
                                    if (solicitor.get(PARTY).has("Organisation")) {
                                        names.add(solicitor.get(PARTY)
                                                      .get("Organisation")
                                                      .get("OrganisationName")
                                                      .asText());
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }

        return GeneralHelper.convertToDelimitedString(names, ", ");
    }

    private static String useMaskedNameIfRequested(JsonNode nameDetails) {
        if (GeneralHelper.findAndReturnNodeText(nameDetails, "IsMasked").equals("yes")) {
            return GeneralHelper.findAndReturnNodeText(nameDetails, "MaskedName");
        } else {
            return formatIndividualName(nameDetails.get("Name"));
        }
    }

}
