package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefendantInfo {
    private String name;
    private String dob;
    private String age;
    private String address;
    private String plea;
    private String pleaDate;
}
