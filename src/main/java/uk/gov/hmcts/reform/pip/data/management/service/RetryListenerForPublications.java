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
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        //Needs tidying up, however in this method we should delete the blob that has been created in Azure storage,
        //as the transaction has failed
        String payloadUrl = ((Artefact)(((MethodInvocationRetryCallback<?, ?>) callback)
            .getInvocation().getArguments()[0])).getPayload();

        if (payloadUrl != null) {
            azureBlobService.deleteBlob(ArtefactHelper.getUuidFromUrl(payloadUrl));
        }
    }
}
