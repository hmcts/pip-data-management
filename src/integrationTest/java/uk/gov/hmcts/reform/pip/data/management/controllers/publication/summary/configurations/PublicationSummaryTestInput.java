package uk.gov.hmcts.reform.pip.data.management.controllers.publication.summary.configurations;

import lombok.Data;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;

@Data
public class PublicationSummaryTestInput {
    private ListType listType;
    private String excelFilePath;
    private String jsonFilePath;
    private List<String> expectedFields;

    public PublicationSummaryTestInput(ListType listType, String excelFilePath,
                                       String jsonFilePath, List<String> expectedFields) {
        this.listType = listType;
        this.excelFilePath = excelFilePath;
        this.jsonFilePath = jsonFilePath;
        this.expectedFields = expectedFields;
    }

    public static PublicationSummaryTestInput withoutExcel(
        ListType listType,
        String jsonFilePath,
        List<String> expectedFields) {
        return new PublicationSummaryTestInput(listType, null, jsonFilePath, expectedFields);
    }
}
