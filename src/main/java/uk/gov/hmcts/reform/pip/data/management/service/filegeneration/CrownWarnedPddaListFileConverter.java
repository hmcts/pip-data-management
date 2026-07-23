package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist.CrownWarnedPddaList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownPddaListHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownWarnedPddaListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrownWarnedPddaListFileConverter extends ExcelAbstractList implements FileConverter {
    private static final String LIST_HEADER = "ListHeader";
    private static final String WARNED_LIST = "WarnedList";

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources)
        throws IOException {
        Context context = new Context();
        Language language = Language.valueOf(metadata.get("language"));
        context.setVariable("contentDate",
            CrownWarnedPddaListHelper.formatContentDate(metadata.get("contentDate"), language.toString()));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("common/linkToFact", language));
        context.setVariable("i18n", languageResources);
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("provenance", metadata.get("provenance"));

        JsonNode listNode = artefact.get(WARNED_LIST);

        processDateInfo(context, listNode, metadata);
        processVenueAddress(context, listNode);

        context.setVariable("listData", CrownWarnedPddaListHelper.processPayload(artefact));
        context.setVariable("version", listNode.get(LIST_HEADER).get("Version").asText());
        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    @Override
    public List<String> getExcelHeaders(Map<String, Object> languageResources) {
        @SuppressWarnings("unchecked")
        List<String> tableHeaders = (List<String>) languageResources.get("tableHeaders");

        return List.of(
            languageResources.get("hearingDescription").toString(),
            tableHeaders.get(0),
            tableHeaders.get(1),
            tableHeaders.get(2),
            tableHeaders.get(3),
            tableHeaders.get(4),
            tableHeaders.get(5)
        );
    }

    @Override
    public List<List<String>> getExcelRows(JsonNode json, Map<String, Object> languageResources, Language language) {
        List<List<String>> rows = new ArrayList<>();
        Map<String, List<CrownWarnedPddaList>> processedData = CrownWarnedPddaListHelper.processPayload(json);

        processedData.forEach(
            (hearingDescription, cases) -> cases.forEach(
                hearingCase -> rows.add(List.of(
                    getHearingDescription(hearingDescription, languageResources),
                    hearingCase.getFixedDate(),
                    hearingCase.getCaseReference(),
                    hearingCase.getDefendantNames(),
                    hearingCase.getProsecutingAuthority(),
                    hearingCase.getLinkedCases(),
                    hearingCase.getListingNotes()
                ))
            ));
        return rows;
    }

    private String getHearingDescription(String hearingDescription, Map<String, Object> languageResources) {
        return "To be allocated".equalsIgnoreCase(hearingDescription)
            ? languageResources.get("toBeAllocatedText").toString()
            : hearingDescription;
    }

    private void processDateInfo(Context context, JsonNode listNode, Map<String, String> metadata) {
        Language language = Language.valueOf(metadata.get("language"));
        JsonNode listHeader = listNode.get(LIST_HEADER);
        String publicationDateTime = listHeader.get("PublishedTime").asText();
        context.setVariable("publicationDate",
                            DateHelper.formatTimeStampToBst(publicationDateTime, language, false, false));
        context.setVariable("publicationTime",
                            DateHelper.formatTimeStampToBst(publicationDateTime, language, true, false));

        String startDate = listHeader.get("StartDate").asText();
        context.setVariable("startDate", DateHelper.convertDateFormat(startDate, "yyyy-MM-dd"));

        String endDate = GeneralHelper.findAndReturnNodeText(listHeader, "EndDate");
        if (!endDate.isEmpty()) {
            context.setVariable("endDate", DateHelper.convertDateFormat(endDate, "yyyy-MM-dd"));
        }
    }

    private void processVenueAddress(Context context, JsonNode listNode) {
        JsonNode crownCourt = listNode.get("CrownCourt");
        if (crownCourt.has("CourtHouseAddress")) {
            context.setVariable("venueAddress",
                                CrownPddaListHelper.formatAddress(crownCourt.get("CourtHouseAddress")));
        }
    }
}
