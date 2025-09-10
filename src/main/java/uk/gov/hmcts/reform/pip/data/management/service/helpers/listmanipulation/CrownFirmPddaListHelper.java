package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownfirmpddalist.CrownFirmPddaList;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownfirmpddalist.HearingInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownfirmpddalist.SittingInfo;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CrownFirmPddaListHelper {

    private static final String FIRM_LIST = "FirmList";
    private static final String COURT_LIST = "CourtLists";
    private static final String COURT_HOUSE = "CourtHouse";
    private static final String COURT_HOUSE_NAME = "CourtHouseName";
    private static final String COURT_HOUSE_ADDRESS = "CourtHouseAddress";
    private static final String COURT_HOUSE_TELEPHONE = "CourtHouseTelephone";
    private static final String COURT_ROOM_NUMBER = "CourtRoomNumber";
    private static final String SITTING_DATE = "SittingDate";
    private static final String SITTING_AT = "SittingAt";
    private static final String SITTINGS = "Sittings";
    private static final String HEARINGS = "Hearings";
    private static final String JUDICIARY = "Judiciary";
    private static final String JUDGE = "Judge";
    private static final String JUSTICE = "Justice";
    private static final String CASE_NUMBER_CATH = "CaseNumberCaTH";
    private static final String HEARING_DETAILS = "HearingDetails";
    private static final String HEARING_DESCRIPTION = "HearingDescription";
    private static final String LIST_NOTE = "ListNote";
    private static final String PERSONAL_DETAILS = "PersonalDetails";
    private static final String PROSECUTION = "Prosecution";
    private static final String DEFENDANTS = "Defendants";
    private static final String COUNSEL = "Counsel";
    private static final String SOLICITOR = "Solicitor";
    private static final String PARTY = "Party";
    private static final String PROSECUTING_AUTHORITY = "ProsecutingAuthority";
    private static final String YES = "yes";

    private CrownFirmPddaListHelper() {
    }

    public static List<CrownFirmPddaList> processPayload(JsonNode payload) {
        List<CrownFirmPddaList> results = new ArrayList<>();

        payload.get(FIRM_LIST).get(COURT_LIST).forEach(courtList -> {
            CrownFirmPddaList result = new CrownFirmPddaList();

            result.setSittingDate(formatSittingDate(courtList, SITTING_DATE));
            result.setCourtName(courtList.get(COURT_HOUSE).get(COURT_HOUSE_NAME).asText());
            result.setCourtAddress(processCourtAddress(courtList));
            result.setCourtPhone(GeneralHelper.findAndReturnNodeText(courtList.get(COURT_HOUSE),
                                                                     COURT_HOUSE_TELEPHONE));
            List<SittingInfo> sittings = new ArrayList<>();

            courtList.get(SITTINGS).forEach(sitting -> {
                SittingInfo sittingInfo = new SittingInfo();
                sittingInfo.setCourtRoomNumber(sitting.get(COURT_ROOM_NUMBER).asText());
                sittingInfo.setSittingAt(formatSittingTime(sitting, SITTING_AT));
                sittingInfo.setJudgeName(formatJudgeName(sitting.get(JUDICIARY)));

                List<HearingInfo> hearings = processHearingInfo(sitting);
                sittingInfo.setHearings(hearings);
                sittings.add(sittingInfo);
            });
            result.setSittings(sittings);
            results.add(result);
        });

        return results;
    }

    public static List<String> formatAddress(JsonNode address) {
        List<String> addressLines = new ArrayList<>();
        if (address.has("Line")) {
            address.get("Line").forEach(line -> {
                if (!line.asText().isEmpty()) {
                    addressLines.add(line.asText());
                }
            });
        }

        String postcode = GeneralHelper.findAndReturnNodeText(address, "Postcode");
        if (!postcode.isEmpty()) {
            addressLines.add(postcode);
        }
        return addressLines;

    }

    private static List<String> processCourtAddress(JsonNode courtList) {
        JsonNode courtHouse = courtList.get(COURT_HOUSE);
        return courtHouse.has(COURT_HOUSE_ADDRESS) ? formatAddress(courtHouse.get(COURT_HOUSE_ADDRESS))
            : Collections.emptyList();
    }

    private static List<HearingInfo> processHearingInfo(JsonNode sitting) {
        List<HearingInfo> hearings = new ArrayList<>();

        sitting.get(HEARINGS).forEach(hearing -> {
            HearingInfo hearingInfo = new HearingInfo();
            hearingInfo.setCaseNumber(hearing.get(CASE_NUMBER_CATH).asText());
            hearingInfo.setDefendantName(hearing.has(DEFENDANTS) ? formatDefendantName(hearing) : "");
            hearingInfo.setHearingType(hearing.get(HEARING_DETAILS).get(HEARING_DESCRIPTION).asText());
            hearingInfo.setRepresentativeName(hearing.has(DEFENDANTS) ? formatRepresentativeName(hearing) : "");
            hearingInfo.setProsecutingAuthority(GeneralHelper.findAndReturnNodeText(hearing.get(PROSECUTION),
                                                                                    PROSECUTING_AUTHORITY));
            hearingInfo.setListNote(GeneralHelper.findAndReturnNodeText(hearing, LIST_NOTE));

            hearings.add(hearingInfo);
        });

        return hearings;
    }

    private static String formatSittingDate(JsonNode courtList, String nodeName) {
        String date = GeneralHelper.findAndReturnNodeText(courtList, nodeName);
        return DateHelper.convertDateFormat(date, "yyyy-MM-dd", "EEEE dd MMMM yyyy");
    }

    private static String formatSittingTime(JsonNode sitting, String nodeName) {
        String time = GeneralHelper.findAndReturnNodeText(sitting, nodeName);
        return DateHelper.convertTimeFormat(time, "HH:mm:ss");
    }

    private static String formatJudgeName(JsonNode judiciary) {
        List<String> names = new ArrayList<>();
        names.add(formatIndividualName(judiciary.get(JUDGE)));

        if (judiciary.has(JUSTICE)) {
            judiciary.get(JUSTICE).forEach(justice -> {
                names.add(formatIndividualName(justice));
            });
        }

        return GeneralHelper.convertToDelimitedString(names, ", ");
    }

    private static String formatDefendantName(JsonNode hearing) {
        List<String> names = new ArrayList<>();
        hearing.get(DEFENDANTS).forEach(
            defendant -> names.add(useMaskedNameIfRequested(defendant.get(PERSONAL_DETAILS)))
        );
        return GeneralHelper.convertToDelimitedString(names, ", ");
    }

    private static String formatRepresentativeName(JsonNode hearing) {
        List<String> names = new ArrayList<>();
        hearing.get(DEFENDANTS).forEach(defendant -> {
            if (defendant.has(COUNSEL)) {
                defendant.get(COUNSEL).forEach(counsel -> {
                    if (counsel.has(SOLICITOR)) {
                        setSolicitorName(counsel, names);
                    }
                });
            }
        });
        return GeneralHelper.convertToDelimitedString(names, ", ");
    }

    private static void setSolicitorName(JsonNode counsel, List<String> names) {
        counsel.get(SOLICITOR).forEach(solicitor -> {
            if (solicitor.has(PARTY)) {
                JsonNode party = solicitor.get(PARTY);
                if (party.has("Person")) {
                    names.add(useMaskedNameIfRequested(party.get("Person")));
                } else if (party.has("Organisation")) {
                    names.add(party.get("Organisation").get("OrganisationName").asText());
                }
            }
        });
    }

    private static String useMaskedNameIfRequested(JsonNode nameDetails) {
        return YES.equals(GeneralHelper.findAndReturnNodeText(nameDetails, "IsMasked"))
            ? GeneralHelper.findAndReturnNodeText(nameDetails, "MaskedName")
            : formatIndividualName(nameDetails.get("Name"));
    }

    private static String formatIndividualName(JsonNode individual) {
        return individual.has("CitizenNameRequestedName")
               ? individual.get("CitizenNameRequestedName").asText()
               : formatNameParts(individual);
    }

    private static String formatNameParts(JsonNode individual) {
        List<String> nameParts = new ArrayList<>();
        nameParts.add(GeneralHelper.findAndReturnNodeText(individual, "CitizenNameTitle"));
        nameParts.add(GeneralHelper.findAndReturnNodeText(individual, "CitizenNameForename"));
        nameParts.add(GeneralHelper.findAndReturnNodeText(individual, "CitizenNameSurname"));
        nameParts.add(GeneralHelper.findAndReturnNodeText(individual, "CitizenNameSuffix"));

        return GeneralHelper.convertToDelimitedString(nameParts, " ");
    }
}
