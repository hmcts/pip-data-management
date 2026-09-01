package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.FamilyMixedList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CftListHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.FamilyMixedListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FamilyMixedDailyCauseListFileConverter extends ExcelAbstractList implements FileConverter {
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
        @SuppressWarnings("unchecked")
        List<String> tableHeadersUnwrap = (List<String>) languageResources.get("headerValuesUnwrap");

        List<String> headers = new ArrayList<>();
        if (languageResources.get("courtHouse") != null && languageResources.get("courtRoom") != null) {
            headers.add(languageResources.get("courtHouse").toString());
            headers.add(languageResources.get("courtRoom").toString());
        }

        headers.add(tableHeaders.get(0));
        headers.add(tableHeaders.get(1));
        headers.add(tableHeaders.get(2));
        headers.add(tableHeaders.get(3));
        headers.add(tableHeaders.get(4));
        headers.add(tableHeaders.get(5));
        headers.add(tableHeaders.get(6));
        headers.add(tableHeadersUnwrap.get(0));
        headers.add(tableHeadersUnwrap.get(1));
        headers.add(languageResources.get("reportingRestriction").toString());

        return headers;
    }

    @Override
    public List<List<String>> getExcelRows(JsonNode json, Map<String, Object> languageResources,
                                           Map<String, String> metadata) {
        CftListHelper.manipulatedListData(json, Language.valueOf(metadata.get("language")), true);
        List<List<String>> rows = new ArrayList<>();
        List<FamilyMixedList> processedData = processRawListData(json, Language.valueOf(metadata.get("language")));

        processedData.forEach(list -> {
            rows.add(List.of(
                list.getCourtHouse(),
                list.getCourtRoom(),
                list.getTime(),
                list.getCaseRef(),
                list.getCaseName(),
                list.getCaseType(),
                list.getHearingType(),
                list.getLocation(),
                list.getDuration(),
                list.getApplicant(),
                list.getRespondent(),
                list.getReportingRestriction()
            ));
        });

        return rows;
    }

    private List<FamilyMixedList> processRawListData(JsonNode jsonBody, Language language) {
        return CftListHelper.processCases(
            jsonBody,
            body -> FamilyMixedListHelper.manipulatedListData(body, language),
            FamilyMixedListHelper::buildFamilyMixedList
        );
    }
}
