package uk.gov.hmcts.reform.pip.data.management.models.court;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

/**
 * This class captures the inbound Court CSV that will be ingested.
 */
@Data
public class CourtCsv {

    @CsvBindByName(column = "P&I ID")
    private int uniqueId;

    @CsvBindByName(column = "Court Desc")
    private String courtDescription;

    @CsvBindByName(column = "Region")
    private String region;

    @CsvBindByName(column = "Jurisdiction")
    private String jurisdiction;

    @CsvBindByName(column = "Provenance")
    private String provenance;

    @CsvBindByName(column = "ProvenanceID")
    private String provenanceId;

}
