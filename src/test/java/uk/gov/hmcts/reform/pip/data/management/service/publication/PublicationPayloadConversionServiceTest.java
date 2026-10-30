package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.service.ExcelConversionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ActiveProfiles(profiles = "test")
@ExtendWith(MockitoExtension.class)
class PublicationPayloadConversionServiceTest {
    private static final String PAYLOAD = " test payload";

    private MultipartFile file;

    @Mock
    private ExcelConversionService excelConversionService;

    @InjectMocks
    private PublicationPayloadConversionService publicationPayloadConversionService;

    @Test
    void testRemoveJsonFormattingSuccess() throws JsonProcessingException {
        String jsonString = "{\"key1\": \"value1\",\n  \"key2\": \"value2\"}";
        String expectedOutput = "{\"key1\":\"value1\",\"key2\":\"value2\"}";

        assertThat(publicationPayloadConversionService.removeJsonFormatting(jsonString))
            .isEqualTo(expectedOutput);
    }

    @Test
    void testRemoveJsonFormattingThrowsJsonProcessingException() {
        assertThatThrownBy(() -> publicationPayloadConversionService.removeJsonFormatting("invalidJson"))
            .isInstanceOf(JsonProcessingException.class);
    }

    @Test
    void testConvertExcelToJsonSuccess() {
        when(excelConversionService.convert(file)).thenReturn(PAYLOAD);

        assertThat(publicationPayloadConversionService.convertExcelToJson(file))
            .isEqualTo(PAYLOAD);
    }
}
