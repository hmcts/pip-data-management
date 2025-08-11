package uk.gov.hmcts.reform.pip.data.management.utils;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactArchivedRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationMetadataRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;

public class IntegrationBasicTestBase extends IntegrationCommonTestBase {
    @MockitoBean
    LocationRepository locationRepository;

    @MockitoBean
    ArtefactRepository artefactRepository;

    @MockitoBean
    ArtefactArchivedRepository artefactArchivedRepository;

    @MockitoBean
    protected LocationMetadataRepository locationMetadataRepository;
}
