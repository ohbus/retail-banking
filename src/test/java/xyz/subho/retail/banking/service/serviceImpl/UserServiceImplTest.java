package xyz.subho.retail.banking.service.serviceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import xyz.subho.retail.banking.dao.RoleDao;
import xyz.subho.retail.banking.dao.UserDao;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.security.Role;
import xyz.subho.retail.banking.security.UserRole;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserDao userDao;

    @Mock
    private RoleDao roleDao;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save() {
        User user = new User();
        userService.save(user);
        verify(userDao, times(1)).save(user);
    }

    @Test
    void findByUsername() {
        User user = new User();
        when(userDao.findByUsername("testuser")).thenReturn(user);
        User foundUser = userService.findByUsername("testuser");
        assertNotNull(foundUser);
        verify(userDao, times(1)).findByUsername("testuser");
    }

    @Test
    void findByEmail() {
        User user = new User();
        when(userDao.findByEmail("test@example.com")).thenReturn(user);
        User foundUser = userService.findByEmail("test@example.com");
        assertNotNull(foundUser);
        verify(userDao, times(1)).findByEmail("test@example.com");
    }

    @Test
    void createUserNewUser() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password");
        Role role = new Role();
        role.setRoleId(1);
        UserRole userRole = new UserRole(user, role);
        Set<UserRole> userRoles = new HashSet<>(Collections.singletonList(userRole));

        when(userDao.findByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userDao.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createUser(user, userRoles);

        assertNotNull(createdUser);
        assertEquals("encodedPassword", createdUser.getPassword());
        assertTrue(createdUser.getUserRoles().contains(userRole));
        assertNotNull(createdUser.getCurrentAccount());
        assertNotNull(createdUser.getSavingsAccount());
        verify(userDao, times(1)).save(user);
        verify(roleDao, times(1)).save(role);
    }

    @Test
    void createUserExistingUser() {
        User user = new User();
        user.setUsername("existinguser");
        when(userDao.findByUsername("existinguser")).thenReturn(user);

        User createdUser = userService.createUser(user, new HashSet<>());

        assertNotNull(createdUser);
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void checkUserExists() {
        when(userDao.findByUsername("testuser")).thenReturn(new User());
        assertTrue(userService.checkUserExists("testuser", "test@example.com"));
    }

    @Test
    void checkUsernameExists() {
        when(userDao.findByUsername("testuser")).thenReturn(new User());
        assertTrue(userService.checkUsernameExists("testuser"));
    }

    @Test
    void checkEmailExists() {
        when(userDao.findByEmail("test@example.com")).thenReturn(new User());
        assertTrue(userService.checkEmailExists("test@example.com"));
    }

    @Test
    void saveUser() {
        User user = new User();
        when(userDao.save(user)).thenReturn(user);
        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser);
        verify(userDao, times(1)).save(user);
    }

    @Test
    void findUserList() {
        List<User> users = Collections.singletonList(new User());
        when(userDao.findAll()).thenReturn(users);
        List<User> foundUsers = userService.findUserList();
        assertNotNull(foundUsers);
        assertEquals(1, foundUsers.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    void enableUser() {
        User user = new User();
        user.setEnabled(false);
        when(userDao.findByUsername("testuser")).thenReturn(user);
        userService.enableUser("testuser");
        assertTrue(user.isEnabled());
        verify(userDao, times(1)).save(user);
    }

    @Test
    void disableUser() {
        User user = new User();
        user.setEnabled(true);
        when(userDao.findByUsername("testuser")).thenReturn(user);
        userService.disableUser("testuser");
        assertFalse(user.isEnabled());
        verify(userDao, times(1)).save(user);
    }
}
