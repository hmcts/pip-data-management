package uk.gov.hmcts.reform.pip.data.management.models.publication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * The files held in blob storage for a publication.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicationFiles {
    private UUID artefactId;
    private List<PublicationFile> files;
}
