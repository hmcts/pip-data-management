package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.Getter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.ArtefactSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.CivilDailyCauseListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.CopDailyCauseListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.CrownDailyListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.CrownFirmListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.CrownWarnedListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.EtDailyListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.EtFortnightlyPressListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.FamilyMixedDailyCauseListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.IacDailyListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.MagistratesPublicListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.MagistratesStandardListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.NonStrategicListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.SscsDailyListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.TribunalNationalListsSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.CareStandardsListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.CivilAndFamilyDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.CivilDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.CopDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.CrownDailyListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.CrownFirmListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.CrownWarnedListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.EtDailyListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.EtFortnightlyPressListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.FamilyDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.FileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.IacDailyListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.MagistratesPublicListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.MagistratesStandardListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.NonStrategicListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.PrimaryHealthListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.SjpPressListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.SjpPublicListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.SscsDailyListFileConverter;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.AST_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CARE_STANDARDS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COMPETITION_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COP_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_FIRM_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_WARNED_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CST_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.ET_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.ET_FORTNIGHTLY_PRESS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FAMILY_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FTT_LR_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FTT_TAX_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.GRC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.IAC_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.IAC_DAILY_LIST_ADDITIONAL_CASES;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAGISTRATES_PUBLIC_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAGISTRATES_STANDARD_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PAAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PATENTS_COURT_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PENSIONS_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PHT_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PLANNING_COURT_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.POAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PRIMARY_HEALTH_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.REVENUE_LIST_CHD_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_EASTERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_LONDON_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_MIDLANDS_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_NORTHERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_SOUTHERN_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SIAC_WEEKLY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_DELTA_PRESS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_DELTA_PUBLIC_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_PRESS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_PUBLIC_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_DAILY_LIST_ADDITIONAL_HEARINGS;
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
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_LONDON_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_LC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.UT_T_AND_CC_DAILY_HEARING_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.WPAFCC_WEEKLY_HEARING_LIST;


@Component
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.UseConcurrentHashMap"})
public class ListConversionFactory {

