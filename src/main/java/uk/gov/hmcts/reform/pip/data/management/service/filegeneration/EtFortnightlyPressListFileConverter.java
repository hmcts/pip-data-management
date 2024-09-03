package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.EtFortnightlyPressListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Map;

@Service
public class EtFortnightlyPressListFileConverter implements FileConverter {
    private static final String VENUE = "venue";
    private static final String VENUE_CONTACT = "venueContact";

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        return TemplateEngine.processTemplate(
            metadata.get("listType"),
            preprocessArtefactForThymeLeafConverter(artefact, metadata, language)
        );
    }

    public static Context preprocessArtefactForThymeLeafConverter(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = new Context();
        LocationHelper.formatCourtAddress(artefact, "|", false);
        context.setVariable("i18n", languageResources);
        Language language = Language.valueOf(metadata.get("language"));
        String publicationDate = artefact.get("document").get("publicationDate").asText();
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("region", metadata.get("region"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("artefact", artefact);
        if (artefact.get(VENUE).has(VENUE_CONTACT)) {
            context.setVariable("phone", artefact.get(VENUE).get(VENUE_CONTACT).get("venueTelephone").asText());
            context.setVariable("email", artefact.get(VENUE).get(VENUE_CONTACT).get("venueEmail").asText());
        } else {
            context.setVariable("phone", "");
            context.setVariable("email", "");
        }
        EtFortnightlyPressListHelper.manipulatedListData(artefact, language, true);
        EtFortnightlyPressListHelper.etFortnightlyListFormatted(artefact, languageResources);
        EtFortnightlyPressListHelper.splitByCourtAndDate(artefact);
        return context;
    }
}
