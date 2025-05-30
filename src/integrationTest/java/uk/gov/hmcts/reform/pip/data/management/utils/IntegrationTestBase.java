package uk.gov.hmcts.reform.pip.data.management.utils;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.LocationMetadataService;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;

@MockitoSettings(strictness = Strictness.LENIENT)
public class IntegrationTestBase extends IntegrationCommonTestBase {
    @MockitoBean
    protected AccountManagementService accountManagementService;

    @MockitoBean
    protected PublicationServicesService publicationServicesService;

    @MockitoBean
    protected LocationMetadataService locationMetaDataService;
}
