package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicListFormatter;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

public class NonStrategicListFileConverter implements FileConverter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convert(JsonNode payload, Map<String, String> metadata, Map<String, Object> languageResources)
        throws IOException {
        Context context = new Context();
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("provenance", metadata.get("provenance"));

        Language language = Language.valueOf(metadata.get("language"));
        context.setVariable("lastUpdatedDate", DateHelper.formatTimeStampToBst(
            metadata.get("lastReceivedDate"), language, false, false
        ));
        context.setVariable("lastUpdatedTime", DateHelper.formatTimeStampToBst(
            metadata.get("lastReceivedDate"), language, true, false
        ));
        context.setVariable("i18n", languageResources);

        String listType = metadata.get("listType");
        String resourceName;
        if (ListType.valueOf(listType).getParentListType() != null) {
            resourceName = "non-strategic/" + UPPER_UNDERSCORE.to(LOWER_CAMEL,
                ListType.valueOf(listType).getParentListType().name());
            languageResources.putAll(LanguageResourceHelper.readResourcesFromPath(resourceName, language));
        }
        resourceName = "non-strategic/" + UPPER_UNDERSCORE.to(LOWER_CAMEL, listType);
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath(resourceName, language));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("common/nonStrategicCommon", language));

        try {
            List<Map<String, String>> data = OBJECT_MAPPER.convertValue(payload, new TypeReference<>(){});
            List<Map<String, String>> formattedData = NonStrategicListFormatter.formatAllFields(
                data, ListType.valueOf(listType)
            );
            context.setVariable("data", formattedData);
        } catch (IllegalArgumentException e) {
            Iterator<Map.Entry<String, JsonNode>> fields = payload.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String sheetName = entry.getKey();
                JsonNode sheetData = entry.getValue();
                List<Map<String, String>> sheetList = OBJECT_MAPPER.convertValue(sheetData, new TypeReference<>(){});
                List<Map<String, String>> formattedSheetData = NonStrategicListFormatter.formatAllFields(
                    sheetList, ListType.valueOf(listType)
                );
                context.setVariable(sheetName, formattedSheetData);
            }
        }

        return TemplateEngine.processNonStrategicTemplate(listType, context);
    }
}
