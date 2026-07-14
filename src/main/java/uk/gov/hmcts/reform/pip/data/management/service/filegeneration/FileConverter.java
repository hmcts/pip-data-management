package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.output.StringBuilderWriter;
import org.slf4j.Logger;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@FunctionalInterface
public interface FileConverter {
    Logger log = getLogger(FileConverter.class);

    /**
     * Interface method that captures the conversion of an artefact to a Html File.
     *
     * @return The converted HTML as a string;
     */
    String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) throws IOException;

    /**
     * Interface method that captures the conversion of an artefact to an Excel spreadsheet.
     *
     * @return The converted Excel spreadsheet as a byte array.
     */
    default byte[] convertToExcel(JsonNode artefact, ListType listType) throws IOException {
        return new byte[0];
    }

    default byte[] convertToExcel(JsonNode artefact, ListType listType, Language language) throws IOException {
        return new byte[0];
    }

    /**
     * Interface method that captures the conversion of an artefact to CSV.
     *
     * @return The converted Excel spreadsheet as a byte array.
     */
    default byte[] convertToCsv(List<String> headers, List<List<String>> rows) throws IOException {
        Writer writer = new StringBuilderWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('"')
            .withEscape('\\')
            .withQuoteMode(QuoteMode.ALL)
            .builder()
            .setHeader(headers.stream().toArray(String[]::new))
            .build();

        try (final CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
            rows.forEach((row -> {
                try {
                    printer.printRecord(row);
                } catch (IOException e) {
                    log.error(writeLog("Unable to write CSV record: " + row));
                }
            }));
        }

        return writer.toString().getBytes();
    }
}
