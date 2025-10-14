package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist.CrownWarnedPddaList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.GeneralHelper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class CrownWarnedPddaListHelper {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private CrownWarnedPddaListHelper() {
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public static Map<String, List<CrownWarnedPddaList>> processPayload(JsonNode warnedPddaListData) {
        Map<String, List<CrownWarnedPddaList>> groupedData = new LinkedHashMap<>();

        JsonNode warnedList = warnedPddaListData.get("WarnedList");
        JsonNode courtLists = warnedList.get("CourtLists");

        Optional.ofNullable(courtLists)
            .filter(JsonNode::isArray)
            .ifPresent(lists -> lists.forEach(courtList -> {
                // Process WithFixedDate
                Optional.ofNullable(courtList.get("WithFixedDate"))
                    .filter(JsonNode::isArray)
                    .ifPresent(withFixedDate -> withFixedDate.forEach(withFixDate ->
                        formatFixture(withFixDate, groupedData, false)));

                // Process WithoutFixedDate
                Optional.ofNullable(courtList.get("WithoutFixedDate"))
                    .filter(JsonNode::isArray)
                    .ifPresent(withoutFixedDate -> withoutFixedDate.forEach(withoutFixDate ->
                        formatFixture(withoutFixDate, groupedData, true)));
            }));

        // Sort cases by fixed date within each group
        groupedData.forEach((key, cases) -> {
            cases.sort(Comparator.comparing(CrownWarnedPddaList::getFixedDateAsLocalDate,
                                            Comparator.nullsLast(Comparator.naturalOrder())));
        });

        return groupedData;
    }

    private static void formatFixture(JsonNode fixtureDate, Map<String,
        List<CrownWarnedPddaList>> groupedData, boolean isWithoutFixedDate) {
        JsonNode fixture = fixtureDate.get("Fixture");
        String fixedDate = GeneralHelper.findAndReturnNodeText(fixture, "FixedDate");

        Optional.ofNullable(fixture.get("Cases"))
            .filter(JsonNode::isArray)
            .ifPresent(cases -> cases.forEach(hearingCase -> {
                Optional.ofNullable(hearingCase.get("Hearing"))
                    .filter(JsonNode::isArray)
                    .ifPresent(hearings -> hearings.forEach(hearing -> {
                        String hearingDescription = isWithoutFixedDate
                            ? "To be allocated"
                            : GeneralHelper.findAndReturnNodeText(hearing, "HearingDescription");

                        groupedData.computeIfAbsent(hearingDescription, k -> new ArrayList<>())
                            .add(formatCaseInformation(fixedDate, hearing, hearingCase));
                    }));
            }));
    }

    private static CrownWarnedPddaList formatCaseInformation(String fixedDate, JsonNode hearing,
                                                             JsonNode hearingCase) {
        String defendantNames = "";
        if (hearingCase.has("Defendants")) {
            defendantNames = CrownPddaListHelper.formatDefendantName(hearingCase.get("Defendants"));
        }

        String prosecutingAuthority =
                GeneralHelper.findAndReturnNodeText(hearingCase.get("Prosecution"), "ProsecutingAuthority");

        String linkedCases = Optional.ofNullable(hearingCase.get("LinkedCases"))
            .filter(JsonNode::isArray)
            .map(linkedCasesNode -> {
                List<String> linkedCasesList = new ArrayList<>();
                linkedCasesNode.forEach(linkedCase -> {
                    if (linkedCase.has("CaseNumber")) {
                        linkedCasesList.add(linkedCase.get("CaseNumber").asText());
                    }
                });
                return String.join(", ", linkedCasesList);
            })
            .orElse("");

        String listingNotes = GeneralHelper.findAndReturnNodeText(hearing, "ListNote");

        String formattedDate = "";
        if (Strings.isNotEmpty(fixedDate)) {
            try {
                LocalDate date = LocalDate.parse(fixedDate);
                formattedDate = date.format(DATE_FORMATTER);
            } catch (Exception e) {
                formattedDate = "";
            }
        }
        String caseReference = GeneralHelper.findAndReturnNodeText(hearingCase, "CaseNumberCaTH");
        return new CrownWarnedPddaList(formattedDate, caseReference, defendantNames,
                                   prosecutingAuthority, linkedCases, listingNotes);
    }

    public static String formatContentDate(String contentDate, String language) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(contentDate, inputFormatter);
        LocalDate mondayDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Locale locale = new Locale("welsh".equalsIgnoreCase(language) ? "cy" : "en", "GB");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", locale);
        return mondayDate.format(outputFormatter);
    }

}
