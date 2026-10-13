package xyz.subho.retail.banking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.security.Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import xyz.subho.retail.banking.service.AccountService;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;
import xyz.subho.retail.banking.model.User;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @InjectMocks
    private AccountController controller;

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    private Principal principal;

    @BeforeEach
    void setUp() {
        principal = () -> "alice";
    }

    @Test
    void depositParsesMoneyAndRedirects() {
        String result = controller.depositPOST("12.50", "Current", principal, new ExtendedModelMap());

        assertEquals("redirect:/userFront", result);
        verify(accountService).deposit("Current", new BigDecimal("12.50"), principal);
    }

    @Test
    void invalidDepositReturnsFormAndDoesNotMutate() {
        String result = controller.depositPOST("not-money", "Current", principal, new ExtendedModelMap());

        assertEquals("deposit", result);
        verify(accountService, never()).deposit(anyString(), any(), any());
    }

    @Test
    void missingDepositAmountReturnsFormAndDoesNotMutate() {
        String result = controller.depositPOST(null, "Current", principal, new ExtendedModelMap());

        assertEquals("deposit", result);
        verify(accountService, never()).deposit(anyString(), any(), any());
    }

    @Test
    void oversizedDepositAmountReturnsFormBeforeParsing() {
        String result = controller.depositPOST("9".repeat(100), "Current", principal, new ExtendedModelMap());

        assertEquals("deposit", result);
        verify(accountService, never()).deposit(anyString(), any(), any());
    }

    @Test
    void rejectedWithdrawalReturnsFormWithError() {
        doThrow(new IllegalArgumentException("Insufficient funds"))
                .when(accountService).withdraw("Savings", new BigDecimal("10.00"), principal);
        ExtendedModelMap model = new ExtendedModelMap();

        String result = controller.withdrawPOST("10.00", "Savings", principal, model);

        assertEquals("withdraw", result);
        assertEquals("Insufficient funds", model.getAttribute("error"));
    }

    @Test
    void concurrentDepositReturnsRetryableFormError() {
        doThrow(new OptimisticLockingFailureException("conflict"))
                .when(accountService).deposit("Current", new BigDecimal("10.00"), principal);
        ExtendedModelMap model = new ExtendedModelMap();

        String result = controller.depositPOST("10.00", "Current", principal, model);

        assertEquals("deposit", result);
        assertEquals("The account changed while processing. Please try again.", model.getAttribute("error"));
    }

    @Test
    void transactionPageIsCapped() {
        User user = new User();
        user.setCurrentAccount(new xyz.subho.retail.banking.model.CurrentAccount());
        when(userService.findByUsername("alice")).thenReturn(user);
        when(transactionService.findCurrentTransactionPage("alice", PageRequest.of(1_000, 50)))
                .thenReturn(Page.empty());

        controller.currentAccount(new ExtendedModelMap(), principal, Integer.MAX_VALUE);

        verify(transactionService).findCurrentTransactionPage("alice", PageRequest.of(1_000, 50));
    }
}
