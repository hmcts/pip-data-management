package uk.gov.hmcts.reform.pip.data.management.models.publication.views;

/**
 * A view class that restricts what is returned back from our external endpoints.
 */
public class ArtefactView { //NOSONAR

    /**
     * The external view.
     */
    public static class External { //NOSONAR
    }

    /**
     * The internal view.
     */
    public static class Internal extends External {
    }

}
