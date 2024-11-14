package uk.gov.hmcts.reform.pip.data.management.utils;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.data.management.service.SubscriptionManagementService;

@MockitoSettings(strictness = Strictness.LENIENT)
public class IntegrationTestBase extends IntegrationCommonTestBase {
    @MockBean
    protected SubscriptionManagementService subscriptionManagementService;

    @MockBean
    protected PublicationServicesService publicationServicesService;
}
