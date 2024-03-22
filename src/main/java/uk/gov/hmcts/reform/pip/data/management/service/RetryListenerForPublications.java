package uk.gov.hmcts.reform.pip.data.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.interceptor.MethodInvocationRetryCallback;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;

@Service
public class RetryListenerForPublications implements RetryListener {

    @Autowired
    AzureBlobService azureBlobService;

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
                                                 Throwable throwable) {
        Object[] callbackArguments = ((MethodInvocationRetryCallback) callback).getInvocation().getArguments();
        Artefact artefact = ((Artefact) callbackArguments[0]);

        if (artefact.getPayload() != null) {
            azureBlobService.deleteBlob(ArtefactHelper.getUuidFromUrl(artefact.getPayload()));
        }
    }
}
