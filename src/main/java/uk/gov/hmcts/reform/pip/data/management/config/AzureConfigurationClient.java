package uk.gov.hmcts.reform.pip.data.management.config;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Configuration
public class AzureConfigurationClient {

    private TableConfiguration tableConfiguration;

    @Autowired
    public AzureConfigurationClient(TableConfiguration tableConfiguration) {
            this.tableConfiguration = tableConfiguration;
    }

    @Bean
    public TableClient tableClient() {
        return new TableClientBuilder()
            .connectionString(tableConfiguration.getConnectionString())
            .tableName(tableConfiguration.getTableName())
            .buildClient();
    }
}
