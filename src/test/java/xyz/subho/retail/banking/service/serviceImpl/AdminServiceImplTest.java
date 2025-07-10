package xyz.subho.retail.banking.service.serviceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xyz.subho.retail.banking.service.UserService;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Disabled
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
        // The current implementation of deactivateAccount in AdminServiceImpl is incomplete
        // and always returns false. This test reflects that behavior.
        // Once the deactivateAccount method is fully implemented, this test should be updated.
        String username = "testuser";
        boolean result = adminService.deactivateAccount(username);
        assertFalse(result);
    }
}
