package uk.gov.hmcts.reform.pip.data.management.service.publication;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PublicationSummaryGenerationService {
    /**
     * Generate the list summary for GOV.UK Notify subscription email template.
     *
     * @param data - the summary data used to generate the summary.
     * @return the summary string
     */
    public String generate(Map<String, List<Map<String, String>>> data) {
        StringBuilder output = new StringBuilder(256);
        // The summary data consist of sections.
        // If the section heading value is 'null', each section consists of a single case only, and each case is
        // separated from the adjacent cases by a horizontal line. Most list types are in this category.
        // If the section has a heading, the heading will be bold. Each section can contain one or more cases, and
        // each section is separated from adjacent sections by a horizontal lines.
        data.entrySet()
            .forEach(summarySection -> {
                if (summarySection.getKey() == null) {
                    summarySection.getValue().forEach(
                        summaryCase -> output
                            .append("---\n")
                            .append(formatSummaryField(summaryCase))
                            .append("\n\n")
                    );
                } else {
                    output
                        .append("---\n##")
                        .append(summarySection.getKey())
                        .append('\n');

                    summarySection.getValue()
                        .forEach(
                            summaryCase -> output
                                .append(formatSummaryField(summaryCase))
                                .append("\n\n")
                        );
                }
            });

        return output.toString();
    }

    private String formatSummaryField(Map<String, String> summaryCase) {
        StringBuilder summaryFields = new StringBuilder(20);
        summaryCase.entrySet().forEach(
            field -> summaryFields
                .append(field.getKey())
                .append(" - ")
                .append(field.getValue())
                .append('\n')
        );
        return summaryFields.toString();
    }
}
