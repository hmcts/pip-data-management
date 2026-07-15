package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratespubliclist;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Sitting {
    private String time;
    private List<Hearing> hearings = new ArrayList<>();
}
