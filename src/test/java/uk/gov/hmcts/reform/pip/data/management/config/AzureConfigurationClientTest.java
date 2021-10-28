package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("test")
public class AzureConfigurationClientTest {

    @Mock
    TableClient tableClientMock;

    @Mock
    PagedIterable<TableEntity> tableEntitiesMock;

    public AzureConfigurationClientTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Bean
    public TableClient tableClient() {
        return tableClientMock;
    }

    @Bean
    public PagedIterable<TableEntity> tableEntities() {
        return tableEntitiesMock;
    }

}
