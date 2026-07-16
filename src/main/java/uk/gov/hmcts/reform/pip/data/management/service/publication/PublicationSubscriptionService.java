package uk.gov.hmcts.reform.pip.data.management.service.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactSearchRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ProcessingException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;
import uk.gov.hmcts.reform.pip.data.management.service.ListConversionFactory;
import uk.gov.hmcts.reform.pip.data.management.service.artefactsummary.ArtefactSummaryData;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactCaseInfo;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PublicationSubscriptionService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final PublicationRetrievalService publicationRetrievalService;
    private final PublicationSummaryGenerationService publicationSummaryGenerationService;
    private final ListConversionFactory listConversionFactory;
    private final ArtefactRepository artefactRepository;
    private final ArtefactSearchRepository artefactSearchRepository;
    private final AccountManagementService accountManagementService;

    public PublicationSubscriptionService(PublicationRetrievalService publicationRetrievalService,
                                          PublicationSummaryGenerationService publicationSummaryGenerationService,
                                          ListConversionFactory listConversionFactory,
                                          ArtefactRepository artefactRepository,
                                          ArtefactSearchRepository artefactSearchRepository,
                                          AccountManagementService accountManagementService) {
        this.publicationRetrievalService = publicationRetrievalService;
        this.publicationSummaryGenerationService = publicationSummaryGenerationService;
        this.listConversionFactory = listConversionFactory;
        this.artefactRepository = artefactRepository;
        this.artefactSearchRepository = artefactSearchRepository;
        this.accountManagementService = accountManagementService;
    }

    /**
     * Checks if the artefact has a display from date of today or previous then triggers the sub fulfilment
     * process on account-management if appropriate.
     */
    public void checkAndTriggerPublicationSubscription(Artefact artefact) {
        if (artefact.getDisplayFrom().toLocalDate().isBefore(LocalDate.now().plusDays(1))
            && (artefact.getDisplayTo() == null
            || artefact.getDisplayTo().toLocalDate().isAfter(LocalDate.now().minusDays(1)))) {
            // For scheduled subscription list types, send to API subscribers only during publication upload.
            // Notify email subscribers once a day only at a scheduled time.
            if (artefact.getListType().isScheduledSubscription()) {
                accountManagementService.sendArtefactForApiSubscription(convertArtefactToSharedModel(artefact));
            } else {
                accountManagementService.sendArtefactForAllSubscriptionsV2(convertArtefactToSharedModel(artefact));
            }
        }
    }

    /**
     * Scheduled method that checks daily for:
     * - newly dated from artefacts.
     * - certain list types where subscription is sent daily at a set time.
     */
    public void checkNewlyActiveArtefacts(boolean scheduledListType) {
        List<Artefact> artefacts;
        // For scheduled subscription list types, the API subscribers have already been notified at publication upload,
        // so only trigger email subscriptions here.
        if (scheduledListType) {
            Set<String> listTypesToTrigger = new HashSet<>();
            EnumSet.allOf(ListType.class).forEach(listType -> {
                if (listType.isScheduledSubscription()) {
                    listTypesToTrigger.add(listType.name());
                }
            });
            artefacts = artefactRepository.findActiveArtefactsByListTypeIn(listTypesToTrigger, LocalDate.now(),
                                                                           LocalDateTime.now());
            artefacts.forEach(artefact -> accountManagementService.sendArtefactForEmailSubscriptionV2(
                convertArtefactToSharedModel(artefact)
            ));
        } else {
            artefacts = artefactRepository.findArtefactsByDisplayFrom(LocalDate.now(), LocalDateTime.now())
                .stream()
                .toList();
            artefacts.forEach(a -> {
                if (a.getListType().isScheduledSubscription()) {
                    accountManagementService.sendArtefactForApiSubscription(convertArtefactToSharedModel(a));
                } else {
                    accountManagementService.sendArtefactForAllSubscriptionsV2(convertArtefactToSharedModel(a));
                }
            });
        }
    }

    public void sendDeleteArtefactForApiSubscription(Artefact artefact) {
        accountManagementService.sendDeletedArtefactForThirdParties(convertArtefactToSharedModel(artefact));
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

    public uk.gov.hmcts.reform.pip.model.publication.Artefact convertArtefactToSharedModel(Artefact artefact) {
        List<ArtefactCaseInfo> artefactCaseInfo = new ArrayList<>();
        artefactSearchRepository.findByArtefactId(artefact.getArtefactId()).forEach(row -> {
            artefactCaseInfo.add(ArtefactCaseInfo.builder()
                .caseNumber(row.getCaseNumber())
                .caseName(row.getCaseName())
                .build());
        });

        return uk.gov.hmcts.reform.pip.model.publication.Artefact.builder()
            .artefactId(artefact.getArtefactId())
            .listType(artefact.getListType())
            .locationId(artefact.getLocationId())
            .isFlatFile(artefact.getIsFlatFile())
            .payload(artefact.getPayload())
            .provenance(artefact.getProvenance())
            .sourceArtefactId(artefact.getSourceArtefactId())
            .type(artefact.getType())
            .contentDate(artefact.getContentDate())
            .sensitivity(artefact.getSensitivity())
            .language(artefact.getLanguage())
            .displayFrom(artefact.getDisplayFrom())
            .displayTo(artefact.getDisplayTo())
            .payloadSize(artefact.getPayloadSize())
            .supersededCount(artefact.getSupersededCount())
            .caseInfoList(artefactCaseInfo)
            .build();
    }
}
