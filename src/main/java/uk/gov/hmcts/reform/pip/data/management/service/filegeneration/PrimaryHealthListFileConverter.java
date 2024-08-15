package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.TribunalNationalListHelper;

import java.util.Map;

@Service
public class PrimaryHealthListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        return TemplateEngine.processTemplate(
            metadata.get("listType"),
            TribunalNationalListHelper.preprocessArtefactForThymeLeafConverter(artefact, metadata, languageResources)
        );
    }
}
