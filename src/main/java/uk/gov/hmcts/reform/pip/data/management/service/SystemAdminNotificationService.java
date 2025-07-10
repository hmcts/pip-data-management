package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.pip.model.system.admin.ActionResult;
import uk.gov.hmcts.reform.pip.model.system.admin.ChangeType;

import java.util.List;
import java.util.stream.Stream;

public class SystemAdminNotificationService {
    private final AccountManagementService accountManagementService;
    private final PublicationServicesService publicationServicesService;

    @Autowired
    public SystemAdminNotificationService(AccountManagementService accountManagementService,
                                          PublicationServicesService publicationServicesService) {
        this.accountManagementService = accountManagementService;
        this.publicationServicesService = publicationServicesService;
    }

    public void sendEmailNotification(String requesterEmail, ActionResult actionResult,
                                      String additionalDetails, ChangeType changeType) throws JsonProcessingException {
        List<String> systemAdminsAad = accountManagementService.getAllAccounts("PI_AAD", "SYSTEM_ADMIN");
        List<String> systemAdminsSso = accountManagementService.getAllAccounts("SSO", "SYSTEM_ADMIN");
        List<String> systemAdmins = Stream.concat(systemAdminsAad.stream(), systemAdminsSso.stream()).toList();
        publicationServicesService.sendSystemAdminEmail(systemAdmins, requesterEmail, actionResult,
                                                        additionalDetails, changeType);
    }
}
