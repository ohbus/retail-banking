package xyz.subho.retail.banking.service.serviceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import xyz.subho.retail.banking.dao.UserDao;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.security.Role;
import xyz.subho.retail.banking.security.UserRole;
import xyz.subho.retail.banking.service.AccountNumberService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserDao userDao;

    @Mock
    private AccountNumberService accountNumberService;

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
        setRegistrationFields(user);
        Role role = new Role();
        role.setRoleId(1);
        UserRole userRole = new UserRole(user, role);
        Set<UserRole> userRoles = new HashSet<>(Collections.singletonList(userRole));

        when(userDao.findByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password-password")).thenReturn("encodedPassword");
        when(userDao.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createUser(user, userRoles);

        assertNotNull(createdUser);
        assertEquals("encodedPassword", createdUser.getPassword());
        assertTrue(createdUser.getUserRoles().contains(userRole));
        assertNotNull(createdUser.getCurrentAccount());
        assertNotNull(createdUser.getSavingsAccount());
        verify(userDao, times(1)).save(user);
    }

    @Test
    void createUserExistingUser() {
        User user = new User();
        user.setUsername("existinguser");
        setRegistrationFields(user);
        when(userDao.findByUsername("existinguser")).thenReturn(user);

        User createdUser = userService.createUser(user, new HashSet<>());

        assertNotNull(createdUser);
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void createUserRejectsIncompleteRegistration() {
        User user = new User();
        user.setUsername("newuser");
        when(userDao.findByUsername("newuser")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user, new HashSet<>()));
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void createUserRejectsPasswordBeyondBcryptInputLimit() {
        User user = new User();
        user.setUsername("newuser");
        setRegistrationFields(user);
        user.setPassword("😀".repeat(19));
        when(userDao.findByUsername("newuser")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user, new HashSet<>()));
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void usesReservedNumbersForBothAccounts() {
        User user = new User();
        user.setUsername("newuser");
        setRegistrationFields(user);
        Role role = new Role();
        UserRole userRole = new UserRole(user, role);
        when(userDao.findByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password-password")).thenReturn("encodedPassword");
        when(accountNumberService.next()).thenReturn(12345, 54321);
        when(userDao.save(any(User.class))).thenReturn(user);

        userService.createUser(user, Set.of(userRole));

        verify(userDao).save(user);
        verify(accountNumberService, times(2)).next();
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
    void checkUserExistsChecksTheSubmittedEmail() {
        when(userDao.findByUsername("newuser")).thenReturn(null);
        when(userDao.findByEmail("test@example.com")).thenReturn(new User());

        assertTrue(userService.checkUserExists("newuser", "test@example.com"));
        verify(userDao).findByEmail("test@example.com");
    }

    @Test
    void checkEmailDoesNotExist() {
        assertFalse(userService.checkEmailExists("missing@example.com"));
    }

    @Test
    void checkUserExistsReturnsFalseWhenUsernameAndEmailAreNew() {
        assertFalse(userService.checkUserExists("newuser", "new@example.com"));
    }

    @Test
    void checkUserExistsDoesNotQueryForInvalidIdentifiers() {
        assertFalse(userService.checkUserExists("x".repeat(256), "valid@example.com"));
        verifyNoInteractions(userDao);
    }

    @Test
    void lookupMethodsRejectInvalidIdentifiersBeforeQuerying() {
        assertNull(userService.findByUsername("x".repeat(256)));
        assertNull(userService.findByEmail(" "));
        verifyNoInteractions(userDao);
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
    void findsUsersAsAPage() {
        List<User> users = Collections.singletonList(new User());
        when(userDao.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(users));
        Page<User> foundUsers = userService.findUserPage(PageRequest.of(0, 10));
        assertNotNull(foundUsers);
        assertEquals(1, foundUsers.getContent().size());
        verify(userDao, times(1)).findAll(PageRequest.of(0, 10));
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

    private void setRegistrationFields(User user) {
        user.setPassword("password-password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setAadhaarId("1234 5678 9012");
        user.setEmail("test@example.com");
        user.setPhone("1234567890");
    }
}
