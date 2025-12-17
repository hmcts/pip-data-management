package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownPddaListHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownWarnedPddaListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class CrownWarnedPddaListFileConverter implements FileConverter {
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
