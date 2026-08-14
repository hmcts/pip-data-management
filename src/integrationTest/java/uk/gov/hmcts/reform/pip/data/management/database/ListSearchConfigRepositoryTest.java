package uk.gov.hmcts.reform.pip.data.management.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("integration-jpa")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ListSearchConfigRepositoryTest {
    private static final String CASE_NUMBER_FIELD_NAME = "caseNumber";
    private static final String CASE_NAME_FIELD_NAME = "caseName";

    @Autowired
    ListSearchConfigRepository listSearchConfigRepository;

    @BeforeEach
    void setUp() {
        ListSearchConfig listSearchConfig = new ListSearchConfig();
        listSearchConfig.setListType(ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST);
        listSearchConfig.setCaseNumberFieldName(CASE_NUMBER_FIELD_NAME);
        listSearchConfig.setCaseNameFieldName(CASE_NAME_FIELD_NAME);
        listSearchConfigRepository.save(listSearchConfig);
    }

    @Test
    void shouldFindListSearchConfigByListType() {
        Optional<ListSearchConfig> listSearchConfig = listSearchConfigRepository.findByListType(
            ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST
        );
        assertThat(listSearchConfig)
            .as("List search config should be found")
            .isPresent();
        assertThat(listSearchConfig.get().getListType())
            .isEqualTo(ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST);
        assertThat(listSearchConfig.get().getCaseNumberFieldName())
            .isEqualTo(CASE_NUMBER_FIELD_NAME);
        assertThat(listSearchConfig.get().getCaseNameFieldName())
            .isEqualTo(CASE_NAME_FIELD_NAME);
    }

    @Test
    void shouldNotFindListSearchConfigByListTypeIfNotExists() {
        assertThat(listSearchConfigRepository.findByListType(ListType.MAGISTRATES_PUBLIC_LIST))
            .as("List search config should not be found")
            .isEmpty();
    }
}
