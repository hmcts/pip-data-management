package uk.gov.hmcts.reform.pip.data.management.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;

import java.util.List;

//@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class LocationRepositoryTest {
    @Autowired
    LocationRepository locationRepository;

    @Test
    void test() {
        List<Location> locations = locationRepository.findAllByNameStartingWithIgnoreCase("Pr");
        System.out.println(locations);
    }
}
