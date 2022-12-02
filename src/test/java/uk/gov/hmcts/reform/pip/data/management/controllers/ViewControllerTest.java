package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.service.ViewService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ViewControllerTest {

    @Mock
    private ViewService viewService;

    @InjectMocks
    private ViewController viewController;

    @Test
    void testResponseFromViewService() {
        ResponseEntity<Void> response = viewController.refreshView();
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Unknown response message returned from controller");
        verify(viewService, times(1)).refreshView();
    }

}
