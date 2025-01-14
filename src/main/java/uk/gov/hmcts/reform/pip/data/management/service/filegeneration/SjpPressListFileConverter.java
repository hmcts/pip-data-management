package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.SjpPressList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FileConverter class for SJP press list and SJP delta press list - builds a nice pdf from input json and a html
 * template (found in resources/mocks). Uses Thymeleaf to take in variables from model and build appropriately. Final
 * output string is passed in to PDF Creation Service.
 */
public class SjpPressListFileConverter extends ExcelAbstractList implements FileConverter {

    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_ADDRESS = "organisationAddress";
    private static final String REPORTING_RESTRICTION = "reportingRestriction";
    private static final String OFFENCE = "offence";
    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";
    private static final String ACCUSED_VALUE = "ACCUSED";
    private static final String PROSECUTOR_VALUE = "PROSECUTOR";

    /**
     * parent method for the process.
     *
     * @param jsonBody - JsonNode representing the data within our jsonBody.
     * @param metadata - immutable map containing relevant data from request headers (i.e. not within json body) used
     *                 to inform the template
     * @return - html string of final output
     */
    @Override
    public String convert(JsonNode jsonBody, Map<String, String> metadata, Map<String, Object> language) {
        Context context = new Context();
        List<SjpPressList> caseList = processRawJson(jsonBody);

        String publishedDate = DateHelper.formatTimeStampToBst(
            jsonBody.get("document").get("publicationDate").asText(), Language.valueOf(metadata.get("language")),
            false,
            true
        );
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("i18n", language);
        context.setVariable("publishedDate", publishedDate);
        context.setVariable("jsonBody", jsonBody);
        context.setVariable("metaData", metadata);
        context.setVariable("cases", caseList);
        context.setVariable("artefact", jsonBody);
        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    /**
     * Create SJP press list or SJP delta press list Excel spreadsheet from list data.
     *
     * @param artefact Tree object model for artefact.
     * @param listType The list type of the publication.
     * @return The converted Excel spreadsheet as a byte array.
     */
    @Override
    public byte[] convertToExcel(JsonNode artefact, ListType listType) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            final List<SjpPressList> cases = processRawJson(artefact);

            Sheet sheet = workbook.createSheet(listType.getFriendlyName());
            CellStyle boldStyle = createBoldStyle(workbook);

            int rowIdx = 0;
            Row headingRow = sheet.createRow(rowIdx++);
            setCellValue(headingRow, 0, "Address", boldStyle);
            setCellValue(headingRow, 1, "Case URN", boldStyle);
            setCellValue(headingRow, 2, "Date of Birth", boldStyle);
            setCellValue(headingRow, 3, "Defendant Name", boldStyle);

            // Write out column headings for the max number of offences a defendant may have
            Integer maxOffences =
                cases.stream().map(SjpPressList::getNumberOfOffences).reduce(Integer::max).orElse(0);
            int offenceHeadingsIdx = 4;

            for (int i = 1; i <= maxOffences; i++) {
                setCellValue(headingRow, offenceHeadingsIdx,
                             String.format("Offence %o Press Restriction Requested", i),boldStyle);
                setCellValue(headingRow, ++offenceHeadingsIdx,
                             String.format("Offence %o Title", i), boldStyle);
                setCellValue(headingRow, ++offenceHeadingsIdx,
                             String.format("Offence %o Wording", i), boldStyle);
                offenceHeadingsIdx++;
            }
            setCellValue(headingRow, offenceHeadingsIdx, "Prosecutor Name", boldStyle);

            // Write out the data to the sheet
            for (SjpPressList entry : cases) {
                Row dataRow = sheet.createRow(rowIdx++);
                String addressRemainder = entry.getAddressRemainder() == null ? ""
                    : String.join(" ", entry.getAddressRemainder());
                String referenceRemainder = entry.getReferenceRemainder() == null ? ""
                    : String.join(" ", entry.getReferenceRemainder());
                String accusedDob = getAccusedDob(entry);

                setCellValue(dataRow, 0, concatenateStrings(entry.getAddressLine1(), addressRemainder));
                setCellValue(dataRow, 1, concatenateStrings(entry.getReference1(), referenceRemainder));
                setCellValue(dataRow, 2, accusedDob);
                setCellValue(dataRow, 3, entry.getName());

                int offenceColumnIdx = 4;

                for (Map<String, String> offence : entry.getOffences()) {
                    setCellValue(dataRow, offenceColumnIdx, offence.get(REPORTING_RESTRICTION));
                    setCellValue(dataRow, ++offenceColumnIdx, offence.get(OFFENCE));
                    setCellValue(dataRow, ++offenceColumnIdx, offence.get("wording"));
                    ++offenceColumnIdx;
                }
                setCellValue(dataRow, offenceHeadingsIdx, entry.getProsecutor());
            }
            autoSizeSheet(sheet);

            return convertToByteArray(workbook);
        }
    }

    /**
     * Process the provided json body into a list of SjpPressList.
     *
     * @param jsonBody The raw json to process.
     * @return A list of SjpPressList.
     */
    List<SjpPressList> processRawJson(JsonNode jsonBody) {
        List<SjpPressList> caseList = new ArrayList<>();

        jsonBody.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(hearing -> {
                            SjpPressList thisCase = new SjpPressList();
                            processPartyRoles(thisCase, hearing);
                            processCaseUrns(thisCase, hearing.get("case"));
                            processOffences(thisCase, hearing.get(OFFENCE));
                            thisCase.setNumberOfOffences(thisCase.getOffences().size());
                            caseList.add(thisCase);
                        })
                    )
                )
            )
        );

        return caseList;
    }

    /**
     * method for handling roles - sorts out accused and prosecutor roles and grabs relevant data from the json body.
     *
     * @param thisCase - case model which is updated by the method.
     * @param hearing    - node to be parsed.
     */
    private void processPartyRoles(SjpPressList thisCase, JsonNode hearing) {
        hearing.get(PARTY).forEach(party -> {
            if (!GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE).isEmpty()) {
                if (ACCUSED_VALUE.equals(party.get(PARTY_ROLE).asText())) {
                    processAccusedParty(thisCase, party);
                } else if (PROSECUTOR_VALUE.equals(party.get(PARTY_ROLE).asText())) {
                    thisCase.setProsecutor(PartyRoleHelper.createOrganisationDetails(party));
                }
            }
        });
    }

    /**
     * case URN processing method. Takes in a case and a case node and grabs all case urns. It is worth mentioning
     * that currently the case Urn field cannot safely be linked to offences where there are more
     * than one as they are on a different level of the json hierarchy.
     *
     * @param thisCase - model representing case data.
     * @param caseNode - json node containing cases on given case.
     */
    private void processCaseUrns(SjpPressList thisCase, JsonNode caseNode) {
        List<String> caseUrns = new ArrayList<>();
        for (final JsonNode currentCase : caseNode) {
            caseUrns.add(currentCase.get("caseUrn").asText());
        }
        thisCase.setReference1(caseUrns.get(0));
        thisCase.setReferenceRemainder(caseUrns.stream().skip(1).toList());
    }

    /**
     * Handling address lines for the model.
     *
     * @param thisCase    - our case model.
     * @param addressNode - our node containing address data.
     */
    private void processAddress(SjpPressList thisCase, JsonNode addressNode) {
        List<String> address = new ArrayList<>();
        if (addressNode.has("line")) {
            addressNode.get("line")
                .forEach(line -> address.add(line.asText()));
        }
        address.add(GeneralHelper.findAndReturnNodeText(addressNode, "town"));
        address.add(GeneralHelper.findAndReturnNodeText(addressNode, "county"));
        address.add(GeneralHelper.findAndReturnNodeText(addressNode, "postCode"));

        List<String> addressLines = address.stream()
            .filter(line -> !StringUtils.isBlank(line))
            .toList();

        if (!addressLines.isEmpty()) {
            thisCase.setAddressLine1(addressLines.get(0));
            thisCase.setAddressRemainder(addressLines.stream()
                                             .skip(1)
                                             .toList());
        }
    }

    /**
     * Method which populates the offence list in our case model.
     *
     * @param currentCase  - case model.
     * @param offencesNode - node containing our offence data.
     */
    private void processOffences(SjpPressList currentCase, JsonNode offencesNode) {
        List<Map<String, String>> listOfOffences = new ArrayList<>();
        Iterator<JsonNode> offences = offencesNode.elements();
        while (offences.hasNext()) {
            JsonNode thisOffence = offences.next();
            Map<String, String> thisOffenceMap = Map.of(
                OFFENCE, thisOffence.get("offenceTitle").asText(),
                REPORTING_RESTRICTION, processReportingRestrictionSjpPress(thisOffence),
                "wording", GeneralHelper.findAndReturnNodeText(thisOffence, "offenceWording")
            );
            listOfOffences.add(thisOffenceMap);
        }
        currentCase.setOffences(listOfOffences);
    }

    /**
     * Method to apply reporting restriction text to our model.
     *
     * @param node - json node representing an offence.
     * @return a String containing the relevant text based on reporting restriction.
     */
    private String processReportingRestrictionSjpPress(JsonNode node) {
        return node.get(REPORTING_RESTRICTION).asBoolean() ? "Active" : "None";
    }

    private void processAccusedParty(SjpPressList thisCase, JsonNode party) {
        thisCase.setAddressLine1("");
        thisCase.setAddressRemainder(Collections.emptyList());

        if (party.has(INDIVIDUAL_DETAILS)) {
            JsonNode individualDetailsNode = party.get(INDIVIDUAL_DETAILS);
            thisCase.setName(PartyRoleHelper.createIndividualDetails(party, false));
            thisCase.setDateOfBirth(GeneralHelper.findAndReturnNodeText(individualDetailsNode, "dateOfBirth"));
            thisCase.setAge(GeneralHelper.findAndReturnNodeText(individualDetailsNode, "age"));

            if (individualDetailsNode.has("address")) {
                processAddress(thisCase, individualDetailsNode.get("address"));
            }
        } else if (party.has(ORGANISATION_DETAILS)) {
            thisCase.setName(PartyRoleHelper.createOrganisationDetails(party));
            JsonNode organisationDetailsNode = party.get(ORGANISATION_DETAILS);

            if (organisationDetailsNode.has(ORGANISATION_ADDRESS)) {
                processAddress(thisCase, organisationDetailsNode.get(ORGANISATION_ADDRESS));
            }
        }
    }

    private String concatenateStrings(String... groupOfStrings) {
        return Arrays.stream(groupOfStrings)
            .filter(s -> !StringUtils.isBlank(s))
            .collect(Collectors.joining(" "));
    }

    private String getAccusedDob(SjpPressList entry) {
        if (StringUtils.isEmpty(entry.getDateOfBirth())) {
            return StringUtils.isEmpty(entry.getAge())
                ? ""
                : String.format("(%s)", entry.getAge());
        } else {
            return StringUtils.isEmpty(entry.getAge())
                ? entry.getDateOfBirth()
                : String.format("%s (%s)", entry.getDateOfBirth(), entry.getAge());
        }
    }
}
