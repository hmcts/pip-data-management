package uk.gov.hmcts.reform.pip.data.management.models.external.account.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.pip.data.management.models.request.Roles;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AzureAccount {

    /**
     * The is the object ID that is returned from azure.
     */
    private String azureAccountId;

    /**
     * The email address for the account.
     */
    private String email;

    /**
     * The first name of the account.
     */
    private String firstName;

    /**
     * The surname of the account.
     */
    private String surname;

    /**
     * The role of the account.
     */
    private Roles role;

    private String displayName;
}
