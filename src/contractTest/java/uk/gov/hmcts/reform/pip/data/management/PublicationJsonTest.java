package uk.gov.hmcts.reform.pip.data.management.contractTest;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import com.azure.storage.blob.BlobContainerClient;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.config.PactTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AuthorisationService;
import uk.gov.hmcts.reform.pip.data.management.service.ValidationService;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationCreationRunner;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationCreationService;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("pact")
@AutoConfigureMockMvc(addFilters = false)
@Import(PactTestConfiguration.class)
@Provider("data-management-service")
@PactBroker(url = "http://localhost:9292")
public class PublicationJsonTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private AuthorisationService authorisationService;

    @MockitoBean
    private ValidationService validationService;

    @MockitoBean
    private PublicationCreationRunner publicationCreationRunner;

    @MockitoBean
    private PublicationCreationService publicationCreationService;

    @MockitoBean
    @Qualifier("artefact")
    private BlobContainerClient artefactBlobContainerClient;

    @MockitoBean
    @Qualifier("publications")
    private BlobContainerClient publicationsBlobContainerClient;

    @BeforeEach
    void setup(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @State("user is authorised to upload JSON publication")
    public void userIsAuthorisedToUploadJsonPublication() {
        when(authorisationService.userCanUploadPublication(any(), anyString()))
            .thenReturn(true);

        when(validationService.validateHeaders(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(validationService).validateBody(anyString(), any(), anyBoolean());

        Artefact mockArtefact = createMockArtefact();
        when(publicationCreationRunner.run(any(Artefact.class), anyString(), anyBoolean()))
            .thenReturn(mockArtefact);
    }

    private Artefact createMockArtefact() {
        LocalDateTime TODAY = LocalDateTime.now();
        LocalDateTime TOMORROW = TODAY.plusDays(1);
        LocalDateTime YESTERDAY = TODAY.minusDays(1);

        Artefact artefact = new Artefact();
        artefact.setArtefactId(UUID.randomUUID());
        artefact.setProvenance("MANUAL_UPLOAD");
        artefact.setType(ArtefactType.LIST);
        artefact.setSensitivity(Sensitivity.PUBLIC);
        artefact.setLanguage(Language.ENGLISH);
        artefact.setLocationId("9");
        artefact.setContentDate(LocalDateTime.now());
        artefact.setListType(ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST);
        artefact.setSourceArtefactId("test-source-id");
        artefact.setDisplayFrom(YESTERDAY);
        artefact.setDisplayTo(TOMORROW);

        return artefact;
    }
}
