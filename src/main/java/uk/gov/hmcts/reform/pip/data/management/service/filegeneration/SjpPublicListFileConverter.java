package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.SjpPublicList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.SjpPublicListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SjpPublicListFileConverter extends ExcelAbstractList implements FileConverter {
    /**
     * Convert SJP public cases into HMTL file for PDF generation.
     *
     * @param artefact Tree object model for artefact
     * @param metadata Artefact metadata
     * @return the HTML representation of the SJP public cases
     */
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String,Object> language)
        throws IOException {
        Context context = new Context();
        String publicationDate = DateHelper.formatTimeStampToBst(
            artefact.get("document").get("publicationDate").textValue(), Language.valueOf(metadata.get("language")),
            false,
            true
        );
        language.putAll(LanguageResourceHelper.readResourcesFromPath("common/linkToFact",
                                                                     Language.valueOf(metadata.get("language"))));
        context.setVariable("publicationDate", publicationDate);
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("i18n", language);
        context.setVariable("cases", processRawListData(artefact));
        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    @Override
    public List<String> getExcelHeaders(JsonNode artefact, Map<String, Object> languageResources) {
        List<String> headers = new ArrayList<>();

        headers.add(languageResources.get("name").toString());
        headers.add(languageResources.get("postcode").toString());
        headers.add(languageResources.get("offence").toString());
        headers.add(languageResources.get("prosecutor").toString());

        return headers;
    }

    @Override
    public List<List<String>> getExcelRows(JsonNode artefact, Map<String, Object> languageResources) {
        List<List<String>> rows = new ArrayList<>();

        processRawListData(artefact).forEach(entry -> {
            List<String> row = new ArrayList<>();
            row.add(entry.getName());
            row.add(entry.getPostcode());
            row.add(entry.getOffence());
            row.add(entry.getProsecutor());
            rows.add(row);
        });

        return rows;
    }

    private List<SjpPublicList> processRawListData(JsonNode data) {
        List<SjpPublicList> sjpCases = new ArrayList<>();

        data.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(hearing -> {
                            Optional<SjpPublicList> sjpCase = SjpPublicListHelper.constructSjpCase(hearing);
                            if (sjpCase.isPresent()) {
                                sjpCases.add(sjpCase.get());
                            }
                        })
                    )
                )
            )
        );
        return sjpCases;
    }
}
