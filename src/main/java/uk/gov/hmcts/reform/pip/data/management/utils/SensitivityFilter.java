package uk.gov.hmcts.reform.pip.data.management.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.UserNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.models.request.PiUser;
import uk.gov.hmcts.reform.pip.data.management.models.request.Roles;
import uk.gov.hmcts.reform.pip.data.management.service.AccountManagementService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class handles the restricting of lists that are returned.
 */
@Component
public class SensitivityFilter {

    private final AccountManagementService accountManagementService;

    @Autowired
    public SensitivityFilter(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    /**
     * Filters an artefact based on the user requesting it.
     * @param artefact The artefact to be filtered.
     * @param userId The user ID requesting the artefact.
     * @return The artefact, present if unfiltered.
     */
    public Optional<Artefact> filterArtefact(Artefact artefact, UUID userId) {
        if (userId == null) {
            return isAuthorizedNoUser(artefact) ? Optional.of(artefact) : Optional.empty();
        }

        Optional<PiUser> piUser = accountManagementService.getUser(userId);
        if (piUser.isEmpty()) {
            throw new UserNotFoundException(userId);
        } else {
            return isAuthorized(artefact, piUser.get()) ? Optional.of(artefact) : Optional.empty();
        }
    }

    /**
     * Filters an artefact list, based on the user requesting it.
     * @param artefactList The artefact list to be filtered.
     * @param userId The user ID requesting the filtering.
     * @return The artefact list, containing any artefacts that have been left unfiltered.
     */
    public List<Artefact> filterArtefactList(List<Artefact> artefactList, UUID userId) {

        if (userId == null) {
            return artefactList.stream().filter(this::isAuthorizedNoUser).collect(Collectors.toList());
        }

        Optional<PiUser> piUser = accountManagementService.getUser(userId);

        if (piUser.isEmpty()) {
            throw new UserNotFoundException(userId);
        } else {
            PiUser foundPiUser = piUser.get();
            return artefactList.stream().filter(artefact -> isAuthorized(artefact, foundPiUser))
                .collect(Collectors.toList());
        }
    }

    private boolean isAuthorizedNoUser(Artefact artefact) {
        if (artefact.getSensitivity().equals(Sensitivity.PUBLIC)) {
            return true;
        }
        return false;
    }

    private boolean isAuthorized(Artefact artefact, PiUser user) {
        switch(artefact.getSensitivity()) {
            case PUBLIC: {
                return true;
            }
            case PRIVATE: {
                if (user.getRoles().equals(Roles.VERIFIED)) {
                    return true;
                }
                break;
            }
            case CLASSIFIED: {
                if (user.getRoles().equals(Roles.VERIFIED) &&
                    user.getUserProvenance().equals(artefact.getListType().getProvenance())) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

}
