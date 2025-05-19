package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactService;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.account.Roles;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceTest {

    private static final String TEST_USER_ID = "123";
    private static final UUID TEST_UUID = UUID.randomUUID();
    private static final String PUBLISHER_ROLE = "APPROLE_api.publisher.admin";
    private static final String ADMIN_ROLE = "APPROLE_api.request.admin";
    private static final ListType LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;
    private static final String PROVENANCE = "MANUAL_UPLOAD";

    @Mock
    private AccountManagementService accountManagementService;

    @Mock
    private ArtefactService artefactService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthorisationService authorisationService;

    private PiUser createUser(Roles role) {
        PiUser user = new PiUser();
        user.setRoles(role);
        return user;
    }

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testUserCanUploadPublicationWhenAdminAndPublisher() {
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(PUBLISHER_ROLE),
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.SYSTEM_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertTrue(authorisationService.userCanUploadPublication(TEST_USER_ID, PROVENANCE),
                   "API Token with Admin and Publisher permission cannot upload publication");
    }

    @Test
    void testUserCannotUploadPublicationWhenProvenanceIsManualAndRequesterIdNotPresent() {
        assertFalse(authorisationService.userCanUploadPublication(null, PROVENANCE),
                   "Requester with no value can upload publication");
    }

    @Test
    void testUserCanUploadPublicationWhenProvenanceIsNotManualAndRequesterIdNotPresent() {
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(PUBLISHER_ROLE),
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(null, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(securityContext.getAuthentication()).thenReturn(auth);

        assertTrue(authorisationService.userCanUploadPublication(null, "TEST_PROVENANCE"),
                   "API Token with Admin and Publisher permission cannot upload publication");
    }

    @Test
    void testUserCannotUploadPublicationWhenNotAdmin() {
        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.VERIFIED));

        assertFalse(authorisationService.userCanUploadPublication(TEST_USER_ID, PROVENANCE),
                    "API Token with no admin permission can upload publication");
    }

    @Test
    void testUserCannotUploadPublicationWhenNotPublisher() {
        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.SYSTEM_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        assertFalse(authorisationService.userCanUploadPublication(TEST_USER_ID, PROVENANCE),
                    "API Token with no publisher permission can upload publication");
    }

    @Test
    void testUserCanUploadLocationWhenSystemAdmin() {
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.SYSTEM_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertTrue(authorisationService.userCanUploadLocation(TEST_USER_ID),
                   "System Admin User cannot upload location");
    }

    @Test
    void testUserCannotUploadLocationWhenNotSystemAdmin() {
        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.VERIFIED));

        assertFalse(authorisationService.userCanUploadLocation(TEST_USER_ID),
                    "Verified User can upload location");
    }

    @Test
    void testUserCanDeleteLocationWhenSystemAdminAndAdminRole() {
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.SYSTEM_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertTrue(authorisationService.userCanDeleteLocation(TEST_USER_ID),
                   "System Admin User and Api Token with Admin role cannot delete location");
    }

    @Test
    void testUserCannotDeleteLocationWhenNotSystemAdmin() {
        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.VERIFIED));

        assertFalse(authorisationService.userCanDeleteLocation(TEST_USER_ID),
                    "Verified User can delete location");
    }

    @Test
    void testUserCannotDeleteLocationWhenNotAdminRole() {
        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.SYSTEM_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        assertFalse(authorisationService.userCanDeleteLocation(TEST_USER_ID),
                    "User can delete location when token does not have admin permission");
    }

    @Test
    void testUserCanAccessPublicationWhenPublicSensitivity() {
        Artefact artefact = new Artefact();
        artefact.setSensitivity(Sensitivity.PUBLIC);
        when(artefactService.getMetadataByArtefactId(TEST_UUID)).thenReturn(artefact);

        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertTrue(authorisationService.userCanAccessPublication(TEST_UUID, TEST_UUID, false),
                   "User cannot access publication with sensitivity PUBLIC");
    }

    @Test
    void testUserCanAccessPublicationWhenSystemOrAdmin() {
        Artefact artefact = new Artefact();
        artefact.setSensitivity(Sensitivity.PRIVATE);
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(artefactService.getMetadataByArtefactId(TEST_UUID)).thenReturn(artefact);

        assertTrue(authorisationService.userCanAccessPublication(TEST_UUID, TEST_UUID, true),
                   "User cannot access publication with sensitivity PUBLIC");
    }

    @Test
    void testUserCanAccessPublicationWhenAuthorised() {
        Artefact artefact = new Artefact();
        artefact.setSensitivity(Sensitivity.PRIVATE);
        artefact.setListType(LIST_TYPE);
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(artefactService.getMetadataByArtefactId(TEST_UUID)).thenReturn(artefact);
        when(accountManagementService.getIsAuthorised(TEST_UUID, LIST_TYPE, Sensitivity.PRIVATE))
            .thenReturn(true);

        assertTrue(authorisationService.userCanAccessPublication(TEST_UUID, TEST_UUID, false),
                   "Authorised User cannot access private publication");
    }

    @Test
    void testUserCannotAccessPublicationWhenNotAuthorised() {
        Artefact artefact = new Artefact();
        artefact.setSensitivity(Sensitivity.PRIVATE);
        artefact.setListType(LIST_TYPE);
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(artefactService.getMetadataByArtefactId(TEST_UUID)).thenReturn(artefact);
        when(accountManagementService.getIsAuthorised(TEST_UUID, LIST_TYPE, Sensitivity.PRIVATE))
            .thenReturn(false);

        assertFalse(authorisationService.userCanAccessPublication(TEST_UUID, TEST_UUID, false),
                    "Unauthorised User can access private publication");
    }

    @Test
    void testUserCanAccessPublicationDataWhenAuthorisedAndAdmin() {
        Artefact artefact = new Artefact();
        artefact.setSensitivity(Sensitivity.PUBLIC);
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(artefactService.getMetadataByArtefactId(TEST_UUID)).thenReturn(artefact);
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertTrue(authorisationService.userCanAccessPublicationData(TEST_UUID, TEST_UUID, true),
                   "Authorised User cannot access private publication");
    }

    @Test
    void testUserCannotAccessPublicationDataWhenNotAdmin() {
        Artefact artefact = new Artefact();
        artefact.setSensitivity(Sensitivity.PUBLIC);
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(PUBLISHER_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertFalse(authorisationService.userCanAccessPublicationData(TEST_UUID, TEST_UUID, false),
                    "Authorised User cannot access public publication");
    }

    @Test
    void testUserCanSearchInPublicationDataWhenAdmin() {
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(securityContext.getAuthentication()).thenReturn(auth);

        assertTrue(authorisationService.userCanSearchInPublicationData(),
                   "API Token with Admin User cannot search publication");
    }

    @Test
    void testUserCannotSearchInPublicationDataWhenNotAdmin() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        assertFalse(authorisationService.userCanSearchInPublicationData(),
                    "API Token without Admin permission can search publication");
    }

    @Test
    void testUserCanArchivePublicationsWhenAdminAndAdminRole() {
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.SYSTEM_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertTrue(authorisationService.userCanArchivePublications(TEST_USER_ID),
                   "API Token with Admin permission cannot archive publication");
    }

    @Test
    void testIsAuthorisedWithoutAdmin() {
        Artefact artefact = new Artefact();
        artefact.setSensitivity(Sensitivity.PUBLIC);
        assertTrue(authorisationService.isAuthorisedWithoutAdmin(artefact, TEST_UUID),
                   "API Token without Admin permission can archive publication");
    }
}
