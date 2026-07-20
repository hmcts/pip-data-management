package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist.CrownPddaList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownPddaListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrownDailyPddaListFileConverter extends ExcelAbstractList implements FileConverter {
    private static final String LIST_HEADER = "ListHeader";

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources)
        throws IOException {
        Context context = new Context();
        context.setVariable("metadata", metadata);
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("i18n", languageResources);
        Language language = Language.valueOf(metadata.get("language"));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("common/linkToFact", language));

        JsonNode listNode = artefact.get("DailyList");
        CrownPddaListHelper.processDateInfo(context, listNode, language);
        CrownPddaListHelper.processVenueAddress(context, listNode);

        context.setVariable("version", listNode.get(LIST_HEADER).get("Version").asText());
        context.setVariable("listData", CrownPddaListHelper.processPayload(artefact, ListType.CROWN_DAILY_PDDA_LIST));

        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    @Override
    public List<String> getExcelHeaders(Map<String, Object> languageResources) {
        @SuppressWarnings("unchecked")
        List<String> tableHeaders = (List<String>) languageResources.get("tableHeaders");

        return List.of(
            languageResources.get("courtHouse").toString(),
            languageResources.get("courtAddress").toString(),
            languageResources.get("courtPhone").toString(),
            languageResources.get("courtRoom").toString(),
            languageResources.get("sittingAt").toString(),
            tableHeaders.get(0),
            tableHeaders.get(1),
            tableHeaders.get(2),
            tableHeaders.get(3),
            tableHeaders.get(4),
            tableHeaders.get(5)
        );
    }

    @Override
    public List<List<String>> getExcelRows(JsonNode json, Map<String, Object> languageResources) {
        List<List<String>> rows = new ArrayList<>();
        List<CrownPddaList> processedData = CrownPddaListHelper.processPayload(json, ListType.CROWN_DAILY_PDDA_LIST);

        processedData.forEach(
            data -> data.getSittings().forEach(sitting -> {
                String courtRoomInfo = CrownPddaListHelper.constructCourtRoomInfo(sitting, languageResources);
                sitting.getHearings().forEach(
                    hearing -> rows.add(List.of(
                        data.getCourtName(),
                        String.join(", ", data.getCourtAddress()),
                        data.getCourtPhone(),
                        courtRoomInfo,
                        sitting.getSittingAt(),
                        hearing.getHearingTime(),
                        hearing.getCaseNumber(),
                        hearing.getDefendantName(),
                        hearing.getHearingType(),
                        hearing.getProsecutingAuthority(),
                        hearing.getListNote()
                    )));
            }));

        return rows;
    }
}
