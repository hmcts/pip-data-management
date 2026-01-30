package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.CourtRoom;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.GroupedPartyMatters;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.Matter;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.MatterMetadata;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.PartyInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.Offence;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.JudiciaryHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MagistratesStandardListHelper {
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_HOUSE_NAME = "courtHouseName";
    private static final String LJA = "lja";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String APPLICATION = "application";

    private static final String CASE = "case";
    private static final String CASE_HEARING_CHANNEL = "channel";
    private static final String CASE_URN = "caseUrn";
    private static final String APPLICATION_REFERENCE = "applicationReference";
    private static final String APPLICATION_TYPE = "applicationType";
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String HEARING_TYPE = "hearingType";
    private static final String PANEL = "panel";
    private static final String CONVICTION_DATE = "convictionDate";
    private static final String ADJOURNED_DATE = "adjournedDate";
    private static final String OFFENCE_LEGISLATION = "offenceLegislation";

    private static final String PARTY = "party";
    private static final String SUBJECT = "subject";
    private static final String PARTY_ROLE = "partyRole";
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_NAME = "organisationName";
    private static final String GENDER = "gender";
    private static final String DOB = "dateOfBirth";
    private static final String AGE = "age";
    private static final String ADDRESS = "address";
    private static final String ORGANISATION_ADDRESS = "organisationAddress";
    private static final String ASN = "asn";
    private static final String IN_CUSTODY = "inCustody";
    private static final String PLEA = "plea";
    private static final String PLEA_DATE = "pleaDate";

    private static final String OFFENCE = "offence";
    private static final String OFFENCE_TITLE = "offenceTitle";
    private static final String OFFENCE_WORDING = "offenceWording";
    private static final String OFFENCE_CODE = "offenceCode";
    private static final String OFFENCE_MAX_PENALTY = "offenceMaxPen";

    private static final String FORMATTED_COURT_ROOM = "formattedSessionCourtRoom";
    private static final String TIME = "time";
    private static final String PROSECUTING_AUTHORITY = "PROSECUTING_AUTHORITY";

    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String TIME_FORMAT = "h:mma";

    private MagistratesStandardListHelper() {
    }

    /**
     * Process raw JSON for Magistrates standard list to generate cases sorted by the plea date.
     * @param jsonData JSON data for the list
     * @return a map of court room/judiciary to Magistrates standard list cases
     */
    public static Map<String, CourtRoom> processRawListData(JsonNode jsonData) {
        Map<String, CourtRoom> listData = new LinkedHashMap<>();

        jsonData.get(COURT_LISTS).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom ->
                    courtRoom.get(SESSION).forEach(
                        session -> {
                            List<GroupedPartyMatters> groupedPartyMatters = new ArrayList<>();
                            session.get(SITTINGS).forEach(sitting -> {
                                processSittingInfo(courtRoom, session, sitting);
                                sitting.get(HEARING).forEach(hearing -> {
                                    if (hearing.has(CASE)) {
                                        hearing.get(CASE).forEach(caseObject -> {
                                            if (caseObject.has(PARTY)) {
                                                MatterMetadata matterMetadata = buildMatter(caseObject, hearing, false);
                                                caseObject.get(PARTY).forEach(
                                                    party -> processParty(
                                                        party, sitting,
                                                        matterMetadata, groupedPartyMatters
                                                    )
                                                );
                                            }
                                        });
                                    }

                                    if (hearing.has(APPLICATION)) {
                                        hearing.get(APPLICATION).forEach(applicationObject -> {
                                            if (applicationObject.has(PARTY)) {
                                                MatterMetadata applicationInfo = buildMatter(
                                                    applicationObject,
                                                    hearing, true
                                                );
                                                applicationObject.get(PARTY).forEach(
                                                    party -> processParty(
                                                        party, sitting,
                                                        applicationInfo, groupedPartyMatters
                                                    )
                                                );
                                            }
                                        });
                                    }
                                });
                            });

                            listData.computeIfAbsent(
                                session.get(FORMATTED_COURT_ROOM).asText()
                                    + courtList.get(COURT_HOUSE).get(COURT_HOUSE_NAME).asText(),
                                x -> {
                                    CourtRoom metadata = new CourtRoom();
                                    metadata.setCourtRoomName(session.get(FORMATTED_COURT_ROOM).asText());
                                    metadata.setLja(GeneralHelper.findAndReturnNodeText(
                                        courtList.get(COURT_HOUSE), LJA));
                                    metadata.setCourtHouseName(
                                        GeneralHelper.findAndReturnNodeText(
                                            courtList.get(COURT_HOUSE), COURT_HOUSE_NAME));
                                    return metadata;
                                }
                            ).getGroupedPartyMatters().addAll(groupedPartyMatters);
                        }
                    )
            )
        );
        return listData;
    }

    private static void manipulateCourtRoomName(JsonNode courtRoom, JsonNode session) {
        String courtRoomName = GeneralHelper.findAndReturnNodeText(courtRoom, "courtRoomName");
        String judiciary = JudiciaryHelper.findAndManipulateJudiciary(session);
        judiciary = !courtRoomName.isEmpty() ? courtRoomName + ": " + judiciary : judiciary;
        ((ObjectNode) session).put(FORMATTED_COURT_ROOM, judiciary);
    }

    private static void processSittingInfo(JsonNode courtRoom, JsonNode session, JsonNode sitting) {
        manipulateCourtRoomName(courtRoom, session);
        SittingHelper.findAndConcatenateHearingPlatform(sitting, session);
        DateHelper.formatStartTime(sitting, TIME_FORMAT);
    }

    private static MatterMetadata buildMatter(JsonNode caseOrApplication, JsonNode hearing, boolean isApplication) {
        MatterMetadata matterMetadata = new MatterMetadata();

        matterMetadata.setProsecutingAuthority(getProsecutingAuthority(caseOrApplication));
        matterMetadata.setAttendanceMethod(GeneralHelper.formatNodeArray(hearing, CASE_HEARING_CHANNEL, ","));

        if (isApplication) {
            matterMetadata.setReference(GeneralHelper.findAndReturnNodeText(caseOrApplication, APPLICATION_REFERENCE));
            matterMetadata.setApplicationType(GeneralHelper.findAndReturnNodeText(caseOrApplication, APPLICATION_TYPE));
        } else {
            matterMetadata.setReference(GeneralHelper.findAndReturnNodeText(caseOrApplication, CASE_URN));
        }

        matterMetadata.setCaseSequenceIndicator(GeneralHelper.findAndReturnNodeText(caseOrApplication,
                                                                                    CASE_SEQUENCE_INDICATOR));
        matterMetadata.setHearingType(GeneralHelper.findAndReturnNodeText(hearing, HEARING_TYPE));
        matterMetadata.setPanel(GeneralHelper.findAndReturnNodeText(hearing, PANEL));

        return matterMetadata;
    }

    private static String getProsecutingAuthority(JsonNode hearingCase) {
        for (JsonNode party : hearingCase.get(PARTY)) {
            if (party.has(PARTY_ROLE) && party.get(PARTY_ROLE).asText().equals(PROSECUTING_AUTHORITY)) {
                return GeneralHelper.findAndReturnNodeText(party.get(ORGANISATION_DETAILS), ORGANISATION_NAME);
            }
        }
        return "";
    }

    private static String formatDate(String dateStr) {
        return StringUtils.isBlank(dateStr)
            ? ""
            : DateHelper.formatTimeStampToBst(dateStr, Language.ENGLISH, false, false, DATE_FORMAT);
    }

    private static void processParty(JsonNode party, JsonNode sittingJson, MatterMetadata matterMetadata,
                                     List<GroupedPartyMatters> groupedPartyMatters) {
        if (party.has(SUBJECT) && party.get(SUBJECT).asBoolean()) {

            Matter matter = buildSitting(sittingJson);
            matter.setMatterMetadata(matterMetadata);
            matter.setOffences(processOffences(party));
            String partyHeading;
            if (party.has(INDIVIDUAL_DETAILS)) {
                matter.setPartyInfo(buildIndividualPartyInfo(party));
                partyHeading = formatIndividualPartyHeading(party.get(INDIVIDUAL_DETAILS),
                                                                          matter.getPartyInfo().getName());
            } else {
                matter.setPartyInfo(buildOrganisationPartyInfo(party));
                partyHeading = matter.getPartyInfo().getName();
            }

            addMatter(groupedPartyMatters, partyHeading, matter);
        }
    }

    private static Matter buildSitting(JsonNode sittingJson) {
        Matter matter = new Matter();
        matter.setSittingStartTime(GeneralHelper.findAndReturnNodeText(sittingJson, TIME));
        return matter;
    }

    private static PartyInfo buildIndividualPartyInfo(JsonNode party) {
        PartyInfo partyInfo = new PartyInfo();
        partyInfo.setName(PartyRoleHelper.createIndividualDetails(party));

        JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);
        partyInfo.setDob(GeneralHelper.findAndReturnNodeText(individualDetails, DOB));
        partyInfo.setAge(GeneralHelper.findAndReturnNodeText(individualDetails, AGE));
        partyInfo.setAddress(individualDetails.has(ADDRESS)
                                        ? CrimeListHelper.formatAddress(individualDetails.get(ADDRESS))
                                        : "");
        partyInfo.setAsn(GeneralHelper.findAndReturnNodeText(individualDetails, ASN));

        return partyInfo;
    }

    private static PartyInfo buildOrganisationPartyInfo(JsonNode party) {
        PartyInfo partyInfo = new PartyInfo();
        partyInfo.setName(PartyRoleHelper.createOrganisationDetails(party));

        JsonNode organisationDetails = party.get(ORGANISATION_DETAILS);
        partyInfo.setAddress(organisationDetails.has(ORGANISATION_ADDRESS)
                                        ? CrimeListHelper.formatAddress(organisationDetails.get(ORGANISATION_ADDRESS))
                                        : "");
        return partyInfo;
    }

    private static String formatIndividualPartyHeading(JsonNode individualDetails, String formattedName) {
        String gender = GeneralHelper.findAndReturnNodeText(individualDetails,GENDER);
        return formattedName
            + (gender.isEmpty() ? "" : " (" + gender + ")")
            + (isInCustody(individualDetails) ? "*" : "");

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
                offence.setPlea(GeneralHelper.findAndReturnNodeText(o, PLEA));
                offence.setPleaDate(formatDate(GeneralHelper.findAndReturnNodeText(o, PLEA_DATE)));
                offence.setConvictionDate(formatDate(GeneralHelper.findAndReturnNodeText(o, CONVICTION_DATE)));
                offence.setAdjournedDate(formatDate(GeneralHelper.findAndReturnNodeText(o, ADJOURNED_DATE)));
                offence.setOffenceLegislation(GeneralHelper.findAndReturnNodeText(o, OFFENCE_LEGISLATION));
                offence.setOffenceCode(GeneralHelper.findAndReturnNodeText(o, OFFENCE_CODE));
                offence.setOffenceMaxPenalty(GeneralHelper.findAndReturnNodeText(o, OFFENCE_MAX_PENALTY));
                offences.add(offence);
            });
        }
        return offences;
    }

    private static void addMatter(List<GroupedPartyMatters> cases, String partyHeading,
                                              Matter matter) {
        // Check if a case / application with the same party heading has already been stored.
        // If so append the new case to it, or else create a new case and add to the list of cases
        Optional<GroupedPartyMatters> commonMatter = fetchCommonMatter(cases, partyHeading);

        if (commonMatter.isPresent()) {
            commonMatter.get().getMatters().add(matter);
        } else {
            List<Matter> matters = new ArrayList<>();
            matters.add(matter);
            cases.add(new GroupedPartyMatters(partyHeading, matters));
        }
    }

    private static Optional<GroupedPartyMatters> fetchCommonMatter(List<GroupedPartyMatters> existingMatters,
                                                                                   String partyHeading) {
        for (GroupedPartyMatters matter : existingMatters) {
            if (matter.getPartyHeading().equals(partyHeading)) {
                return Optional.of(matter);
            }
        }
        return Optional.empty();
    }
}
