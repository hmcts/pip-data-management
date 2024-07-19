package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Defendant {
    private String name = "";
    private String prosecutor = "";
    private List<Offence> offences;
}
