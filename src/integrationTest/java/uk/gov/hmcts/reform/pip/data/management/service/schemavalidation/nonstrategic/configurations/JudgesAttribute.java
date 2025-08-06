package uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.configurations;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.JUDGES;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_JUDICIAL_REVIEWS_LONDON_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;
import static uk.gov.hmcts.reform.pip.data.management.service.schemavalidation.nonstrategic.NonStrategicListTestConstants.UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH;

public final class JudgesAttribute {
    private static final Map<ListType, String> LIST_TYPE_JSON_FILE = Map.ofEntries(
        entry(ListType.CIC_WEEKLY_HEARING_LIST, CIC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.FTT_TAX_WEEKLY_HEARING_LIST, FTT_TAX_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.GRC_WEEKLY_HEARING_LIST, GRC_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_EASTERN_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_LONDON_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST, RPT_WEEKLY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_AAC_DAILY_HEARING_LIST,
              UT_ADMINISTRATIVE_APPEALS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_LEEDS_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_LONDON_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST,
              UT_IAC_JUDICIAL_REVIEWS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST,
              UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_LC_DAILY_HEARING_LIST, UT_LANDS_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH),
        entry(ListType.UT_T_AND_CC_DAILY_HEARING_LIST, UT_TAX_CHAMBER_DAILY_HEARING_LIST_JSON_FILE_PATH)
    );

    private static final Map<ListType, List<String>> LIST_TYPE_JSON_FILE_PARENT_NODES =
        Collections.emptyMap();

    public static Stream<Arguments> judgesMandatoryAttribute() {
        return SchemaValidationTestInput.generateListTypeTestInputsForAttribute(LIST_TYPE_JSON_FILE,
            LIST_TYPE_JSON_FILE_PARENT_NODES, JUDGES)
            .stream()
            .map(Arguments::of);
    }

    private JudgesAttribute() {
    }
}
