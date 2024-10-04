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
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.OpaPressListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.OpaPublicListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.OpaResultsSummaryData;
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
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.OpaPressListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.OpaPublicListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.OpaResultsFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.PrimaryHealthListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.SjpPressListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.SjpPublicListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.SscsDailyListFileConverter;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.CARE_STANDARDS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COP_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_FIRM_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_WARNED_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.ET_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.ET_FORTNIGHTLY_PRESS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FAMILY_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.IAC_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.IAC_DAILY_LIST_ADDITIONAL_CASES;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAGISTRATES_PUBLIC_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAGISTRATES_STANDARD_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.OPA_PRESS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.OPA_PUBLIC_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.OPA_RESULTS;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PRIMARY_HEALTH_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_DELTA_PRESS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_DELTA_PUBLIC_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_PRESS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_PUBLIC_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_DAILY_LIST_ADDITIONAL_HEARINGS;

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
        Map.entry(OPA_PUBLIC_LIST, new ConversionPair(new OpaPublicListFileConverter(),
                                                      new OpaPublicListSummaryData())),
        Map.entry(OPA_PRESS_LIST, new ConversionPair(new OpaPressListFileConverter(), new OpaPressListSummaryData())),
        Map.entry(OPA_RESULTS, new ConversionPair(new OpaResultsFileConverter(), new OpaResultsSummaryData()))
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
