package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesAdultCourtListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.Map;

@AllArgsConstructor
public class MagistratesAdultCourtListFileConverter implements FileConverter {

    private boolean isStandardList;

    @Override
    public String convert(JsonNode payload, Map<String, String> metadata,
                          Map<String, Object> languageResources) throws IOException {
        Context context = new Context();

        context.setVariable("i18n", languageResources);
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("locationName", metadata.get("locationName"));

        JsonNode payloadDocument = payload.get("document");
        String publicationDate = payloadDocument.get("data").get("job").get("printdate").asText();
        context.setVariable("publicationDate",
                            DateHelper.convertDateFormat(publicationDate, "dd/MM/yyyy"));

        if (payloadDocument.has("info") && payloadDocument.get("info").has("start_time")) {
            String publicationTime = payloadDocument.get("info").get("start_time").asText();
            context.setVariable("publicationTime",
                                DateHelper.convertTimeFormat(publicationTime, "HH:mm:ss"));
        } else {
            context.setVariable("publicationTime", "");
        }

        Language language = Language.valueOf(metadata.get("language"));
        context.setVariable("listData",
                            MagistratesAdultCourtListHelper.processPayload(payload, language, isStandardList));

        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }
}
