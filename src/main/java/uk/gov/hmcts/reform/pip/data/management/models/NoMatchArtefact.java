package uk.gov.hmcts.reform.pip.data.management.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class NoMatchArtefact {

    private UUID artefactId;
    private String provenance;
    private String locationId;
}
