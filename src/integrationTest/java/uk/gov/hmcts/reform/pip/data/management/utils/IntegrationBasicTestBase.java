package uk.gov.hmcts.reform.pip.data.management.utils;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactArchivedRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ListSearchConfigRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationMetadataRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;

public class IntegrationBasicTestBase extends IntegrationCommonTestBase {
    @MockitoBean
    LocationRepository locationRepository;

    @MockitoBean
    ArtefactRepository artefactRepository;

    @MockitoBean
    ArtefactArchivedRepository artefactArchivedRepository;

    @MockitoBean
    ListSearchConfigRepository listSearchConfigRepository;

    @MockitoBean
    protected LocationMetadataRepository locationMetadataRepository;
}
