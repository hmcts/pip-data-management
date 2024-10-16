package uk.gov.hmcts.reform.pip.data.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Profile("disable-async")
public class AsyncConfiguration {

    @Primary
    @Bean
    public Executor asyncExecutor() {
        return new SyncTaskExecutor();
    }

}
