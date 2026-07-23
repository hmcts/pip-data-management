package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesadultcourtlist.MagistratesAdultCourtList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesAdultCourtListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MagistratesAdultCourtListFileConverter extends ExcelAbstractList implements FileConverter {

    @Override
    public String convert(JsonNode payload, Map<String, String> metadata,
                          Map<String, Object> languageResources) throws IOException {
        Context context = new Context();

        context.setVariable("i18n", languageResources);
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("locationName", metadata.get("locationName"));

        MagistratesAdultCourtListHelper.processDateInfo(context, payload);

        Language language = Language.valueOf(metadata.get("language"));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("common/linkToFact", language));
        context.setVariable("listData",
                            MagistratesAdultCourtListHelper.processPayload(payload, language, true));

        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    @Override
    public List<String> getExcelHeaders(Map<String, Object> languageResources) {
        @SuppressWarnings("unchecked")
        List<String> tableHeaders = (List<String>) languageResources.get("tableHeaders");

        return List.of(
            languageResources.get("courtHouse").toString(),
            languageResources.get("sittingAt").toString(),
            languageResources.get("lja").toString(),
            languageResources.get("sessionStart").toString(),
            tableHeaders.get(0),
            tableHeaders.get(1),
            tableHeaders.get(2),
            tableHeaders.get(3),
            tableHeaders.get(4),
            tableHeaders.get(5),
            tableHeaders.get(6),
            tableHeaders.get(7),
            languageResources.get("offenceTitle").toString(),
            languageResources.get("offenceSummary").toString()
        );
    }

    @Override
    public List<List<String>> getExcelRows(JsonNode json, Map<String, Object> languageResources, Language language) {
        List<List<String>> rows = new ArrayList<>();
        List<MagistratesAdultCourtList> processedData = MagistratesAdultCourtListHelper.processPayload(
            json, language, true
        );

        processedData.forEach(
            data -> data.getCases().forEach(
                hearingCase -> rows.add(List.of(
                    data.getCourtName(),
                    languageResources.get("courtRoom").toString() + data.getCourtRoom(),
                    data.getLja(),
                    data.getSessionStartTime(),
                    hearingCase.getBlockStartTime(),
                    hearingCase.getDefendantName(),
                    hearingCase.getDefendantDob(),
                    hearingCase.getDefendantAddress(),
                    hearingCase.getDefendantAge(),
                    hearingCase.getInformant(),
                    hearingCase.getCaseNumber(),
                    hearingCase.getOffence().getOffenceCode(),
                    hearingCase.getOffence().getOffenceTitle(),
                    hearingCase.getOffence().getOffenceSummary()
                ))));

        return rows;
    }
}
