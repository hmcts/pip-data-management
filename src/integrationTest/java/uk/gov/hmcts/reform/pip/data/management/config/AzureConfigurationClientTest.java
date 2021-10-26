package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.data.tables.TableClient;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("test")
public class AzureConfigurationClientTest {

    @Mock
    TableClient tableClientMock;

    public AzureConfigurationClientTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Bean
    public TableClient tableClient() {
        return tableClientMock;
    }

}
