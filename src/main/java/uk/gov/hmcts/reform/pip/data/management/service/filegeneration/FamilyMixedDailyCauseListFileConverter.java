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

        return List.of(
            tableHeaders.get(0),
            tableHeaders.get(1),
            tableHeaders.get(2),
            tableHeaders.get(3),
            tableHeaders.get(4),
            tableHeaders.get(5),
            tableHeaders.get(6),
            tableHeadersUnwrap.get(0),
            tableHeadersUnwrap.get(1),
            languageResources.get("reportingRestriction").toString()
        );
    }

    @Override
    public List<List<String>> getExcelRows(JsonNode json, Map<String, Object> languageResources, Language language) {
        CftListHelper.manipulatedListData(json, language, true);
        List<List<String>> rows = new ArrayList<>();
        List<FamilyMixedList> processedData = processRawListData(json, language);

        processedData.forEach(list -> {
            rows.add(List.of(
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
