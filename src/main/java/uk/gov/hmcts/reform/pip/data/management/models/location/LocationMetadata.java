package uk.gov.hmcts.reform.pip.data.management.models.location;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class LocationMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", insertable = false, updatable = false, nullable = false)
    private UUID locationMetadataId;

    private Integer locationId;
    private String cautionMessage;
    private String welshCautionMessage;
    private String noListMessage;
    private String welshNoListMessage;
}
