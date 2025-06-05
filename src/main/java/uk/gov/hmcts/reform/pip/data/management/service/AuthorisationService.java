package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.account.Roles;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Service("authorisationService")
@Slf4j
@SuppressWarnings("PMD.GodClass")
public class AuthorisationService {

    private final AccountManagementService accountManagementService;

    public AuthorisationService(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    public boolean userCanAddLocationMetadata(String requesterId) {
        if (!isUserSystemAdmin(requesterId)
            || !isAdmin()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to add location metadata", requesterId
                )));
            return false;
        }

        return true;
    }

    public boolean userCanUpdateLocationMetadata(String requesterId) {
        if (!isUserSystemAdmin(requesterId)
            || !isAdmin()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to update location metadata", requesterId
                )));
            return false;
        }

        return true;
    }

    public boolean userCanDeleteLocationMetadata(String requesterId) {
        if (!isUserSystemAdmin(requesterId)
            || !isAdmin()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to delete location metadata", requesterId
                )));
            return false;
        }

        return true;
    }

    public boolean userCanGetLocationMetadata(String requesterId) {
        if (!isUserSystemAdmin(requesterId)
            || !isAdmin()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to get location metadata", requesterId
                )));
            return false;
        }

        return true;
    }

    private boolean isUserSystemAdmin(String requesterId) {
        if (requesterId != null && !requesterId.isEmpty()) {
            PiUser user = accountManagementService.getUserById(requesterId);
            if (user != null && user.getRoles() != null) {
                return Roles.SYSTEM_ADMIN.equals(user.getRoles());
            }
        }

        return false;
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return hasAuthority(authentication, "APPROLE_api.request.admin");
    }

    private boolean hasAuthority(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
            .anyMatch(granted -> granted.getAuthority().equals(role));
    }
}
