package uk.gov.hmcts.reform.pip.data.management.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PublicationFileSizes {
    private Long primaryPdf;
    private Long additionalPdf;
    private Long excel;
}
