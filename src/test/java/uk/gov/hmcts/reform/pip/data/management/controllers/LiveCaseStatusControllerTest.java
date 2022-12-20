package uk.gov.hmcts.reform.pip.data.management.controllers;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationTestConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.LiveCaseStatus;
import uk.gov.hmcts.reform.pip.data.management.service.LiveCaseStatusService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.LiveCaseHelper.createMockLiveCaseList;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class LiveCaseStatusControllerTest {

    private List<LiveCaseStatus> lcsu;

    @Mock
    private LiveCaseStatusService liveCaseStatusService;

    @InjectMocks
    private LiveCaseStatusUpdatesController lcsuController;

    @BeforeEach
    void setup() {
        lcsu = createMockLiveCaseList();

        when(liveCaseStatusService.handleLiveCaseRequest(1)).thenReturn(lcsu);
    }

    @Test
    void testLiveCaseListReturned() {
        assertEquals(lcsu, lcsuController.getLiveCaseStatus(1).getBody(),
                     "Live cases should match"
        );
    }
}
