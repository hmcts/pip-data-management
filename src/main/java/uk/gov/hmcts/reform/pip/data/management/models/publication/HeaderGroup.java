package uk.gov.hmcts.reform.pip.data.management.models.publication;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class HeaderGroup {

    @Valid
    @NotNull
    @NotBlank
    private String provenance;

    @Valid
    @NotNull
    @NotBlank
    private String sourceArtefactId;

    @Enumerated
    @NotBlank
    private ArtefactType type;

    @Enumerated
    private Sensitivity sensitivity;

    @Enumerated
    private Language language;

    @Valid
    private LocalDateTime displayFrom;

    @Valid
    private LocalDateTime displayTo;

    @Enumerated
    private ListType listType;

    @Valid
    @NotNull
    @NotBlank
    private String courtId;

    @Valid
    private LocalDateTime contentDate;

}
