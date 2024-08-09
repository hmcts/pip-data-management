package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.TribunalNationalList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TribunalNationalListHelper {
    private TribunalNationalListHelper() {
    }

    public static Context preprocessArtefactForThymeLeafConverter(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = new Context();
        Language language = Language.valueOf(metadata.get("language"));
        String publicationDate = artefact.get("document").get("publicationDate").asText();
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("email", artefact.get("venue").get("venueContact").get("venueEmail").asText());
        context.setVariable("phone", artefact.get("venue").get("venueContact").get("venueTelephone").asText());
        context.setVariable("i18n", languageResources);

        LocationHelper.formatCourtAddress(artefact, "\n", true);

        context.setVariable("cases", processRawListData(artefact, language));
        return context;
    }

    public static List<TribunalNationalList> processRawListData(JsonNode data, Language language) {
        List<TribunalNationalList> cases = new ArrayList<>();

        data.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(session -> {
                    String hearingDate = DateHelper.formatTimeStampToBst(session.get("sessionStartTime").asText(),
                                                                         language, false, false,
                                                                         "dd MMMM");
                    session.get("sittings").forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language, true);
                        sitting.get("hearing").forEach(hearing -> {
                            String hearingType = GeneralHelper.findAndReturnNodeText(hearing, "hearingType");
                            hearing.get("case").forEach(hearingCase -> {
                                String duration = CaseHelper.appendCaseSequenceIndicator(
                                    sitting.get("formattedDuration").asText(),
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "caseSequenceIndicator")
                                );
                                cases.add(new TribunalNationalList(
                                    hearingDate, hearingCase.get("caseName").asText(), duration, hearingType,
                                    courtList.get("courtHouse").get("formattedCourtHouseAddress").asText()
                                ));
                            });
                        });
                    });
                })
            )
        );
        return cases;
    }
}
