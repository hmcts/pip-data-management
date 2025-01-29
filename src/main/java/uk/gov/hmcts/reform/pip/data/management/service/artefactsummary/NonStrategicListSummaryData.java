package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicListFormatter;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.CST_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FTT_LR_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FTT_TAX_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.GRC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PAAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PHT_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.POAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_EASTERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_LONDON_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SIAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_AAC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_LC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_T_AND_CC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.WPAFCC_WEEKLY_HEARING_LIST;

@SuppressWarnings("PMD.UseConcurrentHashMap")
public class NonStrategicListSummaryData implements ArtefactSummaryData {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DATE = "date";
    private static final String HEARING_TIME = "hearingTime";
    private static final String TIME = "time";
    private static final String CASE_NAME = "caseName";
    private static final String CASE_REFERENCE_NUMBER = "caseReferenceNumber";
    private static final String APPEAL_REFERENCE_NUMBER = "appealReferenceNumber";
    private static final String APPELLANT = "appellant";

    private static final Map<ListType, List<String>> LIST_TYPE_SUMMARY_FIELDS = Map.ofEntries(
        Map.entry(CST_WEEKLY_HEARING_LIST, List.of(DATE, CASE_NAME)),
        Map.entry(PHT_WEEKLY_HEARING_LIST, List.of(DATE, CASE_NAME)),
        Map.entry(GRC_WEEKLY_HEARING_LIST, List.of(DATE, HEARING_TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(WPAFCC_WEEKLY_HEARING_LIST, List.of(DATE, HEARING_TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(UT_IAC_JR_LONDON_DAILY_HEARING_LIST, List.of(HEARING_TIME,
                                                               CASE_REFERENCE_NUMBER)),
        Map.entry(UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST, List.of(HEARING_TIME,
                                                                   CASE_REFERENCE_NUMBER)),
        Map.entry(UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST, List.of(HEARING_TIME,
                                                                   CASE_REFERENCE_NUMBER)),
        Map.entry(UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST, List.of(HEARING_TIME,
                                                                CASE_REFERENCE_NUMBER)),
        Map.entry(UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST, List.of(HEARING_TIME,
                                                                       APPEAL_REFERENCE_NUMBER)),
        Map.entry(SIAC_WEEKLY_HEARING_LIST, List.of(DATE, TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(POAC_WEEKLY_HEARING_LIST, List.of(DATE, TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(PAAC_WEEKLY_HEARING_LIST, List.of(DATE, TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(FTT_TAX_WEEKLY_HEARING_LIST, List.of(DATE, HEARING_TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(FTT_LR_WEEKLY_HEARING_LIST, List.of(DATE, HEARING_TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(RPT_EASTERN_WEEKLY_HEARING_LIST, List.of(DATE, TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(RPT_LONDON_WEEKLY_HEARING_LIST, List.of(DATE, TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(RPT_MIDLANDS_WEEKLY_HEARING_LIST, List.of(DATE, TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(RPT_NORTHERN_WEEKLY_HEARING_LIST, List.of(DATE, TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(RPT_SOUTHERN_WEEKLY_HEARING_LIST, List.of(DATE, TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(UT_T_AND_CC_DAILY_HEARING_LIST, List.of(TIME, CASE_REFERENCE_NUMBER, CASE_NAME)),
        Map.entry(UT_LC_DAILY_HEARING_LIST, List.of(TIME, CASE_REFERENCE_NUMBER, CASE_NAME)),
        Map.entry(UT_AAC_DAILY_HEARING_LIST, List.of(TIME, CASE_REFERENCE_NUMBER, APPELLANT))
    );

    private final ListType listType;

    public NonStrategicListSummaryData(ListType listType) {
        this.listType = listType;
    }

    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> data = OBJECT_MAPPER.convertValue(payload, new TypeReference<>(){});
        Optional<Map<String, Function<String, String>>> listTypeFormatter = NonStrategicListFormatter
            .getListTypeFormatter(listType);

        List<Map<String, String>> summaryCases = new ArrayList<>();
        data.forEach(hearing -> {
            Map<String, String> summaryCase = new LinkedHashMap<>();
            if (LIST_TYPE_SUMMARY_FIELDS.containsKey(listType)) {
                List<String> summaryFields = LIST_TYPE_SUMMARY_FIELDS.get(listType);
                summaryFields.forEach(field -> {
                    String formattedKey = StringUtils.capitalize(
                        StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(field), StringUtils.SPACE)
                            .toLowerCase(Locale.UK)
                    );

                    String formattedValue = listTypeFormatter.isPresent() && listTypeFormatter.get().containsKey(field)
                        ? NonStrategicListFormatter.formatField(field, hearing.get(field), listTypeFormatter.get())
                        : hearing.get(field);
                    summaryCase.put(formattedKey, formattedValue);
                });
                summaryCases.add(summaryCase);
            }
        });

        return Collections.singletonMap(null, summaryCases);
    }
}
