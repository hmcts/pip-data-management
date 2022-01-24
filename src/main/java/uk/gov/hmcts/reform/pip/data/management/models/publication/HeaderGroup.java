package uk.gov.hmcts.reform.pip.data.management.models.publication;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class HeaderGroup {

    @Valid
    @NotNull
    private String provenance;

    @Valid
    @NotNull
    private String sourceArtefactId;

    @Enumerated
    private ArtefactType type;

    @Enumerated
    private Sensitivity sensitivity;

    @Enumerated
    private Language language;

    @Valid
    private LocalDateTime displayFrom;

    @Valid
    private LocalDateTime displayTo;


}
