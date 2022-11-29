package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.SjpPublicList;

@Repository
public interface SjpPublicListRepository extends KeyValueRepository<SjpPublicList, Long> {

    Page<SjpPublicList> findALl(Pageable pageable);
}

