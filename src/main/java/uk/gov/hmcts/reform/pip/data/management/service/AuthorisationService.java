package uk.gov.hmcts.reform.pip.data.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactService;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.account.Roles;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.util.UUID;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Service("authorisationService")
@Slf4j
@SuppressWarnings("PMD.GodClass")
public class AuthorisationService {

    private final AccountManagementService accountManagementService;
    private final ArtefactService artefactService;

    public AuthorisationService(AccountManagementService accountManagementService,
                                ArtefactService artefactService) {
        this.accountManagementService = accountManagementService;
        this.artefactService = artefactService;
    }

    public boolean userCanUploadPublication(String requesterId, String provenance) {
        if ("MANUAL_UPLOAD".equals(provenance)
            && !isUserAdmin(requesterId)) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to upload publication", requesterId
                )));
            return false;
        }

        if (!isPublisher()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to upload publication", requesterId
                )));
            return false;
        }

        return true;
    }

    public boolean userCanUploadLocation(String requesterId) {
        if (!isUserSystemAdmin(requesterId)
                || !isAdmin()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to upload location", requesterId
                )));
            return false;
        }

        return true;
    }

    public boolean userCanDeleteLocation(String requesterId) {
        if (!isUserSystemAdmin(requesterId)
                || !isAdmin()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to delete location", requesterId
                )));
            return false;
        }

        return true;
    }

    public boolean userCanGetPublicationsPerLocation(String requesterId) {
        if (!isUserSystemAdmin(requesterId)
            || !isAdmin()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to get publication per location", requesterId
                )));
            return false;
        }

        return true;
    }

    public boolean userCanGetLocationCsv(String requesterId) {
        if (!isUserSystemAdmin(requesterId)
                || !isAdmin()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to download location CSV", requesterId
                )));
            return false;
        }

        return true;
    }

    public boolean userCanAccessPublication(UUID requesterId, UUID artefactId, boolean system) {
        if (!isAdmin()) {
            log.error(writeLog(
                "Api token permission is not authorised to access artefact meta data"));
            return false;
        }

        Artefact artefact = artefactService.getMetadataByArtefactId(artefactId);
        if (!isAuthorised(artefact, requesterId, system)) {
            log.error(writeLog(
                String.format("User with id %s is not authorised to access artefact with id %s", requesterId, artefactId
                )));
            return false;
        }
        return true;
    }

    public boolean userCanAccessPublicationData(UUID requesterId, UUID artefactId, boolean systemOrAdmin) {
        if (!isAdmin()) {
            log.error(writeLog(
                "Api token permission is not authorised to access artefact meta data"));
            return false;
        }

        Artefact artefact = artefactService.getMetadataByArtefactId(artefactId);
        if (!isAuthorised(artefact, requesterId, systemOrAdmin)) {
            log.error(writeLog(
                String.format("User with id %s is not authorised to access artefact meta data with id %s",
                              requesterId, artefactId)));
            return false;
        }
        return true;
    }

    public boolean userCanSearchInPublicationData() {
        if (!isAdmin()) {
            log.error(writeLog("Action Search in publication is not allowed"));
            return false;
        }
        return true;
    }

    public boolean userCanSearchPublicationForLocation() {
        if (!isAdmin()) {
            log.error(writeLog("Action Search in publication for Location is not allowed"));
            return false;
        }
        return true;
    }

    public boolean userCanArchivePublications(String requesterId) {
        if (!isUserAdmin(requesterId)
            || !isAdmin()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to archive the publication", requesterId
                )));
            return false;
        }

        return true;
    }

    public boolean userCanDeletePublicationsByLocation(String requesterId) {
        if (!isUserSystemAdmin(requesterId)
            || !isAdmin()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to delete the publications by location", requesterId
                )));
            return false;
        }

        return true;
    }

    public boolean userCanGetAllNoMatchPublications(String requesterId) {
        if (!isUserSystemAdmin(requesterId)
            || !isAdmin()) {
            log.error(writeLog(
                String.format("User with ID %s is forbidden to get all non match publications", requesterId
                )));
            return false;
        }

        return true;
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

    private boolean isUserAdmin(String requesterId) {
        if (requesterId != null && !requesterId.isEmpty()) {
            PiUser user = accountManagementService.getUserById(requesterId);
            if (user != null && user.getRoles() != null) {
                return Roles.ALL_ADMINS.contains(user.getRoles());
            }
        }

        return false;
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

    private boolean isPublisher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return hasAuthority(authentication, "APPROLE_api.publisher.admin")
            || hasAuthority(authentication, "APPROLE_api.request.admin");
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return hasAuthority(authentication, "APPROLE_api.request.admin");
    }

    private boolean hasAuthority(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
            .anyMatch(granted -> granted.getAuthority().equals(role));
    }

    public boolean isAuthorisedWithoutAdmin(Artefact artefact, UUID userId) {
        return isAuthorised(artefact, userId, false);
    }

    private boolean isAuthorised(Artefact artefact, UUID userId, boolean systemOrAdmin) {
        if (systemOrAdmin || artefact.getSensitivity().equals(Sensitivity.PUBLIC)) {
            return true;
        } else if (userId == null) {
            return false;
        }
        return accountManagementService.getIsAuthorised(userId, artefact.getListType(),
                                                                               artefact.getSensitivity());
    }

}