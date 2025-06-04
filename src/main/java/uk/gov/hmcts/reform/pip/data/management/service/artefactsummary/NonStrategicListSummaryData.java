package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.NonStrategicListFormatter;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.AST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CST_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FTT_LR_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FTT_TAX_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.GRC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PAAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PHT_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PLANNING_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.POAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.REVENUE_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_EASTERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_LONDON_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SIAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_LONDON_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_MIDLANDS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_NORTH_EAST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_NORTH_WEST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_SCOTLAND_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_SOUTH_EAST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_AAC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_LC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_T_AND_CC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.WPAFCC_WEEKLY_HEARING_LIST;

@SuppressWarnings({"PMD.UseConcurrentHashMap", "PMD.ExcessiveImports"})
public class NonStrategicListSummaryData implements ArtefactSummaryData {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DATE = "date";
    private static final String HEARING_TIME = "hearingTime";
    private static final String TIME = "time";
    private static final String CASE_NAME = "caseName";
    private static final String CASE_NUMBER = "caseNumber";
    private static final String CASE_REFERENCE_NUMBER = "caseReferenceNumber";
    private static final String APPEAL_REFERENCE_NUMBER = "appealReferenceNumber";
    private static final String APPELLANT = "appellant";
    private static final String HEARING_TYPE = "hearingType";

    private static final Map<ListType, List<String>> LIST_TYPE_SUMMARY_FIELDS = Map.ofEntries(
        Map.entry(CST_WEEKLY_HEARING_LIST, List.of(DATE, CASE_NAME)),
        Map.entry(PHT_WEEKLY_HEARING_LIST, List.of(DATE, CASE_NAME)),
        Map.entry(GRC_WEEKLY_HEARING_LIST, List.of(DATE, HEARING_TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(WPAFCC_WEEKLY_HEARING_LIST, List.of(DATE, HEARING_TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(UT_IAC_JR_LONDON_DAILY_HEARING_LIST, List.of(HEARING_TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST, List.of(HEARING_TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST, List.of(HEARING_TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST, List.of(HEARING_TIME, CASE_REFERENCE_NUMBER)),
        Map.entry(UT_IAC_JR_LEEDS_DAILY_HEARING_LIST, List.of(HEARING_TIME, CASE_REFERENCE_NUMBER)),
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
        Map.entry(UT_AAC_DAILY_HEARING_LIST, List.of(TIME, CASE_REFERENCE_NUMBER, APPELLANT)),
        Map.entry(AST_DAILY_HEARING_LIST, List.of(APPELLANT, APPEAL_REFERENCE_NUMBER, HEARING_TIME)),
        Map.entry(SSCS_MIDLANDS_DAILY_HEARING_LIST, List.of(HEARING_TIME, HEARING_TYPE, APPEAL_REFERENCE_NUMBER)),
        Map.entry(SSCS_SOUTH_EAST_DAILY_HEARING_LIST, List.of(HEARING_TIME, HEARING_TYPE, APPEAL_REFERENCE_NUMBER)),
        Map.entry(SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST,
                  List.of(HEARING_TIME, HEARING_TYPE, APPEAL_REFERENCE_NUMBER)),
        Map.entry(SSCS_SCOTLAND_DAILY_HEARING_LIST, List.of(HEARING_TIME, HEARING_TYPE, APPEAL_REFERENCE_NUMBER)),
        Map.entry(SSCS_NORTH_EAST_DAILY_HEARING_LIST, List.of(HEARING_TIME, HEARING_TYPE, APPEAL_REFERENCE_NUMBER)),
        Map.entry(SSCS_NORTH_WEST_DAILY_HEARING_LIST, List.of(HEARING_TIME, HEARING_TYPE, APPEAL_REFERENCE_NUMBER)),
        Map.entry(SSCS_LONDON_DAILY_HEARING_LIST, List.of(HEARING_TIME, HEARING_TYPE, APPEAL_REFERENCE_NUMBER)),
        Map.entry(LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER)),
        Map.entry(PLANNING_COURT_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER)),
        Map.entry(COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER)),
        Map.entry(CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER)),
        Map.entry(COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER)),
        Map.entry(FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER)),
        Map.entry(KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER)),
        Map.entry(KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER)),
        Map.entry(SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER)),
        Map.entry(MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER)),
        Map.entry(INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(PATENTS_COURT_CHD_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(PENSIONS_LIST_CHD_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(REVENUE_LIST_CHD_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(BUSINESS_LIST_CHD_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(COMPETITION_LIST_CHD_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, CASE_NAME)),
        Map.entry(BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, HEARING_TYPE)),
        Map.entry(BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, HEARING_TYPE)),
        Map.entry(MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, HEARING_TYPE)),
        Map.entry(LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST, List.of(TIME, CASE_NUMBER, HEARING_TYPE))
    );

    private final ListType listType;

    public NonStrategicListSummaryData(ListType listType) {
        this.listType = listType;
    }

    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> data = new ArrayList<>();

        if (payload.isObject()) {
            Iterator<String> fieldNames = payload.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldNode = payload.get(fieldName);
                if (fieldNode.isArray()) {
                    data = OBJECT_MAPPER.convertValue(fieldNode, new TypeReference<>(){});
                    break;
                }
            }
        } else if (payload.isArray()) {
            data = OBJECT_MAPPER.convertValue(payload, new TypeReference<>(){});
        }



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
