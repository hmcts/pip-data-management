package uk.gov.hmcts.reform.pip.data.management.models.location;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class LocationMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", insertable = false, updatable = false, nullable = false)
    private UUID locationMetadataId;

    @Valid
    @NotNull
    private Integer locationId;

    @Valid
    private String cautionMessage;

    @Valid
    private String welshCautionMessage;

    @Valid
    private String noListMessage;

    @Valid
    private String welshNoListMessage;
}
