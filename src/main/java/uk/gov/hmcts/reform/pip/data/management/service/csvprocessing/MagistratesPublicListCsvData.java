package uk.gov.hmcts.reform.pip.data.management.service.csvprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesPublicListHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MagistratesPublicListCsvData implements CsvData {
    @Override
    public List<String> getHeaders(Map<String, Object> languageResources) {
        @SuppressWarnings("unchecked")
        List<String> headerValuesNoWrap = (List<String>) languageResources.get("headerValuesNoWrap");
        @SuppressWarnings("unchecked")
        List<String> headerValuesWrap = (List<String>) languageResources.get("headerValuesWrap");

        return List.of(
            languageResources.get("courtHouse").toString(),
            languageResources.get("courtRoom").toString(),
            headerValuesNoWrap.get(0),
            headerValuesWrap.get(0),
            headerValuesWrap.get(1),
            headerValuesWrap.get(2),
            headerValuesWrap.get(3),
            languageResources.get("offenceDetails").toString(),
            languageResources.get("reportingRestrictions").toString()
        );
    }

    @Override
    public List<List<String>> getRows(JsonNode json, Map<String, String> metadata, Map<String, Object> languageResources) {
        List<List<String>> rows = new ArrayList<>();
        MagistratesPublicListHelper.manipulatedMagistratesPublicListData(json);

        json.get("courtLists").forEach(
                courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                        courtRoom -> courtRoom.get("session").forEach(
                                session -> session.get("sittings").forEach(
                                        sitting -> sitting.get("hearing").forEach(hearing -> {
                                            String courtHouse = metadata.get("locationName");
                                            if (hearing.has("case")) {
                                                hearing.get("case").forEach(
                                                    hearingCase -> rows.add(
                                                        List.of(
                                                            courtHouse,
                                                            session.get("formattedSessionCourtRoom").asText(),
                                                            sitting.get("time").asText(),
                                                            hearingCase.get("caseUrn").asText(),
                                                            hearingCase.get("defendant").asText(),
                                                            hearing.get("hearingType").asText(),
                                                            hearingCase.get("prosecutingAuthority").asText(),
                                                            GeneralHelper.findAndReturnNodeText(hearingCase,
                                                                                                "offence"),
                                                            getReportingRestriction(
                                                                hearingCase,
                                                                languageResources
                                                            )
                                                        )));
                                            }
                                            if (hearing.has("application")) {
                                                hearing.get("application").forEach(
                                                    application -> rows.add(
                                                        List.of(
                                                            courtHouse,
                                                            session.get("formattedSessionCourtRoom").asText(),
                                                            sitting.get("time").asText(),
                                                            application.get("applicationReference").asText(),
                                                            application.get("defendant").asText(),
                                                            "",
                                                            application.get("prosecutingAuthority").asText(),
                                                            GeneralHelper.findAndReturnNodeText(application,
                                                                                                "offence"),
                                                            ""
                                                        )));
                                            }
                                        })
                                )
                        )
                )
        );


        return rows;
    }

    private String getReportingRestriction(JsonNode hearingCase, Map<String, Object> languageResources) {
        return hearingCase.has("reportingRestriction") && hearingCase.get("reportingRestriction").asBoolean()
                ? languageResources.get("reportingRestrictionText").toString()
                : "";
    }
}
