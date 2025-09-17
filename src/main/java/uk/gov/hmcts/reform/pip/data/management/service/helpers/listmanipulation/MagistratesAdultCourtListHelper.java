package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesadultcourtlist.CaseInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesadultcourtlist.MagistratesAdultCourtList;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesadultcourtlist.Offence;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.List;

public final class MagistratesAdultCourtListHelper {
    private MagistratesAdultCourtListHelper() {
    }

    public static List<MagistratesAdultCourtList> processPayload(
        JsonNode payload, Language language, boolean standardList) {
        List<MagistratesAdultCourtList> results = new ArrayList<>();

        payload.get("document").get("data").get("job").get("sessions").forEach(sessionNode -> {
            MagistratesAdultCourtList result = new MagistratesAdultCourtList();
            result.setLja(sessionNode.get("lja").asText());
            result.setCourtName(sessionNode.get("court").asText());
            result.setCourtRoom(sessionNode.get("room").asText());
            result.setSessionStartTime(DateHelper.convertTimeFormat(sessionNode.get("sstart").asText(),
                                                                    "HH:mm"));
            List<CaseInfo> cases = new ArrayList<>();

            sessionNode.get("blocks").forEach(
                blockNode -> blockNode.get("cases").forEach(caseNode -> {
                    CaseInfo caseInfo = new CaseInfo();
                    caseInfo.setBlockStartTime(
                        DateHelper.convertTimeFormat(blockNode.get("bstart").asText(), "HH:mm")
                    );
                    caseInfo.setCaseNumber(caseNode.get("caseno").asText());
                    caseInfo.setDefendantName(caseNode.get("def_name").asText());

                    if (standardList) {
                        caseInfo.setDefendantDob(GeneralHelper.findAndReturnNodeText(caseNode, "def_dob"));
                        caseInfo.setDefendantAge(GeneralHelper.findAndReturnNodeText(caseNode, "def_age"));
                        caseInfo.setDefendantAddress(formatDefendantAddress(caseNode.get("def_addr")));
                        caseInfo.setInformant(caseNode.get("inf").asText());
                        caseInfo.setOffence(processOffences(caseNode.get("offences"), language));
                    }
                    cases.add(caseInfo);
                })
            );
            result.setCases(cases);
            results.add(result);
        });
        return results;
    }

    private static String formatDefendantAddress(JsonNode addressNode) {
        List<String> fullAddress = new ArrayList<>();
        fullAddress.add(GeneralHelper.findAndReturnNodeText(addressNode, "line1"));
        fullAddress.add(GeneralHelper.findAndReturnNodeText(addressNode, "line2"));
        fullAddress.add(GeneralHelper.findAndReturnNodeText(addressNode, "line3"));
        fullAddress.add(GeneralHelper.findAndReturnNodeText(addressNode, "line4"));
        fullAddress.add(GeneralHelper.findAndReturnNodeText(addressNode, "line5"));
        fullAddress.add(GeneralHelper.findAndReturnNodeText(addressNode, "pcode"));
        return GeneralHelper.convertToDelimitedString(fullAddress, ", ");
    }

    private static Offence processOffences(JsonNode offences, Language language) {
        List<String> offenceCodes = new ArrayList<>();
        List<String> offenceTitles = new ArrayList<>();
        List<String> offenceSummaries = new ArrayList<>();

        offences.forEach(offenceNode -> {
            offenceCodes.add(offenceNode.get("code").asText());
            String offenceTitle = offenceNode.get(
                language == Language.WELSH && offenceNode.has("cy_title") ? "cy_title" : "title"
            ).asText();
            offenceTitles.add(offenceTitle);
            String offenceSummary = offenceNode.get(
                language == Language.WELSH && offenceNode.has("cy_sum") ? "cy_sum" : "sum"
            ).asText();
            offenceSummaries.add(offenceSummary);
        });

        return new Offence(
            GeneralHelper.convertToDelimitedString(offenceCodes, ", "),
            GeneralHelper.convertToDelimitedString(offenceTitles, ", "),
            GeneralHelper.convertToDelimitedString(offenceSummaries, ", ")
        );
    }
}
