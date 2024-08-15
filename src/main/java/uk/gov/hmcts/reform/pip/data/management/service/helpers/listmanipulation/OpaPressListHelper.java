package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist.Offence;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist.OpaCaseInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist.OpaDefendantInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist.OpaPressList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("PMD.TooManyMethods")
public final class OpaPressListHelper {
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";
    private static final String CASE_URN = "caseUrn";
    private static final String SCHEDULED_HEARING_DATE = "scheduledHearingDate";
    private static final String INFORMANT = "informant";
    private static final String PROSECUTING_AUTHORITY_REF = "prosecutionAuthorityRef";

    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String INDIVIDUAL_FORENAMES = "individualForenames";
    private static final String INDIVIDUAL_MIDDLE_NAME = "individualMiddleName";
    private static final String INDIVIDUAL_SURNAME = "individualSurname";
    private static final String DOB = "dateOfBirth";
    private static final String AGE = "age";
    private static final String ADDRESS = "address";
    private static final String POSTCODE = "postCode";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_NAME = "organisationName";
    private static final String ORGANISATION_ADDRESS = "organisationAddress";

    private static final String OFFENCE = "offence";
    private static final String OFFENCE_TITLE = "offenceTitle";
    private static final String OFFENCE_SECTION = "offenceSection";
    private static final String OFFENCE_WORDING = "offenceWording";
    private static final String PLEA = "plea";
    private static final String PLEA_DATE = "pleaDate";
    private static final String REPORTING_RESTRICTION_DETAIL = "reportingRestrictionDetail";

    private static final String DEFENDANT = "DEFENDANT";
    private static final String PROSECUTING_AUTHORITY = "PROSECUTING_AUTHORITY";
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String DELIMITER = ", ";

    private OpaPressListHelper() {
    }

