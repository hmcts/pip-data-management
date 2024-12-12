package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

@Service
public class CstWeeklyHearingListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode data, Map<String, String> metadata, Map<String, Object> languageResources)
        throws IOException {
        Context context = new Context();
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("i18n", languageResources);

        Language language = Language.valueOf(metadata.get("language"));
        String listType = metadata.get("listType");
        String resourceName = "non-strategic/" + UPPER_UNDERSCORE.to(LOWER_CAMEL, listType);
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath(resourceName, language));

        data.forEach(hearing -> {
            String date = hearing.get("date").asText();
            if (!date.isEmpty()) {
                ((ObjectNode) hearing).put("formattedDate",
                                           DateHelper.convertDateFormat(date, "dd/MM/yyyy", "d MMMM yyyy"));
            }
        });
        context.setVariable("data", data);
        return TemplateEngine.processNonStrategicTemplate(listType, context);
    }
}
