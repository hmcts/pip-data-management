package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.service.ExcelConversionService;

@Service
public class PublicationPayloadConversionService {
    private final ObjectMapper objectMapper;
    private final ExcelConversionService excelConversionService;

    @Autowired
    public PublicationPayloadConversionService(ExcelConversionService excelConversionService) {
        this.excelConversionService = excelConversionService;
        this.objectMapper = new ObjectMapper();
    }

    public String removeJsonFormatting(String payload) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(payload);
        return objectMapper.writeValueAsString(jsonNode);
    }

    public String convertExcelToJson(MultipartFile excelFile) {
        return excelConversionService.convert(excelFile);
    }

}
