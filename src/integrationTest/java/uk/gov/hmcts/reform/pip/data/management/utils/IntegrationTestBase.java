package uk.gov.hmcts.reform.pip.data.management.utils;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.data.management.service.SubscriptionManagementService;

@MockitoSettings(strictness = Strictness.LENIENT)
public class IntegrationTestBase extends IntegrationCommonTestBase {
    @MockitoBean
    protected AccountManagementService accountManagementService;

    @MockitoBean
    protected SubscriptionManagementService subscriptionManagementService;

    @MockitoBean
    protected PublicationServicesService publicationServicesService;
}
