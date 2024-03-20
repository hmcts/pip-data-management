package uk.gov.hmcts.reform.pip.data.management.database;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;

@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ArtefactRepositoryTest {

    @Autowired
    private ArtefactRepository artefactRepository;

    private Artefact artefact;
    private Artefact artefact2;
    @BeforeAll
    void setup() {
        artefact = Artefact.builder()
            .provenance("MANUAL_UPLOAD")
            .sourceArtefactId("")
            .type(ArtefactType.LIST)
            .sensitivity(Sensitivity.PUBLIC)
            .language(Language.ENGLISH)
            .displayFrom(LocalDateTime.now())
            .displayTo(LocalDateTime.now().plusDays(1))
            .listType(ListType.SJP_PRESS_LIST)
            .locationId("9")
            .contentDate(LocalDateTime.now())
            .expiryDate(LocalDateTime.now().plusDays(1))
            .build();

        artefact2 = Artefact.builder()
            .provenance("MANUAL_UPLOAD")
            .sourceArtefactId("")
            .type(ArtefactType.LIST)
            .sensitivity(Sensitivity.PUBLIC)
            .language(Language.ENGLISH)
            .displayFrom(LocalDateTime.now())
            .displayTo(LocalDateTime.now().plusDays(1))
            .listType(ListType.SJP_PRESS_LIST)
            .locationId("9")
            .contentDate(LocalDateTime.now().plusDays(1))
            .expiryDate(LocalDateTime.now().plusDays(1))
            .build();
    }


    @Test
    void test() {
       Artefact createItem = artefactRepository.save(artefact);

        Artefact createItem2 = artefactRepository.save(artefact);
    }
}
