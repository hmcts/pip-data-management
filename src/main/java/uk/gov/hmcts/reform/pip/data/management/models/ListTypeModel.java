package uk.gov.hmcts.reform.pip.data.management.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Provenance;

import java.util.List;

@Data
@AllArgsConstructor
public class ListTypeModel {

    private String name;

    private LocationType listLocationLevel;

    private List<Provenance> provenanceList;
}
