package xyz.subho.retail.banking.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import xyz.subho.retail.banking.controller.AccountController;
import xyz.subho.retail.banking.service.AccountService;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;
import xyz.subho.retail.banking.service.serviceImpl.UserSecurityServiceImpl;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, RequestFilter.class})
@TestPropertySource(properties = "security.remember-me-key=test-only-remember-me-key")
class SecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserSecurityServiceImpl userSecurityService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserService userService;

    @MockBean
    private TransactionService transactionService;

    @Test
    void stateChangingRequestRequiresCsrf() throws Exception {
        mockMvc.perform(post("/account/deposit")
                        .with(user("alice").roles("USER"))
                        .param("accountType", "Current")
                        .param("amount", "10.00"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validCsrfAllowsAuthenticatedRequest() throws Exception {
        mockMvc.perform(post("/account/deposit")
                        .with(user("alice").roles("USER"))
                        .with(csrf())
                        .param("accountType", "Current")
                        .param("amount", "10.00"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void ordinaryUserCannotEnterAdminPath() throws Exception {
        mockMvc.perform(get("/admin/panel").with(user("alice").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginPostRequiresCsrf() throws Exception {
        mockMvc.perform(post("/index")
                        .param("username", "alice")
                        .param("password", "password"))
                .andExpect(status().isForbidden());
    }
}
