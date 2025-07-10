package xyz.subho.retail.banking.service.serviceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xyz.subho.retail.banking.dao.CurrentAccountDao;
import xyz.subho.retail.banking.dao.SavingsAccountDao;
import xyz.subho.retail.banking.model.*;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled
class AccountServiceImplTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private CurrentAccountDao currentAccountDao;

    @Mock
    private SavingsAccountDao savingsAccountDao;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCurrentAccount() {
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountNumber(12345);
        currentAccount.setAccountBalance(new BigDecimal("0.00")); // Initialize balance
        when(currentAccountDao.save(any(CurrentAccount.class))).thenReturn(currentAccount);
        when(currentAccountDao.findByAccountNumber(any(Integer.class))).thenReturn(currentAccount);

        CurrentAccount createdAccount = accountService.createCurrentAccount();

        assertNotNull(createdAccount);
        assertEquals(new BigDecimal("0.00"), createdAccount.getAccountBalance());
        assertNotNull(createdAccount.getAccountNumber());
        verify(currentAccountDao, times(1)).save(any(CurrentAccount.class));
        verify(currentAccountDao, times(1)).findByAccountNumber(any(Integer.class));
    }

    @Test
    void createSavingsAccount() {
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountNumber(54321);
        savingsAccount.setAccountBalance(new BigDecimal("0.00")); // Initialize balance
        when(savingsAccountDao.save(any(SavingsAccount.class))).thenReturn(savingsAccount);
        when(savingsAccountDao.findByAccountNumber(any(Integer.class))).thenReturn(savingsAccount);

        SavingsAccount createdAccount = accountService.createSavingsAccount();

        assertNotNull(createdAccount);
        assertEquals(new BigDecimal("0.00"), createdAccount.getAccountBalance());
        assertNotNull(createdAccount.getAccountNumber());
        verify(savingsAccountDao, times(1)).save(any(SavingsAccount.class));
        verify(savingsAccountDao, times(1)).findByAccountNumber(any(Integer.class));
    }

    @Test
    void depositCurrentAccount() {
        User user = new User();
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountBalance(new BigDecimal("100.00"));
        user.setCurrentAccount(currentAccount);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);
        doNothing().when(currentAccountDao).save(any(CurrentAccount.class));
        doNothing().when(transactionService).saveCurrentDepositTransaction(any(CurrentTransaction.class));

        accountService.deposit("Current", 50.0, principal);

        assertEquals(new BigDecimal("150.00"), currentAccount.getAccountBalance());
        verify(currentAccountDao, times(1)).save(currentAccount);
        verify(transactionService, times(1)).saveCurrentDepositTransaction(any(CurrentTransaction.class));
    }

    @Test
    void depositSavingsAccount() {
        User user = new User();
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal("200.00"));
        user.setSavingsAccount(savingsAccount);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);
        doNothing().when(savingsAccountDao).save(any(SavingsAccount.class));
        doNothing().when(transactionService).saveSavingsDepositTransaction(any(SavingsTransaction.class));

        accountService.deposit("Savings", 75.0, principal);

        assertEquals(new BigDecimal("275.00"), savingsAccount.getAccountBalance());
        verify(savingsAccountDao, times(1)).save(savingsAccount);
        verify(transactionService, times(1)).saveSavingsDepositTransaction(any(SavingsTransaction.class));
    }

    @Test
    void withdrawCurrentAccount() {
        User user = new User();
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountBalance(new BigDecimal("100.00"));
        user.setCurrentAccount(currentAccount);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);
        doNothing().when(currentAccountDao).save(any(CurrentAccount.class));
        doNothing().when(transactionService).saveCurrentWithdrawTransaction(any(CurrentTransaction.class));

        accountService.withdraw("Current", 50.0, principal);

        assertEquals(new BigDecimal("50.00"), currentAccount.getAccountBalance());
        verify(currentAccountDao, times(1)).save(currentAccount);
        verify(transactionService, times(1)).saveCurrentWithdrawTransaction(any(CurrentTransaction.class));
    }

    @Test
    void withdrawSavingsAccount() {
        User user = new User();
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal("200.00"));
        user.setSavingsAccount(savingsAccount);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(savingsAccountDao.save(any(SavingsAccount.class))).thenReturn(savingsAccount);
        doNothing().when(transactionService).saveSavingsWithdrawTransaction(any(SavingsTransaction.class));

        accountService.withdraw("Savings", 75.0, principal);

        assertEquals(new BigDecimal("125.00"), savingsAccount.getAccountBalance());
        verify(savingsAccountDao, times(1)).save(savingsAccount);
        verify(transactionService, times(1)).saveSavingsWithdrawTransaction(any(SavingsTransaction.class));
    }
}
