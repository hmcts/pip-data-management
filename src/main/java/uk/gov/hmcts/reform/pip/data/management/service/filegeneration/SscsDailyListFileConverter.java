package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.sscsdailylist.CourtHouse;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.SscsListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class SscsDailyListFileConverter implements FileConverter {

    @Override
    public String convert(JsonNode highestLevelNode, Map<String, String> metadata,
                          Map<String, Object> languageResources) throws IOException {
        Context context = new Context();
        Language language = Language.valueOf(metadata.get("language"));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("common/openJusticeStatement", language));

        context.setVariable("i18n", languageResources);
        context.setVariable("metadata", metadata);
        context.setVariable("telephone", GeneralHelper.safeGet("venue.venueContact.venueTelephone", highestLevelNode));
        context.setVariable("email", GeneralHelper.safeGet("venue.venueContact.venueEmail", highestLevelNode));

        context.setVariable("publishedDate", DateHelper.formatTimeStampToBst(
            GeneralHelper.safeGet("document.publicationDate", highestLevelNode), language,
            false, true)
        );

        List<CourtHouse> listOfCourtHouses = new ArrayList<>();
        for (JsonNode courtHouse : highestLevelNode.get("courtLists")) {
            listOfCourtHouses.add(SscsListHelper.courtHouseBuilder(courtHouse));
        }
        context.setVariable("courtList", listOfCourtHouses);
        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }
}



