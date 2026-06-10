package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListSearchConfig;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListSearchConfigRepository extends JpaRepository<ListSearchConfig, UUID> {
    Optional<ListSearchConfig> findByListType(ListType listType);

    List<ListSearchConfig> findListSearchConfigByListType(ListType listType);
}
