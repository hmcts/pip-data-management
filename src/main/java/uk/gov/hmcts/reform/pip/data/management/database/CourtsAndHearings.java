package uk.gov.hmcts.reform.pip.data.management.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@Data
@Slf4j
public class CourtsAndHearings {

    private List<Court> listCourts;

    private List<Hearing> listHearings;

    public CourtsAndHearings() throws IOException {
        ObjectMapper om = new ObjectMapper().setDateFormat(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH));

        InputStream fileHearings = this.getClass().getClassLoader().getResourceAsStream("mocks/hearingsList.json");
        InputStream fileCourts = this.getClass().getClassLoader()
            .getResourceAsStream("mocks/courtsAndHearingsCount.json");

        Hearing[] list = new Hearing[0];
        Court[] courts = new Court[0];
        try {
            list = om.readValue(fileHearings, Hearing[].class);
            courts = om.readValue(fileCourts, Court[].class);
        } catch (IOException e) {
            log.error(e.getMessage()); // avoiding handling with throw away solution
        } finally {
            fileCourts.close();
            fileHearings.close();
        }
        this.listHearings = Arrays.asList(list);
        this.listCourts = Arrays.asList(courts);
        this.buildCourts();
    }

    private void buildCourts() {
        this.listCourts.forEach(court -> court.setHearingList(this.getListHearings(court.getCourtId())));
    }

    public List<Hearing> getListHearings(int courtId) {
        return this.listHearings.stream()
            .filter(h -> h.getCourtId().equals(courtId))
            .collect(Collectors.toList());
    }


}
