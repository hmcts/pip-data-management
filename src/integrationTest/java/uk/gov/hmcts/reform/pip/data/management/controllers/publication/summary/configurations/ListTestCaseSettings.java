package uk.gov.hmcts.reform.pip.data.management.controllers.publication.summary.configurations;

import lombok.Data;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;

@Data
public class ListTestCaseSettings {
    private ListType listType;
    private String excelFilePath;
    private String jsonFilePath;
    private List<String> expectedFields;

    public ListTestCaseSettings(ListType listType, String excelFilePath,
                                String jsonFilePath, List<String> expectedFields) {
        this.listType = listType;
        this.excelFilePath = excelFilePath;
        this.jsonFilePath = jsonFilePath;
        this.expectedFields = expectedFields;
    }

    public static ListTestCaseSettings withoutExcel(
        ListType listType,
        String jsonFilePath,
        List<String> expectedFields) {
        return new ListTestCaseSettings(listType, null, jsonFilePath, expectedFields);
    }
}
