package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownPddaListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Map;

@AllArgsConstructor
public class CrownPddaListFileConverter implements FileConverter {
    private static final String DAILY_LIST = "DailyList";
    private static final String FIRM_LIST = "FirmList";
    private static final String LIST_HEADER = "ListHeader";

    private ListType listType;

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = new Context();
        context.setVariable("metadata", metadata);
        context.setVariable("i18n", languageResources);
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("provenance", metadata.get("provenance"));

        JsonNode listNode = ListType.CROWN_DAILY_PDDA_LIST.equals(listType)
            ? artefact.get(DAILY_LIST)
            : artefact.get(FIRM_LIST);

        processDateInfo(context, listNode, metadata);
        processVenueAddress(context, listNode);

        context.setVariable("version", listNode.get(LIST_HEADER).get("Version").asText());
        context.setVariable("listData", CrownPddaListHelper.processPayload(artefact, listType));

        return TemplateEngine.processTemplate(listType.name(), context);
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
