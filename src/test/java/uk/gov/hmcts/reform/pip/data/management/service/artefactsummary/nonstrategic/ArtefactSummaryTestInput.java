package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.nonstrategic;

import lombok.Data;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;

@Data
public class ArtefactSummaryTestInput {
    private String jsonFileName;
    private ListType listType;
    private int expectedSectionCount;
    private int expectedCaseCount;
    private int expectedFieldCount;
    private List<String> expectedFieldKeys;
    private List<String> expectedFieldValues;

    public ArtefactSummaryTestInput(String jsonFileName, ListType listType, int expectedSectionCount,
                                    int expectedCaseCount, int expectedFieldCount,
                                    List<String> expectedFieldKeys, List<String> expectedFieldValues) {
        this.jsonFileName = jsonFileName;
        this.listType = listType;
        this.expectedSectionCount = expectedSectionCount;
        this.expectedCaseCount = expectedCaseCount;
        this.expectedFieldCount = expectedFieldCount;
        this.expectedFieldKeys = expectedFieldKeys;
        this.expectedFieldValues = expectedFieldValues;
    }
}
