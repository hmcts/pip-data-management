package uk.gov.hmcts.reform.pip.data.management.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.awaitility.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.Application;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class})
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class SchedulerTest {

    @SpyBean
    private PublicationService publicationService;

    @Test
    void testSchedulerNewlyActiveArtefactRuns() {
        await().atMost(Duration.ONE_MINUTE).untilAsserted(() -> verify(publicationService, times(1))
            .checkNewlyActiveArtefacts());
    }

    @Test
    void testSchedulerRunDailyTasksRuns() {
        await().atMost(Duration.ONE_MINUTE).untilAsserted(() -> verify(publicationService, times(1))
            .runDailyTasks());
    }
}
