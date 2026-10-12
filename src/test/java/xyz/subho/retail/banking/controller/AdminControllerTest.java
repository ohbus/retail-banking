package xyz.subho.retail.banking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.security.Principal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import xyz.subho.retail.banking.dao.RoleDao;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.security.Role;
import xyz.subho.retail.banking.security.SessionInvalidationService;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @InjectMocks
    private AdminController controller;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private RoleDao roleDao;

    @Mock
    private SessionInvalidationService sessionInvalidationService;

    @Test
    void selfDeactivationOnlyAcceptsOwnUsername() {
        Principal principal = () -> "alice";
        User user = new User();
        when(userService.findByUsername("alice")).thenReturn(user);

        assertEquals("redirect:/", controller.profileSelfDeactivator("alice", new ExtendedModelMap(), principal));
        verify(sessionInvalidationService).expireSessions("alice");
        assertEquals("error", controller.profileSelfDeactivator("bob", new ExtendedModelMap(), principal));
    }

    @Test
    void selfDeactivationAlsoTogglesDisabledUser() {
        Principal principal = () -> "alice";
        User user = new User();
        user.setEnabled(false);
        when(userService.findByUsername("alice")).thenReturn(user);

        assertEquals("redirect:/", controller.profileSelfDeactivator("alice", new ExtendedModelMap(), principal));
        assertTrue(user.isEnabled());
    }

    @Test
    void adminCanToggleAnotherUser() {
        User user = new User();
        user.setEnabled(true);
        when(userService.findByUsername("bob")).thenReturn(user);

        assertEquals("redirect:/admin/panel", controller.profileActivator("bob", new ExtendedModelMap()));
        assertFalse(user.isEnabled());
        verify(userService).saveUser(user);
        verify(sessionInvalidationService).expireSessions("bob");
    }

    @Test
    void adminSignupReturnsFormOnFailure() {
        User user = new User();
        user.setUsername("admin2");
        user.setEmail("admin2@example.com");
        when(roleDao.findByName("ROLE_ADMIN")).thenReturn(new Role());
        doThrow(new IllegalStateException("database unavailable")).when(userService)
                .createUser(eq(user), anySet());

        assertEquals("adminSignup", controller.signupPost(user, new ExtendedModelMap()));
    }

    @Test
    void adminSignupRedirectsAfterCreation() {
        User user = new User();
        user.setUsername("admin2");
        user.setEmail("admin2@example.com");
        when(roleDao.findByName("ROLE_ADMIN")).thenReturn(new Role());

        assertEquals("redirect:/", controller.signupPost(user, new ExtendedModelMap()));
    }

    @Test
    void duplicateAdminSignupReturnsForm() {
        User user = new User();
        user.setUsername("admin2");
        user.setEmail("admin2@example.com");
        when(userService.checkUserExists("admin2", "admin2@example.com")).thenReturn(true);
        when(userService.checkUsernameExists("admin2")).thenReturn(true);

        assertEquals("adminSignup", controller.signupPost(user, new ExtendedModelMap()));
    }

    @Test
    void duplicateAdminSignupAlsoReportsExistingEmail() {
        User user = new User();
        user.setUsername("admin2");
        user.setEmail("admin2@example.com");
        when(userService.checkUserExists("admin2", "admin2@example.com")).thenReturn(true);
        when(userService.checkEmailExists("admin2@example.com")).thenReturn(true);

        assertEquals("adminSignup", controller.signupPost(user, new ExtendedModelMap()));
    }

    @Test
    void togglingDisabledUserEnablesIt() {
        User user = new User();
        user.setEnabled(false);
        when(userService.findByUsername("bob")).thenReturn(user);

        assertEquals("redirect:/admin/panel", controller.profileActivator("bob", new ExtendedModelMap()));
        assertTrue(user.isEnabled());
    }

    @Test
    void adminUserPageIsCapped() {
        when(userService.findUserPage(PageRequest.of(1_000, 50))).thenReturn(Page.empty());

        controller.profile(new ExtendedModelMap(), Integer.MAX_VALUE);

        verify(userService).findUserPage(PageRequest.of(1_000, 50));
    }
}
