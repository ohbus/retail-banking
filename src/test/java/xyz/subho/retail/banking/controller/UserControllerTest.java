package xyz.subho.retail.banking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.ExtendedModelMap;

import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController controller;

    @Mock
    private UserService userService;

    @Test
    void profilePostAlwaysUpdatesAuthenticatedUser() {
        User authenticated = new User();
        authenticated.setUsername("alice");
        User submitted = new User();
        submitted.setUsername("victim");
        submitted.setFirstName("Updated");
        Principal principal = () -> "alice";
        when(userService.findByUsername("alice")).thenReturn(authenticated);

        String result = controller.profilePost(submitted, new ExtendedModelMap(), principal);

        assertEquals("profile", result);
        assertEquals("alice", authenticated.getUsername());
        assertEquals("Updated", authenticated.getFirstName());
        verify(userService).findByUsername("alice");
        verify(userService).saveUser(authenticated);
    }

    @Test
    void duplicateProfileEmailReturnsFormAndRestoresExistingEmail() {
        User authenticated = new User();
        authenticated.setUsername("alice");
        authenticated.setEmail("alice@example.com");
        User submitted = new User();
        submitted.setEmail("taken@example.com");
        Principal principal = () -> "alice";
        ExtendedModelMap model = new ExtendedModelMap();
        when(userService.findByUsername("alice")).thenReturn(authenticated);
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("duplicate email"))
                .when(userService).saveUser(authenticated);

        assertEquals("profile", controller.profilePost(submitted, model, principal));
        assertEquals("alice@example.com", authenticated.getEmail());
        assertEquals(true, model.getAttribute("emailExists"));
    }
}
