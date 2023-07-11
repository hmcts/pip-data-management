package uk.gov.hmcts.reform.pip.data.management.models.publication;

import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that represents a group of headers to be validated in the ValidationService.
 */
@Data
@AllArgsConstructor
public class HeaderGroup {

    /**
     * Name of source system.
     */
    @Valid
    @NotNull
    @NotBlank
    private String provenance;

    /**
     * Unique of ID of what publication is called by source system.
     */
    @Valid
    @NotNull
    @NotBlank
    private String sourceArtefactId;

    /**
     * List / Outcome / Judgement / Status Update.
     */
    @Enumerated
    @NotBlank
    private ArtefactType type;

    /**
     * Level of sensitivity of publication.
     */
    @Enumerated
    private Sensitivity sensitivity;

    /**
     * Language of publication.
     */
    @Enumerated
    private Language language;

    /**
     * Date / Time from which the publication will be displayed.
     */
    @Valid
    private LocalDateTime displayFrom;

    /**
     * Date / Time until which the publication will be displayed.
     */
    @Valid
    private LocalDateTime displayTo;

    /**
     * DL / SL / WL / FL / PL / SJP.
     */
    @Enumerated
    private ListType listType;

    /**
     * The court id from the source system.
     */
    @Valid
    @NotNull
    @NotBlank
    private String courtId;

    /**
     * The local date time the publication is referring to.
     */
    @Valid
    private LocalDateTime contentDate;

    /**
     * Returns a map of the headers used to log within app insights.
     * @return A map of headers
     */
    public Map<String, String> getAppInsightsHeaderMap() {
        Map<String, String> map = new ConcurrentHashMap<>();
        map.computeIfAbsent("PROVENANCE", val -> provenance);
        map.computeIfAbsent("SOURCE_ARTEFACT_ID", val -> sourceArtefactId);
        map.computeIfAbsent("TYPE", val -> type.toString());
        map.computeIfAbsent("SENSITIVITY", val -> sensitivity.toString());
        map.computeIfAbsent("LANGUAGE", val -> language.toString());
        if (displayFrom != null) {
            map.computeIfAbsent("DISPLAY_FROM", val -> displayFrom.toString());
        }

        if (displayTo != null) {
            map.computeIfAbsent("DISPLAY_TO", val -> displayTo.toString());
        }
        map.computeIfAbsent("LIST_TYPE", val -> listType.toString());
        map.computeIfAbsent("COURT_ID", val -> courtId);
        map.computeIfAbsent("CONTENT_DATE", val -> contentDate.toString());
        return map;
    }

}
