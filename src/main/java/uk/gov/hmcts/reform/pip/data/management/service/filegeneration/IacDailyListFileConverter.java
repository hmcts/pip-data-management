package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.JudiciaryHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.Map;

/**
 * FileConverter class for the IAC daily list to generate the PDF.
 */
public class IacDailyListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata,
                          Map<String, Object> languageResources) throws IOException {
        Context context = new Context();
        calculateListData(artefact);

        context.setVariable("i18n", languageResources);
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("artefact", artefact);
        context.setVariable("contentDate", metadata.get("contentDate"));

        context.setVariable("locationName", metadata.get("locationName"));
        String publicationDate = artefact.get("document").get("publicationDate").asText();
        Language language = Language.valueOf(metadata.get("language"));
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false
        ));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false
        ));

        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }

    /**
     * This method calculates the list data for the artefact.
     *
     * @param artefact List data to calculate.
     */
    private void calculateListData(JsonNode artefact) {
        artefact.get("courtLists").forEach(courtList -> {

            ((ObjectNode) courtList).put(
                "isBailList",
                "bail list".equalsIgnoreCase(courtList.get("courtListName")
                                                 .asText())
            );

            courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(session -> {
                    String formattedJoh = JudiciaryHelper.findAndManipulateJudiciaryForCrime(session);
                    ((ObjectNode) session).put("formattedJudiciary", formattedJoh);

                    session.get("sittings").forEach(sitting -> {
                        String sittingStart = DateHelper.formatTimeStampToBst(
                            sitting.get("sittingStart").asText(), Language.ENGLISH, false, false, "h:mma"
                        );

                        ((ObjectNode) sitting).put("formattedStart", sittingStart);

                        SittingHelper.findAndConcatenateHearingPlatform(sitting, session);

                        sitting.get("hearing").forEach(hearing ->
                            hearing.get("case").forEach(hearingCase -> {
                                PartyRoleHelper.findAndManipulatePartyInformation(hearingCase, false);
                            })
                        );
                    });
                })
            );
        });
    }
}
