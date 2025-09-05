package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrimeListHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownFirmPddaListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Map;

public class CrownFirmPddaListFileConverter  implements FileConverter {
    private static final String FIRM_LIST = "FirmList";
    private static final String LIST_HEADER = "ListHeader";

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        return TemplateEngine.processTemplate(
            metadata.get("listType"),
            preprocessArtefactForThymeLeafConverter(artefact, metadata, language)
        );
    }

    private Context preprocessArtefactForThymeLeafConverter(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = new Context();
        context.setVariable("metadata", metadata);
        context.setVariable("i18n", languageResources);

        Language language = Language.valueOf(metadata.get("language"));
        String publicationDate = artefact.get(FIRM_LIST).get(LIST_HEADER).get("PublishedTime").asText();
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));

        String startDate = artefact.get(FIRM_LIST).get(LIST_HEADER).get("StartDate").asText();
        context.setVariable("startDate", DateHelper.convertDateFormat(startDate, "yyyy-MM-dd"));
        String endDate = GeneralHelper.findAndReturnNodeText(artefact.get(FIRM_LIST).get(LIST_HEADER), "EndDate");
        if (!endDate.isEmpty()) {
            context.setVariable("endDate", DateHelper.convertDateFormat(endDate, "yyyy-MM-dd"));
        }
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("venueAddress", CrimeListHelper.formatAddress(artefact.get(FIRM_LIST).get("CrownCourt")));
        context.setVariable("version", artefact.get(FIRM_LIST).get(LIST_HEADER).get("Version").asText());
        context.setVariable("listData", CrownFirmPddaListHelper.crownFirmPddaListFormatted(artefact));

        return context;
    }
}
