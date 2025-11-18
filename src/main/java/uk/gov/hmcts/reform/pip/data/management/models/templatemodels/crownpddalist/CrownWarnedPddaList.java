package uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.CrownWarnedPddaListHelper.DATE_FORMATTER;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrownWarnedPddaList {
    private String fixedDate;
    private String caseReference;
    private String defendantNames;
    private String prosecutingAuthority;
    private String linkedCases;
    private String listingNotes;

    public LocalDate getFixedDateAsLocalDate() {
        if (fixedDate == null || fixedDate.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(fixedDate, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
