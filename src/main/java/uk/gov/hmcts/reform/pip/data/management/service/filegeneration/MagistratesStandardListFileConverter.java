package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesStandardListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Map;

public class MagistratesStandardListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = new Context();
        Language language = Language.valueOf(metadata.get("language"));

        setPublicationDateTime(context, artefact.get("document").get("publicationDate").asText(), language);
        context.setVariable("i18n", languageResources);

        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("version", artefact.get("document").get("version").asText());

        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("venueAddress", LocationHelper.formatFullVenueAddress(artefact));
        context.setVariable("cases", MagistratesStandardListHelper.processRawListData(artefact, language));

        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    private void setPublicationDateTime(Context context, String publicationDate, Language language) {
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));
    }
}
