package uk.gov.hmcts.reform.pip.data.management.controllers.publication.summary.configurations;

import lombok.Data;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Arrays;

@Data
public class ListTestCaseSettings {
    private ListType listType;
    private String excelFilePath;
    private String jsonFilePath;
    private String[] expectedFields;

    public ListTestCaseSettings(ListType listType, String excelFilePath,
                                String jsonFilePath, String... expectedFields) {
        this.listType = listType;
        this.excelFilePath = excelFilePath;
        this.jsonFilePath = jsonFilePath;
        this.expectedFields = Arrays.copyOf(expectedFields, expectedFields.length);
    }

    public static ListTestCaseSettings withoutExcel(
        ListType listType,
        String jsonFilePath,
        String... expectedFields) {
        return new ListTestCaseSettings(listType, null, jsonFilePath, expectedFields);
    }
}
