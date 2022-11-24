package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ViewServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ArtefactRepository artefactRepository;

    @InjectMocks
    private ViewService viewService;

    @Test
    void refreshViewTest() {
        viewService.refreshView();
        verify(locationRepository, times(1)).refreshLocationView();
        verify(artefactRepository, times(1)).refreshArtefactView();

    }

}
