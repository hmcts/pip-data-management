package uk.gov.hmcts.reform.pip.data.management.models.location;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class captures the inbound Court CSV that will be ingested.
 */
@Data
public class LocationCsv {

    @CsvBindByName(column = "P&I ID")
    private Integer uniqueId;

    @CsvBindByName(column = "Court Desc")
    private String locationName;

    @CsvBindAndSplitByName(elementType = String.class, splitOn = ";(\\s)?")
    private List<String> region = new ArrayList<>();

    @CsvBindAndSplitByName(elementType = String.class, splitOn = ";(\\s)?")
    private List<String> jurisdiction = new ArrayList<>();

    @CsvBindByName(column = "Provenance")
    private String provenance;

    @CsvBindByName(column = "Provenance Location ID")
    private String provenanceLocationId;

    @CsvBindByName(column = "Provenance Location Type")
    private String provenanceLocationType;

}
