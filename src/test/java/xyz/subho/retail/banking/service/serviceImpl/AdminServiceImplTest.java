package xyz.subho.retail.banking.service.serviceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.UserService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminServiceImplTest {

    @InjectMocks
    private AdminServiceImpl adminService;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deactivateAccount() {
        String username = "testuser";
        User user = new User();
        user.setEnabled(true);
        when(userService.findByUsername(username)).thenReturn(user);
        boolean result = adminService.deactivateAccount(username);
        assertTrue(result);
        assertFalse(user.isEnabled());
        verify(userService).saveUser(user);
    }

    @Test
    void deactivateAccountReturnsFalseForMissingOrAlreadyDisabledUser() {
        when(userService.findByUsername("missing")).thenReturn(null);
        assertFalse(adminService.deactivateAccount("missing"));

        User user = new User();
        user.setEnabled(false);
        when(userService.findByUsername("disabled")).thenReturn(user);
        assertFalse(adminService.deactivateAccount("disabled"));
        verify(userService, never()).saveUser(any(User.class));
    }
}
