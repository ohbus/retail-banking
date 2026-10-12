package xyz.subho.retail.banking.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.mockito.ArgumentCaptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import xyz.subho.retail.banking.config.RequestFilter;
import xyz.subho.retail.banking.config.SecurityConfig;
import xyz.subho.retail.banking.dao.RoleDao;
import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.Recipient;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.AccountService;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;
import xyz.subho.retail.banking.security.SessionInvalidationService;
import xyz.subho.retail.banking.security.Role;
import xyz.subho.retail.banking.security.UserRole;
import xyz.subho.retail.banking.service.serviceImpl.UserSecurityServiceImpl;

@WebMvcTest(controllers = {
        AccountController.class,
        AdminController.class,
        HomeController.class,
        TransferController.class,
        UserController.class
})
@Import({SecurityConfig.class, RequestFilter.class})
@TestPropertySource(properties = "security.remember-me-key=test-only-remember-me-key")
class UiTemplateWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserSecurityServiceImpl userSecurityService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private UserService userService;

    @MockBean
    private RoleDao roleDao;

    @MockBean
    private SessionInvalidationService sessionInvalidationService;

    private User user;

    @BeforeEach
    void setUp() {
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountNumber(10001);
        currentAccount.setAccountBalance(new BigDecimal("100.00"));

        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountNumber(20002);
        savingsAccount.setAccountBalance(new BigDecimal("200.00"));

        user = new User();
        user.setUsername("alice");
        user.setFirstName("Alice");
        user.setLastName("Tester");
        user.setEmail("alice@example.com");
        user.setAadhaarId("1234 5678 9012");
        user.setCurrentAccount(currentAccount);
        user.setSavingsAccount(savingsAccount);

        when(userService.findByUsername("alice")).thenReturn(user);
        when(transactionService.findCurrentTransactionPage(eq("alice"), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(Page.empty());
        when(transactionService.findSavingsTransactionPage(eq("alice"), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(Page.empty());
        when(userService.findUserPage(org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(Page.empty());
        when(transactionService.findRecipientPage(eq("alice"), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(recipient())));
    }

    @Test
    void rendersPublicPagesAndSignupForm() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("Please sign in")))
                .andExpect(content().string(containsString("/signup")))
                .andExpect(content().string(containsString("_csrf")));

        mockMvc.perform(get("/index?error&logout&errorSignUp&successSignUp"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Invalid username or password.")))
                .andExpect(content().string(containsString("You have been logged out.")))
                .andExpect(content().string(containsString("Your account has been activated!")));

        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(content().string(containsString("/signup")))
                .andExpect(content().string(containsString("_csrf")));
    }

    @Test
    void signupCannotBindSensitiveUserFields() throws Exception {
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        when(userService.checkUserExists("alice", "alice@example.com")).thenReturn(false);
        when(roleDao.findByName("ROLE_USER")).thenReturn(userRole);

        mockMvc.perform(post("/signup").with(csrf())
                        .param("username", "alice")
                        .param("email", "alice@example.com")
                        .param("password", "password")
                        .param("userId", "99")
                        .param("enabled", "false")
                        .param("currentAccount.accountNumber", "99999")
                        .param("userRoles[0].role.name", "ROLE_ADMIN"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<UserRole>> rolesCaptor = ArgumentCaptor.forClass(Set.class);
        verify(userService).createUser(userCaptor.capture(), rolesCaptor.capture());
        User submitted = userCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertNull(submitted.getUserId());
        org.junit.jupiter.api.Assertions.assertTrue(submitted.isEnabled());
        org.junit.jupiter.api.Assertions.assertNull(submitted.getCurrentAccount());
        org.junit.jupiter.api.Assertions.assertEquals(1, rolesCaptor.getValue().size());
        org.junit.jupiter.api.Assertions.assertEquals("ROLE_USER",
                rolesCaptor.getValue().iterator().next().getRole().getName());
    }

    @Test
    void rendersDashboardAndAccountPages() throws Exception {
        mockMvc.perform(get("/userFront").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("userFront"))
                .andExpect(content().string(containsString("100.00")))
                .andExpect(content().string(containsString("200.00")));

        mockMvc.perform(get("/account/currentAccount").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("currentAccount"))
                .andExpect(content().string(containsString("Current Balance")));

        mockMvc.perform(get("/account/savingsAccount").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("savingsAccount"))
                .andExpect(content().string(containsString("Savings Balance")));
    }

    @Test
    void rendersMoneyAndTransferFormsWithCsrf() throws Exception {
        mockMvc.perform(get("/account/deposit").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("deposit"))
                .andExpect(content().string(containsString("/account/deposit")))
                .andExpect(content().string(containsString("_csrf")));

        mockMvc.perform(get("/account/withdraw").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("withdraw"))
                .andExpect(content().string(containsString("/account/withdraw")))
                .andExpect(content().string(containsString("_csrf")));

        mockMvc.perform(get("/transfer/betweenAccounts").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("betweenAccounts"))
                .andExpect(content().string(containsString("/transfer/betweenAccounts")))
                .andExpect(content().string(containsString("_csrf")))
                .andExpect(content().string(containsString("value=\"Current\">Current")));

        mockMvc.perform(get("/transfer/toSomeoneElse").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("toSomeoneElse"))
                .andExpect(content().string(containsString("/transfer/toSomeoneElse")))
                .andExpect(content().string(containsString("_csrf")));
    }

    @Test
    void preservesSelectedAccountsWhenTransferValidationFails() throws Exception {
        doThrow(new IllegalArgumentException("Amount is invalid")).when(transactionService)
                .betweenAccountsTransfer(eq("Current"), eq("Savings"), eq("-1.00"), any(), any());

        mockMvc.perform(post("/transfer/betweenAccounts")
                        .with(user("alice").roles("USER"))
                        .with(csrf())
                        .param("transferFrom", "Current")
                        .param("transferTo", "Savings")
                        .param("amount", "-1.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("betweenAccounts"))
                .andExpect(content().string(containsString("value=\"Current\" selected=\"selected\"")))
                .andExpect(content().string(containsString("value=\"Savings\" selected=\"selected\"")));
    }

    @Test
    void rendersRecipientAndProfilePagesWithCsrf() throws Exception {
        mockMvc.perform(get("/transfer/recipient").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("recipient"))
                .andExpect(content().string(containsString("recipient@example.com")))
                .andExpect(content().string(containsString("/transfer/recipient/save")))
                .andExpect(content().string(containsString("/transfer/recipient/delete")))
                .andExpect(content().string(containsString("_csrf")));

        mockMvc.perform(get("/user/profile").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(content().string(containsString("My Profile")))
                .andExpect(content().string(containsString("/user/profile")))
                .andExpect(content().string(containsString("_csrf")))
                .andExpect(content().string(containsString("/admin/toggleUserSelf")));
    }

    @Test
    void rendersAdminPagesAndGeneratedPostFormCsrf() throws Exception {
        User disabledUser = new User();
        disabledUser.setUsername("bob");
        disabledUser.setFirstName("Bob");
        disabledUser.setEnabled(false);
        when(userService.findUserPage(org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(user, disabledUser)));

        mockMvc.perform(get("/admin/panel").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("adminPanel"))
                .andExpect(content().string(containsString("Account Management Portal")))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("bob")))
                .andExpect(content().string(containsString("****9012")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("1234 5678 9012"))))
                .andExpect(content().string(containsString("Enabled")))
                .andExpect(content().string(containsString("Disabled")))
                .andExpect(content().string(containsString("addCsrfField")))
                .andExpect(content().string(containsString("/admin/toggleUser")))
                .andExpect(content().string(containsString("/admin/currentAccount")))
                .andExpect(content().string(containsString("/admin/savingsAccount")))
                .andExpect(content().string(containsString(
                        "onclick=\"showCurrentAcc(this.getAttribute(&#39;datausername&#39;))\">Current")))
                .andExpect(content().string(containsString(
                        "onclick=\"showSavingsAcc(this.getAttribute(&#39;datausername&#39;))\">Savings")));

        mockMvc.perform(get("/admin/signup").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("adminSignup"))
                .andExpect(content().string(containsString("New Administrator Registration Page")))
                .andExpect(content().string(containsString("/admin/signup")))
                .andExpect(content().string(containsString("_csrf")));
    }

    @Test
    void rendersAccessDeniedTemplateForSelfToggleMismatch() throws Exception {
        mockMvc.perform(post("/admin/toggleUserSelf")
                .with(user("alice").roles("USER"))
                .with(csrf())
                .param("username", "bob"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("The Page can't be found")));
    }

    private Recipient recipient() {
        Recipient recipient = new Recipient();
        recipient.setName("Alice's recipient");
        recipient.setEmail("recipient@example.com");
        recipient.setAccountNumber("30003");
        return recipient;
    }
}