    /**
     * Process raw JSON for OPA press list to generate hearingCase sorted by the plea date.
     *
     * @param jsonData JSON data for the list
     * @return a sorted map of plea date to OPA press list hearingCase
     */
    public static Map<String, List<OpaPressList>> processRawListData(JsonNode jsonData) {
        Map<String, List<OpaPressList>> result = new ConcurrentHashMap<>();

        jsonData.get(COURT_LISTS).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(
                    session -> session.get(SITTINGS).forEach(
                        sitting -> sitting.get(HEARING).forEach(
                            hearing -> hearing.get(CASE).forEach(hearingCase -> {
                                if (hearingCase.has(PARTY)) {

                                    processPartyRoles(hearingCase).forEach(defendant -> {

                                        OpaCaseInfo caseInfo = buildHearingCase(hearingCase);

                                        // All the offences under the same defendant have the same plea date
                                        String pleaDate = defendant.getOffences().get(0).getPleaDate();
                                        result.computeIfAbsent(pleaDate, x -> new ArrayList<>())
                                            .add(new OpaPressList(defendant, caseInfo));
                                    });
                                }
                            })
                        )
                    )
                )
            )
        );
        return OpaPressListSorter.sort(result);
    }

    private static OpaCaseInfo buildHearingCase(JsonNode hearingCase) {
        String scheduledHearingDate = DateHelper.formatTimeStampToBst(
            GeneralHelper.findAndReturnNodeText(hearingCase, SCHEDULED_HEARING_DATE),
            Language.ENGLISH, false, false, DATE_FORMAT
        );
        return new OpaCaseInfo(
            GeneralHelper.findAndReturnNodeText(hearingCase, CASE_URN),
            scheduledHearingDate,
            GeneralHelper.formatNodeArray(hearingCase, REPORTING_RESTRICTION_DETAIL, DELIMITER)
        );
    }

    private static List<OpaDefendantInfo> processPartyRoles(JsonNode hearingCase) {
        List<OpaDefendantInfo> defendantInfo = new ArrayList<>();
        hearingCase.get(PARTY).forEach(party -> {
            if (party.has(PARTY_ROLE) && DEFENDANT.equals(party.get(PARTY_ROLE).asText())) {
                OpaDefendantInfo defendant = new OpaDefendantInfo();
                processDefendant(party, defendant);

                // The offence's plea date is used to group and sort the hearingCase for the defendant. If plea date is
                // missing the entry will be dropped
                if (!defendant.getName().isEmpty()
                    && !defendant.getOffences().isEmpty()
                    && !defendant.getOffences().get(0).getPleaDate().isEmpty()) {
                    defendantInfo.add(defendant);
                }
            }
        });
        defendantInfo.forEach(d -> d.setProsecutor(processProsecutor(hearingCase)));
        return defendantInfo;
    }

    private static void processDefendant(JsonNode party, OpaDefendantInfo defendantInfo) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            processIndividualDefendant(party, defendantInfo);
        } else if (party.has(ORGANISATION_DETAILS)) {
            processOrganisationDefendant(party, defendantInfo);
        }
    }

    private static void processIndividualDefendant(JsonNode party, OpaDefendantInfo defendantInfo) {
        JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);
        String address = individualDetails.has(ADDRESS)
            ? CrimeListHelper.formatDefendantAddress(individualDetails.get(ADDRESS)) : "";

        String addressWithoutPostcode = individualDetails.has(ADDRESS)
            ? CrimeListHelper.formatDefendantAddressWithoutPostcode(individualDetails.get(ADDRESS)) : "";

        String postcode = individualDetails.has(ADDRESS) && individualDetails.get(ADDRESS).has(POSTCODE)
            ? individualDetails.get(ADDRESS).get(POSTCODE).asText() : "";

        defendantInfo.setName(formatDefendantName(individualDetails));
        defendantInfo.setDob(GeneralHelper.findAndReturnNodeText(individualDetails, DOB));
        defendantInfo.setAge(GeneralHelper.findAndReturnNodeText(individualDetails, AGE));
        defendantInfo.setAddress(address);
        defendantInfo.setAddressWithoutPostcode(addressWithoutPostcode);
        defendantInfo.setPostcode(postcode);
        defendantInfo.setOffences(processOffences(individualDetails));
    }

    private static void processOrganisationDefendant(JsonNode party, OpaDefendantInfo defendantInfo) {
        JsonNode organisationDetails = party.get(ORGANISATION_DETAILS);
        String address = organisationDetails.has(ORGANISATION_ADDRESS)
            ? CrimeListHelper.formatDefendantAddress(organisationDetails.get(ORGANISATION_ADDRESS)) : "";

        String addressWithoutPostcode = organisationDetails.has(ORGANISATION_ADDRESS)
            ? CrimeListHelper.formatDefendantAddressWithoutPostcode(
            organisationDetails.get(ORGANISATION_ADDRESS)) : "";

        String postcode = organisationDetails.has(ORGANISATION_ADDRESS)
            && organisationDetails.get(ORGANISATION_ADDRESS).has(POSTCODE)
            ? organisationDetails.get(ORGANISATION_ADDRESS).get(POSTCODE).asText() : "";

        defendantInfo.setName(
            GeneralHelper.findAndReturnNodeText(organisationDetails, ORGANISATION_NAME)
        );
        defendantInfo.setAddress(address);
        defendantInfo.setAddressWithoutPostcode(addressWithoutPostcode);
        defendantInfo.setPostcode(postcode);
        defendantInfo.setOffences(processOffences(organisationDetails));
    }

    private static String formatDefendantName(JsonNode individualDetails) {
        String forename = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_FORENAMES);
        String middleName = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_MIDDLE_NAME);
        String surname = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_SURNAME);

        String forenames = Stream.of(forename, middleName)
            .filter(n -> !StringUtils.isBlank(n))
            .collect(Collectors.joining(" "));

        return Stream.of(surname, forenames)
            .filter(n -> !StringUtils.isBlank(n))
            .collect(Collectors.joining(DELIMITER));
    }

    public static String processProsecutor(JsonNode hearingCase) {
        String prosecutor = getPartyInformant(hearingCase);
        if (prosecutor.isEmpty()) {
            prosecutor = getPartyProsecutor(hearingCase);
        }
        return prosecutor;

    }

    private static String getPartyInformant(JsonNode hearingCase) {
        if (hearingCase.has(INFORMANT)) {
            JsonNode informantNode = hearingCase.get(INFORMANT);
            return GeneralHelper.findAndReturnNodeText(informantNode, PROSECUTING_AUTHORITY_REF);
        }
        return "";
    }

    private static String getPartyProsecutor(JsonNode hearingCase) {
        List<String> prosecutors = new ArrayList<>();
        if (hearingCase.has(PARTY)) {
            hearingCase.get(PARTY).forEach(party -> {
                if (party.has(PARTY_ROLE)
                    && PROSECUTING_AUTHORITY.equals(party.get(PARTY_ROLE).asText())
                    && party.has(ORGANISATION_DETAILS)) {
                    prosecutors.add(GeneralHelper.findAndReturnNodeText(
                        party.get(ORGANISATION_DETAILS),
                        ORGANISATION_NAME
                    ));
                }
            });
        }
        return GeneralHelper.convertToDelimitedString(prosecutors, DELIMITER);
    }

    private static List<Offence> processOffences(JsonNode detailsNode) {
        List<Offence> offences = new ArrayList<>();

        if (detailsNode.has(OFFENCE)) {
            detailsNode.get(OFFENCE).forEach(o -> {
                String pleaDate = DateHelper.formatTimeStampToBst(
                    GeneralHelper.findAndReturnNodeText(o, PLEA_DATE),
                    Language.ENGLISH, false, false, DATE_FORMAT
                );

                Offence offence = new Offence();
                offence.setOffenceTitle(GeneralHelper.findAndReturnNodeText(o, OFFENCE_TITLE));
                offence.setOffenceSection(GeneralHelper.findAndReturnNodeText(o, OFFENCE_SECTION));
                offence.setOffenceWording(GeneralHelper.findAndReturnNodeText(o, OFFENCE_WORDING));
                offence.setPlea(GeneralHelper.findAndReturnNodeText(o, PLEA));
                offence.setPleaDate(pleaDate);
                offence.setOffenceReportingRestriction(
                    GeneralHelper.formatNodeArray(o, REPORTING_RESTRICTION_DETAIL, DELIMITER)
                );
                offences.add(offence);
            });
        }
        return offences;
    }
}
