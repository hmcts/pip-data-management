package uk.gov.hmcts.reform.pip.data.management.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.pip.data.management.models.court.NewCourt;

@Repository
public interface CourtRepository extends JpaRepository<NewCourt, Long> {

}
