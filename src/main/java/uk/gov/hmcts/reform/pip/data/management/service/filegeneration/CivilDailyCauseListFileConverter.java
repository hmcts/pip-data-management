package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.CivilDailyList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CftListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CivilDailyCauseListFileConverter extends ExcelAbstractList implements FileConverter {

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources)
        throws IOException {
        Language language = Language.valueOf(metadata.get("language"));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath(
            "common/openJusticeStatement", language));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath(
            "common/linkToFact", language));

        return TemplateEngine.processTemplate(
            metadata.get("listType"),
            CftListHelper.preprocessArtefactForThymeLeafConverter(artefact, metadata, languageResources, false)
        );
    }

    @Override
    public List<String> getExcelHeaders(Map<String, Object> languageResources) {
        @SuppressWarnings("unchecked")
        List<String> tableHeaders = (List<String>) languageResources.get("headerValuesWrap");

        return List.of(
            tableHeaders.get(0),
            tableHeaders.get(1),
            tableHeaders.get(2),
            tableHeaders.get(3),
            tableHeaders.get(4),
            tableHeaders.get(5),
            tableHeaders.get(6)
        );
    }

    @Override
    public List<List<String>> getExcelRows(JsonNode json, Map<String, Object> languageResources, Language language) {
        CftListHelper.manipulatedListData(json, language, true);
        List<List<String>> rows = new ArrayList<>();
        List<CivilDailyList> processedData = processRawListData(json, language);

        processedData.forEach(list -> {
            rows.add(List.of(
                list.getTime(),
                list.getCaseId(),
                list.getCaseName(),
                list.getCaseType(),
                list.getHearingType(),
                list.getLocation(),
                list.getDuration()
            ));
        });

        return rows;
    }

    private List<CivilDailyList> processRawListData(JsonNode jsonBody, Language language) {
        List<CivilDailyList> caseList = new ArrayList<>();
        CftListHelper.manipulatedListData(jsonBody, language, false);

        jsonBody.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(hearing -> {
                            hearing.get("case").forEach(caseNode -> {
                                CivilDailyList thisCase = new CivilDailyList();

                                thisCase.setTime(sitting.path("time").asText());
                                thisCase.setCaseId(caseNode.path("caseNumber").asText());
                                thisCase.setCaseName(caseNode.path("formattedCaseName").asText());
                                thisCase.setCaseType(caseNode.path("caseType").asText());
                                thisCase.setHearingType(hearing.path("hearingType").asText());
                                thisCase.setLocation(sitting.path("caseHearingChannel").asText());
                                thisCase.setDuration(sitting.path("formattedDuration").asText());
                                caseList.add(thisCase);
                            });
                        })
                    )
                )
            )
        );

        return caseList;
    }
}
