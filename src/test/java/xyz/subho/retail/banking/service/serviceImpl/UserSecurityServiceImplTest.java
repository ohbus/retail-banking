package xyz.subho.retail.banking.service.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import xyz.subho.retail.banking.dao.UserDao;
import xyz.subho.retail.banking.model.User;

@ExtendWith(MockitoExtension.class)
class UserSecurityServiceImplTest {

    @InjectMocks
    private UserSecurityServiceImpl service;

    @Mock
    private UserDao userDao;

    @Test
    void loadsExistingUser() {
        User user = new User();
        when(userDao.findByUsername("alice")).thenReturn(user);

        assertSame(user, service.loadUserByUsername("alice"));
    }

    @Test
    void rejectsUnknownUser() {
        when(userDao.findByUsername("missing")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing"));
    }
}
