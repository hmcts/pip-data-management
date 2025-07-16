package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ProcessingException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.ListConversionFactory;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.ArtefactSummaryData;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PublicationSubscriptionService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final PublicationRetrievalService publicationRetrievalService;
    private final PublicationSummaryGenerationService publicationSummaryGenerationService;
    private final ListConversionFactory listConversionFactory;
    private final ArtefactRepository artefactRepository;
    private final AccountManagementService accountManagementService;

    public PublicationSubscriptionService(PublicationRetrievalService publicationRetrievalService,
                                          PublicationSummaryGenerationService publicationSummaryGenerationService,
                                          ListConversionFactory listConversionFactory,
                                          ArtefactRepository artefactRepository,
                                          AccountManagementService accountManagementService) {
        this.publicationRetrievalService = publicationRetrievalService;
        this.publicationSummaryGenerationService = publicationSummaryGenerationService;
        this.listConversionFactory = listConversionFactory;
        this.artefactRepository = artefactRepository;
        this.accountManagementService = accountManagementService;
    }

    /**
     * Checks if the artefact has a display from date of today or previous then triggers the sub fulfilment
     * process on account-management if appropriate.
     */
    public void checkAndTriggerPublicationSubscription(Artefact artefact) {
        //TODO: fully switch this logic to localdates once artefact model changes //NOSONAR
        if (artefact.getDisplayFrom().toLocalDate().isBefore(LocalDate.now().plusDays(1))
            && (artefact.getDisplayTo() == null
            || artefact.getDisplayTo().toLocalDate().isAfter(LocalDate.now().minusDays(1)))) {
            accountManagementService.sendArtefactForSubscription(artefact);
        }
    }

    /**
     * Scheduled method that checks daily for newly dated from artefacts.
     */
    public void checkNewlyActiveArtefacts() {
        artefactRepository.findArtefactsByDisplayFrom(LocalDate.now())
            .forEach(accountManagementService::sendArtefactForSubscription);
    }

    /**
     * Generate the artefact summary by provided artefact id.
     *
     * @param artefactId The artefact Id to generate the summary for.
     * @return A string of the generated summary
     */
    public String generateArtefactSummary(UUID artefactId) {
        Artefact artefact = publicationRetrievalService.getMetadataByArtefactId(artefactId);
        Optional<ArtefactSummaryData> artefactSummaryData =
            listConversionFactory.getArtefactSummaryData(artefact.getListType());

        if (artefactSummaryData.isEmpty()) {
            return "";
        }

        try {
            String rawJson = publicationRetrievalService.getPayloadByArtefactId(artefactId);
            Map<String, List<Map<String, String>>> summaryData = artefactSummaryData.get()
                .get(MAPPER.readTree(rawJson));

            return publicationSummaryGenerationService.generate(summaryData);
        } catch (JsonProcessingException ex) {
            throw new ProcessingException(String.format("Failed to generate summary for artefact id %s", artefactId));
        }
    }
}
