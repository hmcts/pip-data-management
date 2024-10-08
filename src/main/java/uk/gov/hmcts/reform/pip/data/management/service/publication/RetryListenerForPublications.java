package uk.gov.hmcts.reform.pip.data.management.service.publication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.interceptor.MethodInvocationRetryCallback;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

@Service
public class RetryListenerForPublications implements RetryListener {

    private final AzureArtefactBlobService azureArtefactBlobService;

    @Autowired
    public RetryListenerForPublications(AzureArtefactBlobService azureArtefactBlobService) {
        this.azureArtefactBlobService = azureArtefactBlobService;
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
                                                 Throwable throwable) {
        Object[] callbackArguments = ((MethodInvocationRetryCallback) callback).getInvocation().getArguments();
        Artefact artefact = (Artefact) callbackArguments[0];

        if (artefact.getPayload() != null) {
            azureArtefactBlobService.deleteBlob(ArtefactHelper.getUuidFromUrl(artefact.getPayload()));
        }
    }
}
