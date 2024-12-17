package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicListFormatter;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

@Service
public class NonStrategicListFileConverter implements FileConverter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convert(JsonNode payload, Map<String, String> metadata, Map<String, Object> languageResources)
        throws IOException {
        Context context = new Context();
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("i18n", languageResources);

        Language language = Language.valueOf(metadata.get("language"));
        String listType = metadata.get("listType");
        String resourceName = "non-strategic/" + UPPER_UNDERSCORE.to(LOWER_CAMEL, listType);
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath(resourceName, language));

        List<Map<String, String>> data = OBJECT_MAPPER.convertValue(payload, new TypeReference<>(){});
        List<Map<String, String>> formattedData = NonStrategicListFormatter.formatFields(
            data, ListType.valueOf(listType)
        );
        context.setVariable("data", formattedData);

        return TemplateEngine.processNonStrategicTemplate(listType, context);
    }
}
