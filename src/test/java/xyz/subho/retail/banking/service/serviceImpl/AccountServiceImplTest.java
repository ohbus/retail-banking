package xyz.subho.retail.banking.service.serviceImpl;

import org.junit.jupiter.api.BeforeEach;
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
        when(currentAccountDao.findByAccountNumber(any(Integer.class))).thenReturn(null, currentAccount);

        CurrentAccount createdAccount = accountService.createCurrentAccount();

        assertNotNull(createdAccount);
        assertEquals(new BigDecimal("0.00"), createdAccount.getAccountBalance());
        assertNotNull(createdAccount.getAccountNumber());
        verify(currentAccountDao, times(1)).save(any(CurrentAccount.class));
        verify(currentAccountDao, atLeastOnce()).findByAccountNumber(any(Integer.class));
    }

    @Test
    void createSavingsAccount() {
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountNumber(54321);
        savingsAccount.setAccountBalance(new BigDecimal("0.00")); // Initialize balance
        when(savingsAccountDao.save(any(SavingsAccount.class))).thenReturn(savingsAccount);
        when(savingsAccountDao.findByAccountNumber(any(Integer.class))).thenReturn(null, savingsAccount);

        SavingsAccount createdAccount = accountService.createSavingsAccount();

        assertNotNull(createdAccount);
        assertEquals(new BigDecimal("0.00"), createdAccount.getAccountBalance());
        assertNotNull(createdAccount.getAccountNumber());
        verify(savingsAccountDao, times(1)).save(any(SavingsAccount.class));
        verify(savingsAccountDao, atLeastOnce()).findByAccountNumber(any(Integer.class));
    }

    @Test
    void accountNumberGenerationRetriesOnCollision() {
        CurrentAccount existing = new CurrentAccount();
        CurrentAccount created = new CurrentAccount();
        created.setAccountBalance(BigDecimal.ZERO.setScale(2));
        when(currentAccountDao.findByAccountNumber(any(Integer.class))).thenReturn(existing, null, created);
        when(currentAccountDao.save(any(CurrentAccount.class))).thenReturn(created);

        assertEquals(created, accountService.createCurrentAccount());
    }

    @Test
    void accountNumberGenerationChecksSavingsAccounts() {
        SavingsAccount existing = new SavingsAccount();
        CurrentAccount created = new CurrentAccount();
        created.setAccountBalance(BigDecimal.ZERO.setScale(2));
        when(currentAccountDao.findByAccountNumber(any(Integer.class))).thenReturn(null, null, created);
        when(savingsAccountDao.findByAccountNumber(any(Integer.class))).thenReturn(existing, null);
        when(currentAccountDao.save(any(CurrentAccount.class))).thenReturn(created);

        assertEquals(created, accountService.createCurrentAccount());
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
        when(currentAccountDao.save(any(CurrentAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(transactionService).saveCurrentDepositTransaction(any(CurrentTransaction.class));

        accountService.deposit("Current", new BigDecimal("50.00"), principal);

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
        when(savingsAccountDao.save(any(SavingsAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(transactionService).saveSavingsDepositTransaction(any(SavingsTransaction.class));

        accountService.deposit("Savings", new BigDecimal("75.00"), principal);

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
        when(currentAccountDao.save(any(CurrentAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(transactionService).saveCurrentWithdrawTransaction(any(CurrentTransaction.class));

        accountService.withdraw("Current", new BigDecimal("50.00"), principal);

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

        accountService.withdraw("Savings", new BigDecimal("75.00"), principal);

        assertEquals(new BigDecimal("125.00"), savingsAccount.getAccountBalance());
        verify(savingsAccountDao, times(1)).save(savingsAccount);
        verify(transactionService, times(1)).saveSavingsWithdrawTransaction(any(SavingsTransaction.class));
    }

    @Test
    void rejectsNegativeDeposit() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        assertThrows(IllegalArgumentException.class,
                () -> accountService.deposit("Current", new BigDecimal("-1.00"), principal));
        verifyNoInteractions(userService, currentAccountDao, transactionService);
    }

    @Test
    void rejectsOverdraft() {
        User user = new User();
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountBalance(new BigDecimal("10.00"));
        user.setCurrentAccount(currentAccount);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw("Current", new BigDecimal("10.01"), principal));
        verifyNoInteractions(currentAccountDao, transactionService);
    }

    @Test
    void rejectsInvalidAccountTypeAndScale() {
        Principal principal = mock(Principal.class);
        assertThrows(IllegalArgumentException.class,
                () -> accountService.deposit("Other", new BigDecimal("1.00"), principal));
        assertThrows(IllegalArgumentException.class,
                () -> accountService.deposit("Current", new BigDecimal("1.001"), principal));
        verifyNoInteractions(userService, currentAccountDao, transactionService);
    }

    @Test
    void rejectsNullAmountAndNullBalance() {
        Principal principal = mock(Principal.class);
        assertThrows(IllegalArgumentException.class,
                () -> accountService.deposit("Current", null, principal));

        User user = new User();
        CurrentAccount currentAccount = new CurrentAccount();
        user.setCurrentAccount(currentAccount);
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw("Current", new BigDecimal("1.00"), principal));
        verifyNoInteractions(currentAccountDao, transactionService);
    }
}
