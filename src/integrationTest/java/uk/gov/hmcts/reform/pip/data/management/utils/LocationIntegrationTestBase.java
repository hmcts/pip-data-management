package uk.gov.hmcts.reform.pip.data.management.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.model.account.PiUser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocationIntegrationTestBase extends IntegrationTestBase {
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String UPLOAD_API = "/locations/upload";
    private static final String LOCATION_LIST = "locationList";

    public static final String REQUESTER_ID_HEADER = "x-requester-id";
    private static final String SYSTEM_ADMIN_ID = UUID.randomUUID().toString();

    @Autowired
    protected MockMvc mockMvc;

    private static PiUser piUser;

    protected BiPredicate<Location, Location> compareLocationWithoutReference = (location, otherLocation) ->
        location.getLocationId().equals(otherLocation.getLocationId())
            && location.getName().equals(otherLocation.getName())
            && location.getRegion().equals(otherLocation.getRegion())
            && location.getJurisdiction().equals(otherLocation.getJurisdiction());

    @BeforeAll
    protected static void setup() {
        piUser = new PiUser();
        piUser.setUserId(SYSTEM_ADMIN_ID);
        piUser.setEmail("test@justice.gov.uk");
        piUser.setRoles(SYSTEM_ADMIN);

        OBJECT_MAPPER.findAndRegisterModules();
    }

    protected List<Location> createLocations(String locationsFile) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);
        try (InputStream csvInputStream = this.getClass().getClassLoader()
            .getResourceAsStream(locationsFile)) {
            MockMultipartFile csvFile
                = new MockMultipartFile(LOCATION_LIST, csvInputStream);

            MvcResult mvcResult = mockMvc.perform(multipart(UPLOAD_API).file(csvFile)
                .header(REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID))
                .andExpect(status().isOk()).andReturn();

            return Arrays.asList(
                OBJECT_MAPPER.readValue(mvcResult.getResponse().getContentAsString(), Location[].class));
        }
    }
}
