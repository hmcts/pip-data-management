package uk.gov.hmcts.reform.pip.data.management.utils;


import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;

public class IntegrationBasicTestBase extends IntegrationCommonTestBase {
    @MockBean
    LocationRepository locationRepository;

    @MockBean
    ArtefactRepository artefactRepository;
}
