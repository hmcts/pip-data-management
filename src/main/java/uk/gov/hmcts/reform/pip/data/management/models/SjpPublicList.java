package uk.gov.hmcts.reform.pip.data.management.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SjpPublicList {

    @Id
    private String name;
    private String postcode;
    private String offence;
    private String prosecutor;
}
