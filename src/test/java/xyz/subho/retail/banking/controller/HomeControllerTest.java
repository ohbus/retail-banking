package xyz.subho.retail.banking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import xyz.subho.retail.banking.dao.RoleDao;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.security.Role;
import xyz.subho.retail.banking.service.UserService;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @InjectMocks
    private HomeController controller;

    @Mock
    private UserService userService;

    @Mock
    private RoleDao roleDao;

    @Test
    void signupRedirectsOnlyAfterCreation() {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        when(roleDao.findByName("ROLE_USER")).thenReturn(new Role());

        assertEquals("redirect:/", controller.signupPost(user, new ExtendedModelMap()));
        verify(userService).createUser(eq(user), anySet());
    }

    @Test
    void signupReturnsFormWhenPersistenceFails() {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        when(roleDao.findByName("ROLE_USER")).thenReturn(new Role());
        doThrow(new IllegalStateException("database unavailable")).when(userService)
                .createUser(eq(user), anySet());

        assertEquals("signup", controller.signupPost(user, new ExtendedModelMap()));
    }

    @Test
    void duplicateSignupReturnsFormWithoutCreation() {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        when(userService.checkUserExists("alice", "alice@example.com")).thenReturn(true);
        when(userService.checkEmailExists("alice@example.com")).thenReturn(true);

        assertEquals("signup", controller.signupPost(user, new ExtendedModelMap()));
    }

    @Test
    void duplicateSignupAlsoReportsExistingUsername() {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        when(userService.checkUserExists("alice", "alice@example.com")).thenReturn(true);
        when(userService.checkUsernameExists("alice")).thenReturn(true);

        assertEquals("signup", controller.signupPost(user, new ExtendedModelMap()));
    }
}
