package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.ArtefactSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.EtDailyListSummaryData;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.CivilDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.FileConverter;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(profiles = "test")
class ListConversionFactoryTest {
    private final ListConversionFactory listConversionFactory = new ListConversionFactory();

    @Test
    void testGetFileConverter() {
        Optional<FileConverter> fileConverter = listConversionFactory.getFileConverter(ListType.CIVIL_DAILY_CAUSE_LIST);

        assertThat(fileConverter)
            .as("File converter is not present")
            .isPresent();

        assertThat(fileConverter.get())
            .as("File converter is not an instance of civil daily cause list converter")
            .isInstanceOf(CivilDailyCauseListFileConverter.class);
    }

    @Test
    void testGetFileConverterNotFound() {
        assertThat(listConversionFactory.getFileConverter(ListType.SJP_PRESS_REGISTER))
            .as("File converter is present")
            .isNotPresent();
    }

    @Test
    void testGetArtefactSummaryConverter() {
        Optional<ArtefactSummaryData> summaryConverter =
            listConversionFactory.getArtefactSummaryData(ListType.ET_DAILY_LIST);

        assertThat(summaryConverter)
            .as("Artefact summary is not present")
            .isPresent();

        assertThat(summaryConverter.get())
            .as("Artefact summary does not match")
            .isInstanceOf(EtDailyListSummaryData.class);
    }

    @Test
    void testGetArtefactSummaryConverterNotFound() {
        assertThat(listConversionFactory.getArtefactSummaryData(ListType.SJP_PRESS_REGISTER))
            .as("Artefact summary is not empty")
            .isNotPresent();
    }

    @Test
    void testGetArtefactSummaryConverterListPresentButNoSummary() {
        assertThat(listConversionFactory.getArtefactSummaryData(ListType.SJP_PUBLIC_LIST))
            .as("Artefact summary is not empty")
            .isNotPresent();
    }

}
