package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownFirmPddaListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Map;

public class CrownFirmPddaListFileConverter  implements FileConverter {
    private static final String FIRM_LIST = "FirmList";
    private static final String LIST_HEADER = "ListHeader";

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = new Context();
        context.setVariable("metadata", metadata);
        context.setVariable("i18n", languageResources);
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("provenance", metadata.get("provenance"));

        processDateInfo(context, artefact, metadata);
        processVenueAddress(context, artefact);

        context.setVariable("version", artefact.get(FIRM_LIST).get(LIST_HEADER).get("Version").asText());
        context.setVariable("listData", CrownFirmPddaListHelper.processPayload(artefact));

        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    private void processDateInfo(Context context, JsonNode artefact, Map<String, String> metadata) {
        Language language = Language.valueOf(metadata.get("language"));
        JsonNode listHeader = artefact.get(FIRM_LIST).get(LIST_HEADER);
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

    private void processVenueAddress(Context context, JsonNode artefact) {
        JsonNode crownCourt = artefact.get(FIRM_LIST).get("CrownCourt");
        if (crownCourt.has("CourtHouseAddress")) {
            context.setVariable("venueAddress",
                                CrownFirmPddaListHelper.formatAddress(crownCourt.get("CourtHouseAddress")));
        }
    }
}
