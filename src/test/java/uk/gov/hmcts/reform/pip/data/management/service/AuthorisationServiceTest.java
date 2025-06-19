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
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.account.Roles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceTest {

    private static final String TEST_USER_ID = "123";
    private static final String ADMIN_ROLE = "APPROLE_api.request.admin";

    @Mock
    private AccountManagementService accountManagementService;

    @Mock
    private SecurityContext securityContext;

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
    void testUserCanAddLocationMetadataWhenAdmin() {
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.SYSTEM_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertTrue(authorisationService.userCanAddLocationMetadata(TEST_USER_ID),
                   "API Token with Admin permission cannot add location metadata");
    }

    @Test
    void testUserCannotAddLocationMetadataWhenRequesterIdNotPresent() {
        assertFalse(authorisationService.userCanAddLocationMetadata(null),
                    "Requester with no value can add location metadata");
    }

    @Test
    void testUserCannotAddLocationMetadataWhenRequesterIdNotAdmin() {
        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.VERIFIED));
        assertFalse(authorisationService.userCanAddLocationMetadata(TEST_USER_ID),
                   "API Token with Admin permission cannot add location metadata");
    }

    @Test
    void testUserCannotAddLocationMetadataWhenRequesterDoesNotExists() {
        assertFalse(authorisationService.userCanAddLocationMetadata(TEST_USER_ID),
                    "API Token with no admin permission can add location metadata");
    }

    @Test
    void testUserCanUpdateLocationMetadataWhenAdmin() {
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.SYSTEM_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertTrue(authorisationService.userCanUpdateLocationMetadata(TEST_USER_ID),
                   "API Token with Admin permission cannot update location metadata");
    }

    @Test
    void testUserCannotUpdateLocationMetadataWhenRequesterIdNotPresent() {
        assertFalse(authorisationService.userCanUpdateLocationMetadata(null),
                    "Requester with no value can update location metadata");
    }

    @Test
    void testUserCannotUpdateLocationMetadataWhenRequesterIdNotAdmin() {
        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.VERIFIED));
        assertFalse(authorisationService.userCanUpdateLocationMetadata(TEST_USER_ID),
                    "API Token with Admin permission cannot update location metadata");
    }

    @Test
    void testUserCannotUpdateLocationMetadataWhenRequesterDoesNotExists() {
        assertFalse(authorisationService.userCanUpdateLocationMetadata(TEST_USER_ID),
                    "API Token with no admin permission can update location metadata");
    }

    @Test
    void testUserCanDeleteLocationMetadataWhenAdmin() {
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ADMIN_ROLE)
        );
        Authentication auth = new TestingAuthenticationToken(TEST_USER_ID, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.SYSTEM_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertTrue(authorisationService.userCanDeleteLocationMetadata(TEST_USER_ID),
                   "API Token with Admin permission cannot delete location metadata");
    }

    @Test
    void testUserCannotDeleteLocationMetadataWhenRequesterIdNotPresent() {
        assertFalse(authorisationService.userCanDeleteLocationMetadata(null),
                    "Requester with no value can delete location metadata");
    }

    @Test
    void testUserCannotDeleteLocationMetadataWhenRequesterIdNotAdmin() {
        when(accountManagementService.getUserById(TEST_USER_ID))
            .thenReturn(createUser(Roles.VERIFIED));
        assertFalse(authorisationService.userCanDeleteLocationMetadata(TEST_USER_ID),
                    "API Token with Admin permission cannot delete location metadata");
    }

    @Test
    void testUserCannotDeleteLocationMetadataWhenRequesterDoesNotExists() {
        assertFalse(authorisationService.userCanDeleteLocationMetadata(TEST_USER_ID),
                    "API Token with no admin permission can delete location metadata");
    }
}
