package uk.gov.hmcts.reform.pip.data.management.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.Event;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.LiveCaseStatus;

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
    private List<LiveCaseStatus> listLiveCases;
    private List<Event> listCourtEvents;

    public CourtsAndHearings() {
        ObjectMapper om = new ObjectMapper().setDateFormat(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH));

        Hearing[] list = new Hearing[0];
        Court[] courts = new Court[0];
        LiveCaseStatus[] liveCaseStatuses = new LiveCaseStatus[0];
        Event[] courtStatusEvents = new Event[0];
        try (
            InputStream fileHearings = this.getClass().getClassLoader()
                .getResourceAsStream("mocks/hearingsList.json");
            InputStream fileCourts = this.getClass().getClassLoader()
                .getResourceAsStream("mocks/courtsAndHearingsCount.json");
            InputStream liveCases = this.getClass().getClassLoader()
                .getResourceAsStream("mocks/liveCaseStatusUpdates.json");
            InputStream courtEventStatuses = this.getClass().getClassLoader()
                .getResourceAsStream("mocks/CourtEventStatusDescription.json")) {
            list = om.readValue(fileHearings, Hearing[].class);
            courts = om.readValue(fileCourts, Court[].class);
            liveCaseStatuses = om.readValue(liveCases, LiveCaseStatus[].class);
            courtStatusEvents = om.readValue(courtEventStatuses, Event[].class);
        } catch (IOException e) {
            log.error(e.getMessage()); // avoiding handling with throw away solution
        }

        this.listHearings = Arrays.asList(list);
        this.listCourts = Arrays.asList(courts);
        this.listLiveCases = Arrays.asList(liveCaseStatuses);
        this.listCourtEvents = Arrays.asList(courtStatusEvents);

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

    public List<LiveCaseStatus> getLiveCaseStatus(int courtId) {
        return this.listLiveCases.stream()
            .filter(lcsu -> lcsu.getCourtId().equals(courtId))
            .collect(Collectors.toList());
    }
}
