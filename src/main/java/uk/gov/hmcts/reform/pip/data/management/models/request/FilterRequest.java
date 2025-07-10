package uk.gov.hmcts.reform.pip.data.management.models.request;

import lombok.Data;
import lombok.Value;

import java.util.List;

@Value
@Data
public class FilterRequest {

    List<String> jurisdictions;
    List<String> regions;
}