    private static final Map<ListType, ConversionPair> LIST_MAP = Map.ofEntries(
        Map.entry(SJP_PUBLIC_LIST, new ConversionPair(new SjpPublicListFileConverter())),
        Map.entry(SJP_DELTA_PUBLIC_LIST, new ConversionPair(new SjpPublicListFileConverter())),
        Map.entry(SJP_PRESS_LIST, new ConversionPair(new SjpPressListFileConverter())),
        Map.entry(SJP_DELTA_PRESS_LIST, new ConversionPair(new SjpPressListFileConverter())),
        Map.entry(CROWN_DAILY_LIST, new ConversionPair(new CrownDailyListFileConverter(),
                                                       new CrownDailyListSummaryData())),
        Map.entry(CROWN_FIRM_LIST, new ConversionPair(new CrownFirmListFileConverter(),
                                                      new CrownFirmListSummaryData())),
        Map.entry(CROWN_WARNED_LIST, new ConversionPair(new CrownWarnedListFileConverter(),
                                                        new CrownWarnedListSummaryData())),
        Map.entry(MAGISTRATES_STANDARD_LIST, new ConversionPair(new MagistratesStandardListFileConverter(),
                                                                new MagistratesStandardListSummaryData())),
        Map.entry(MAGISTRATES_PUBLIC_LIST, new ConversionPair(new MagistratesPublicListFileConverter(),
                                                              new MagistratesPublicListSummaryData())),
        Map.entry(CIVIL_DAILY_CAUSE_LIST, new ConversionPair(new CivilDailyCauseListFileConverter(),
                                                             new CivilDailyCauseListSummaryData())),
        Map.entry(FAMILY_DAILY_CAUSE_LIST, new ConversionPair(new FamilyDailyCauseListFileConverter(),
                                                              new FamilyMixedDailyCauseListSummaryData())),
        Map.entry(CIVIL_AND_FAMILY_DAILY_CAUSE_LIST, new ConversionPair(new CivilAndFamilyDailyCauseListFileConverter(),
                                                                        new FamilyMixedDailyCauseListSummaryData())),
        Map.entry(COP_DAILY_CAUSE_LIST, new ConversionPair(new CopDailyCauseListFileConverter(),
                                                           new CopDailyCauseListSummaryData())),
        Map.entry(SSCS_DAILY_LIST, new ConversionPair(new SscsDailyListFileConverter(),
                                                      new SscsDailyListSummaryData())),
        Map.entry(SSCS_DAILY_LIST_ADDITIONAL_HEARINGS, new ConversionPair(new SscsDailyListFileConverter(),
                                                                          new SscsDailyListSummaryData())),
        Map.entry(IAC_DAILY_LIST, new ConversionPair(new IacDailyListFileConverter(),
                                                     new IacDailyListSummaryData())),
        Map.entry(IAC_DAILY_LIST_ADDITIONAL_CASES, new ConversionPair(new IacDailyListFileConverter(),
                                                                      new IacDailyListSummaryData())),
        Map.entry(PRIMARY_HEALTH_LIST, new ConversionPair(new PrimaryHealthListFileConverter(),
                                                          new TribunalNationalListsSummaryData())),
        Map.entry(CARE_STANDARDS_LIST, new ConversionPair(new CareStandardsListFileConverter(),
                                                          new TribunalNationalListsSummaryData())),
        Map.entry(ET_DAILY_LIST, new ConversionPair(new EtDailyListFileConverter(),
                                                    new EtDailyListSummaryData())),
        Map.entry(ET_FORTNIGHTLY_PRESS_LIST, new ConversionPair(new EtFortnightlyPressListFileConverter(),
                                                                new EtFortnightlyPressListSummaryData())),
        Map.entry(CST_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(), new NonStrategicListSummaryData(CST_WEEKLY_HEARING_LIST)
        )),
        Map.entry(PHT_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(), new NonStrategicListSummaryData(PHT_WEEKLY_HEARING_LIST)
        )),
        Map.entry(GRC_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(), new NonStrategicListSummaryData(GRC_WEEKLY_HEARING_LIST)
        )),
        Map.entry(WPAFCC_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(), new NonStrategicListSummaryData(WPAFCC_WEEKLY_HEARING_LIST)
        )),
        Map.entry(UT_IAC_JR_LONDON_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(UT_IAC_JR_LONDON_DAILY_HEARING_LIST)
        )),
        Map.entry(UT_IAC_JR_MANCHESTER_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(UT_IAC_JR_LONDON_DAILY_HEARING_LIST)
        )),
        Map.entry(UT_IAC_JR_BIRMINGHAM_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(UT_IAC_JR_LONDON_DAILY_HEARING_LIST)
        )),
        Map.entry(UT_IAC_JR_CARDIFF_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(UT_IAC_JR_LONDON_DAILY_HEARING_LIST)
        )),
        Map.entry(UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(UT_IAC_STATUTORY_APPEALS_DAILY_HEARING_LIST)
        )),
        Map.entry(SIAC_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(SIAC_WEEKLY_HEARING_LIST)
        )),
        Map.entry(POAC_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(POAC_WEEKLY_HEARING_LIST)
        )),
        Map.entry(PAAC_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(PAAC_WEEKLY_HEARING_LIST)
        )),
        Map.entry(FTT_TAX_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(FTT_TAX_WEEKLY_HEARING_LIST)
        )),
        Map.entry(FTT_LR_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(FTT_LR_WEEKLY_HEARING_LIST)
        )),
        Map.entry(RPT_EASTERN_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(RPT_EASTERN_WEEKLY_HEARING_LIST)
        )),
        Map.entry(RPT_LONDON_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(RPT_LONDON_WEEKLY_HEARING_LIST)
        )),
        Map.entry(RPT_MIDLANDS_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(RPT_MIDLANDS_WEEKLY_HEARING_LIST)
        )),
        Map.entry(RPT_NORTHERN_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(RPT_NORTHERN_WEEKLY_HEARING_LIST)
        )),
        Map.entry(RPT_SOUTHERN_WEEKLY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(RPT_SOUTHERN_WEEKLY_HEARING_LIST)
        )),
        Map.entry(UT_T_AND_CC_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(UT_T_AND_CC_DAILY_HEARING_LIST)
        )),
        Map.entry(UT_LC_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(UT_LC_DAILY_HEARING_LIST)
        )),
        Map.entry(UT_AAC_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(UT_AAC_DAILY_HEARING_LIST)
        )),
        Map.entry(AST_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(), new NonStrategicListSummaryData(AST_DAILY_HEARING_LIST)
        )),
        Map.entry(SSCS_MIDLANDS_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(SSCS_MIDLANDS_DAILY_HEARING_LIST)
        )),
        Map.entry(SSCS_SOUTH_EAST_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(SSCS_SOUTH_EAST_DAILY_HEARING_LIST)
        )),
        Map.entry(SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(SSCS_WALES_AND_SOUTH_WEST_DAILY_HEARING_LIST)
        )),
        Map.entry(SSCS_SCOTLAND_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(SSCS_SCOTLAND_DAILY_HEARING_LIST)
        )),
        Map.entry(SSCS_NORTH_EAST_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(SSCS_NORTH_EAST_DAILY_HEARING_LIST)
        )),
        Map.entry(SSCS_NORTH_WEST_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(SSCS_NORTH_WEST_DAILY_HEARING_LIST)
        )),
        Map.entry(SSCS_LONDON_DAILY_HEARING_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(SSCS_LONDON_DAILY_HEARING_LIST)
        )),
        Map.entry(LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(LONDON_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST)
        )),
        Map.entry(PLANNING_COURT_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(PLANNING_COURT_DAILY_CAUSE_LIST)
        )),
        Map.entry(COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(COUNTY_COURT_LONDON_CIVIL_DAILY_CAUSE_LIST)
        )),
        Map.entry(CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(CIVIL_COURTS_RCJ_DAILY_CAUSE_LIST)
        )),
        Map.entry(COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(COURT_OF_APPEAL_CRIMINAL_DAILY_CAUSE_LIST)
        )),
        Map.entry(FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(FAMILY_DIVISION_HIGH_COURT_DAILY_CAUSE_LIST)
        )),
        Map.entry(KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(KINGS_BENCH_DIVISION_DAILY_CAUSE_LIST)
        )),
        Map.entry(KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(KINGS_BENCH_MASTERS_DAILY_CAUSE_LIST)
        )),
        Map.entry(SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(SENIOR_COURTS_COSTS_OFFICE_DAILY_CAUSE_LIST)
        )),
        Map.entry(MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST)
        )),
        Map.entry(INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(INTERIM_APPLICATIONS_CHD_DAILY_CAUSE_LIST)
        )),
        Map.entry(INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(INTELLECTUAL_PROPERTY_AND_ENTERPRISE_COURT_DAILY_CAUSE_LIST)
        )),
        Map.entry(INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(INTELLECTUAL_PROPERTY_LIST_CHD_DAILY_CAUSE_LIST)
        )),
        Map.entry(LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(LONDON_CIRCUIT_COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST)
        )),
        Map.entry(PATENTS_COURT_CHD_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(PATENTS_COURT_CHD_DAILY_CAUSE_LIST)
        )),
        Map.entry(PENSIONS_LIST_CHD_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(PENSIONS_LIST_CHD_DAILY_CAUSE_LIST)
        )),
        Map.entry(PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(PROPERTY_TRUSTS_PROBATE_LIST_CHD_DAILY_CAUSE_LIST)
        )),
        Map.entry(REVENUE_LIST_CHD_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(REVENUE_LIST_CHD_DAILY_CAUSE_LIST)
        )),
        Map.entry(TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(TECHNOLOGY_AND_CONSTRUCTION_COURT_KB_DAILY_CAUSE_LIST)
        )),
        Map.entry(ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(ADMIRALTY_COURT_KB_DAILY_CAUSE_LIST)
        )),
        Map.entry(BUSINESS_LIST_CHD_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(BUSINESS_LIST_CHD_DAILY_CAUSE_LIST)
        )),
        Map.entry(CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(CHANCERY_APPEALS_CHD_DAILY_CAUSE_LIST)
        )),
        Map.entry(COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(COMMERCIAL_COURT_KB_DAILY_CAUSE_LIST)
        )),
        Map.entry(COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST)
        )),
        Map.entry(COMPETITION_LIST_CHD_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(COMPETITION_LIST_CHD_DAILY_CAUSE_LIST)
        )),
        Map.entry(FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST)
        )),
        Map.entry(INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(INSOLVENCY_AND_COMPANIES_COURT_CHD_DAILY_CAUSE_LIST)
        )),
        Map.entry(BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(BIRMINGHAM_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST)
        )),
        Map.entry(BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(BRISTOL_AND_CARDIFF_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST)
        )),
        Map.entry(MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(MANCHESTER_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST)
        )),
        Map.entry(LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST, new ConversionPair(
            new NonStrategicListFileConverter(),
            new NonStrategicListSummaryData(LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST)
        ))
    );

    /**
     * Inner class, that provides a wrapper for the file and artefact summary converters.
     */
    @Getter
    static class ConversionPair {

        private final FileConverter fileConverter;
        private ArtefactSummaryData artefactSummaryData;

        public ConversionPair(FileConverter fileConverter, ArtefactSummaryData artefactSummaryData) {
            this.fileConverter = fileConverter;
            this.artefactSummaryData = artefactSummaryData;
        }

        public ConversionPair(FileConverter fileConverter) {
            this.fileConverter = fileConverter;
        }
    }

    public Optional<FileConverter> getFileConverter(ListType listType) {
        if (LIST_MAP.containsKey(listType) && LIST_MAP.get(listType).getFileConverter() != null) {
            return Optional.of(LIST_MAP.get(listType).getFileConverter());
        }
        return Optional.empty();
    }

    public Optional<ArtefactSummaryData> getArtefactSummaryData(ListType listType) {
        if (LIST_MAP.containsKey(listType) && LIST_MAP.get(listType).getArtefactSummaryData() != null) {
            return Optional.of(LIST_MAP.get(listType).getArtefactSummaryData());
        }
        return Optional.empty();
    }
}
