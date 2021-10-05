package uk.gov.hmcts.reform.pip.data.management.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.pip.data.management.service.LiveCaseStatusService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.data.management.helpers.LiveCaseHelper.createMockLiveCaseList;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LiveCaseStatusControllerTest {

    @MockBean
    private LiveCaseStatusService liveCaseStatusService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        when(liveCaseStatusService.handleLiveCaseRequest(1)).thenReturn(createMockLiveCaseList());
    }

    @Test
    public void testLiveCaseListReturned() throws Exception {
        mockMvc.perform(get("/lcsu/1"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("1")));
    }
}
