package uk.gov.hmcts.reform.pip.data.management.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.models.Hearing;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.CaseEventGlossary;
import uk.gov.hmcts.reform.pip.data.management.models.lcsu.LiveCaseStatus;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
@Data
@Slf4j
public class CourtsAndHearings {

    private List<Hearing> listHearings;
    private List<LiveCaseStatus> listLiveCases;
    private List<CaseEventGlossary> listCaseEventGlossary;

    public CourtsAndHearings() {
        ObjectMapper om = new ObjectMapper().setDateFormat(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH));

        Hearing[] list = new Hearing[0];
        LiveCaseStatus[] liveCaseStatuses = new LiveCaseStatus[0];
        CaseEventGlossary[] caseEventGlossaries = new CaseEventGlossary[0];
        try (
            InputStream fileHearings = this.getClass().getClassLoader()
                .getResourceAsStream("mocks/hearingsList.json");
            InputStream liveCases = this.getClass().getClassLoader()
                .getResourceAsStream("mocks/liveCaseStatusUpdates.json");
            InputStream courtEventStatuses = this.getClass().getClassLoader()
                .getResourceAsStream("mocks/CaseEventGlossary.json")) {
            list = om.readValue(fileHearings, Hearing[].class);
            liveCaseStatuses = om.readValue(liveCases, LiveCaseStatus[].class);
            caseEventGlossaries = om.readValue(courtEventStatuses, CaseEventGlossary[].class);
        } catch (IOException e) {
            log.error(e.getMessage()); // avoiding handling with throw away solution
        }

        this.listHearings = Arrays.asList(list);
        this.listLiveCases = Arrays.asList(liveCaseStatuses);
        this.listCaseEventGlossary = Arrays.asList(caseEventGlossaries);
    }

    public List<Hearing> getListHearings(int courtId) {
        return this.listHearings.stream()
            .filter(h -> h.getCourtId().equals(courtId))
            .toList();
    }

    public List<LiveCaseStatus> getLiveCaseStatus(int courtId) {
        return this.listLiveCases.stream()
            .filter(lcsu -> lcsu.getCourtId().equals(courtId))
            .toList();
    }
}
