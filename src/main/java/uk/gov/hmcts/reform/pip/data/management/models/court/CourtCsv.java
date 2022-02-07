package uk.gov.hmcts.reform.pip.data.management.models.court;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class captures the inbound Court CSV that will be ingested.
 */
@Data
public class CourtCsv {

    @CsvBindByName(column = "P&I ID")
    private Integer uniqueId;

    @CsvBindByName(column = "Court Desc")
    private String courtName;

    @CsvBindByName(column = "Region")
    private String region;

    @CsvBindAndSplitByName(elementType = String.class, splitOn =";(\\s)?")
    private List<String> jurisdiction = new ArrayList<>();

    @CsvBindByName(column = "Provenance")
    private String provenance;

    @CsvBindByName(column = "ProvenanceID")
    private String provenanceId;

}
