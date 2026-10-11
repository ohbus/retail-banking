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
}
