package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist.OpaPublicList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.OpaPublicListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.List;
import java.util.Map;

public class OpaPublicListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = new Context();
        context.setVariable("i18n", languageResources);
        setPublishedDateTime(context, artefact.get("document").get("publicationDate").asText(),
                               Language.valueOf(metadata.get("language")));
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("venueAddress", LocationHelper.formatFullVenueAddress(artefact));

        List<OpaPublicList> listData = OpaPublicListHelper.formatOpaPublicList(artefact);
        context.setVariable("listData", listData);
        context.setVariable("length", listData.toArray().length);

        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    private void setPublishedDateTime(Context context, String publishedDate, Language language) {
        context.setVariable("publishedDate", DateHelper.formatTimeStampToBst(publishedDate, language,
                                                                               false, false));
        context.setVariable("publishedTime", DateHelper.formatTimeStampToBst(publishedDate, language,
                                                                               true, false));
    }
}
