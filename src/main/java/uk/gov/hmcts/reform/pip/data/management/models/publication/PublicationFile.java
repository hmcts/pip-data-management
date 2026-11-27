package uk.gov.hmcts.reform.pip.data.management.models.publication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Details of a file held in blob storage for a publication.
 * The path is the location of the file within the zip, and is null if the file was not retrieved.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicationFile {
    private String fileName;
    private PublicationFileType fileType;
    private PublicationFileStatus status;
    private Long size;
    private String path;
}
