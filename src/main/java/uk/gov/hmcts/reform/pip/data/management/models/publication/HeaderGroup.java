package uk.gov.hmcts.reform.pip.data.management.models.publication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
public class HeaderGroup {

    private String provenance;

    private String sourceArtefactId;

    private ArtefactType type;

    private Sensitivity sensitivity;

    private Language language;

    private LocalDateTime displayFrom;

    private LocalDateTime displayTo;


}
