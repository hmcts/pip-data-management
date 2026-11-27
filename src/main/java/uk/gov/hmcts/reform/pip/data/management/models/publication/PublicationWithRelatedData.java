package uk.gov.hmcts.reform.pip.data.management.models.publication;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A publication with all of its fields, alongside its related artefact search data.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicationWithRelatedData {

    @JsonUnwrapped
    private Artefact artefact;

    private List<ArtefactSearch> searchData;
}
