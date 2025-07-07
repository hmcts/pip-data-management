package uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.nonstrategic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.pip.data.management.service.ListConversionFactory;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.ArtefactSummaryData;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class NonStrategicCommonArtefactSummaryTestConfig {

    protected static final String SUMMARY_CASES_MESSAGE = "Summary cases count does not match";
    protected static final String NON_STRATEGIC_RESOURCE_FOLDER = "src/test/resources/mocks/non-strategic/";
    protected static final String SUMMARY_SECTIONS_MESSAGE = "Summary sections count does not match";
    protected static final String SUMMARY_FIELDS_MESSAGE = "Summary fields count does not match";
    protected static final String SUMMARY_FIELD_KEY_MESSAGE = "Summary field key does not match";
    protected static final String SUMMARY_FIELD_VALUE_MESSAGE = "Summary field value does not match";


    protected Map<String, List<Map<String, String>>> getArtefactSummaryOutput(String listPath,
                                                                              ListType listType) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get(
                NON_STRATEGIC_RESOURCE_FOLDER,
                listPath
            )), writer,
            Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        ArtefactSummaryData cstSummaryData = new ListConversionFactory()
            .getArtefactSummaryData(listType)
            .get();
        return cstSummaryData.get(payload);
    }

}
