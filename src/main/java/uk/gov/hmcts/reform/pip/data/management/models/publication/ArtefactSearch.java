package uk.gov.hmcts.reform.pip.data.management.models.publication;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "artefact_search")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtefactSearch {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", insertable = false, updatable = false,
        nullable = false)
    private UUID id;

    private UUID artefactId;

    private String caseNumber;

    private String caseName;

}
