package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.EtDailyListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Map;

@Service
public class EtDailyListFileConverter implements FileConverter {
    private static final String VENUE_CONTACT = "venueContact";

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = new Context();
        Language language = Language.valueOf(metadata.get("language"));
        setPublicationDateTime(context, artefact.get("document").get("publicationDate").asText(), language);

        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("region", metadata.get("region"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("i18n", languageResources);

        EtDailyListHelper.processRawListData(artefact, language);
        context.setVariable("artefact", artefact);
        setVenue(context, artefact.get("venue"));

        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    private void setPublicationDateTime(Context context, String publicationDate, Language language) {
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));
    }

    private void setVenue(Context context, JsonNode venue) {
        context.setVariable("venueName", GeneralHelper.findAndReturnNodeText(venue, "venueName"));
        context.setVariable("venueEmail", venue.has(VENUE_CONTACT)
            ? GeneralHelper.findAndReturnNodeText(venue.get(VENUE_CONTACT),"venueEmail")
            : "");
        context.setVariable("venueTelephone", venue.has(VENUE_CONTACT)
            ? GeneralHelper.findAndReturnNodeText(venue.get(VENUE_CONTACT),"venueTelephone")
            : "");
    }

}
