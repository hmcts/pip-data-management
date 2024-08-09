package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

public final class EtDailyListHelper {

    private EtDailyListHelper() {
    }

    public static void processRawListData(JsonNode data, Language language) {
        LocationHelper.formatCourtAddress(data, System.lineSeparator(), false);

        data.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting,"h:mma");
                        SittingHelper.findAndConcatenateHearingPlatform(sitting, session);
                        sitting.get("hearing").forEach(hearing ->
                            hearing.get("case").forEach(hearingCase ->
                                PartyRoleHelper.findAndManipulatePartyInformation(hearingCase, true))
                        );
                    })
                )
            )
        );
    }
}
