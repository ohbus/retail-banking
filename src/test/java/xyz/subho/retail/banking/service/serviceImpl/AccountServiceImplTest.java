package xyz.subho.retail.banking.service.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import xyz.subho.retail.banking.dao.CurrentAccountDao;
import xyz.subho.retail.banking.dao.SavingsAccountDao;
import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.CurrentTransaction;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.SavingsTransaction;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private CurrentAccountDao currentAccountDao;

    @Mock
    private SavingsAccountDao savingsAccountDao;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private Principal principal;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User testUser;
    private CurrentAccount testCurrentAccount;
    private SavingsAccount testSavingsAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");

        testCurrentAccount = new CurrentAccount();
        testCurrentAccount.setAccountNumber(123456);
        testCurrentAccount.setAccountBalance(new BigDecimal("1000.00"));

        testSavingsAccount = new SavingsAccount();
        testSavingsAccount.setAccountNumber(789012);
        testSavingsAccount.setAccountBalance(new BigDecimal("2000.00"));

        testUser.setCurrentAccount(testCurrentAccount);
        testUser.setSavingsAccount(testSavingsAccount);
    }

    @Test
    void testCreateCurrentAccount_Success() {
        // Given
        CurrentAccount expectedAccount = new CurrentAccount();
        expectedAccount.setAccountNumber(123456);
        expectedAccount.setAccountBalance(new BigDecimal("0.0"));

        when(currentAccountDao.save(any(CurrentAccount.class))).thenReturn(expectedAccount);
        when(currentAccountDao.findByAccountNumber(anyInt())).thenReturn(expectedAccount);

        // When
        CurrentAccount result = accountService.createCurrentAccount();

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("0.0"), result.getAccountBalance());
        assertTrue(result.getAccountNumber() >= 2323 && result.getAccountNumber() < 232321474);
        verify(currentAccountDao).save(any(CurrentAccount.class));
        verify(currentAccountDao).findByAccountNumber(anyInt());
    }

    @Test
    void testCreateSavingsAccount_Success() {
        // Given
        SavingsAccount expectedAccount = new SavingsAccount();
        expectedAccount.setAccountNumber(789012);
        expectedAccount.setAccountBalance(new BigDecimal("0.0"));

        when(savingsAccountDao.save(any(SavingsAccount.class))).thenReturn(expectedAccount);
        when(savingsAccountDao.findByAccountNumber(anyInt())).thenReturn(expectedAccount);

        // When
        SavingsAccount result = accountService.createSavingsAccount();

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("0.0"), result.getAccountBalance());
        assertTrue(result.getAccountNumber() >= 2323 && result.getAccountNumber() < 232321474);
        verify(savingsAccountDao).save(any(SavingsAccount.class));
        verify(savingsAccountDao).findByAccountNumber(anyInt());
    }

    @Test
    void testDeposit_CurrentAccount_Success() {
        // Given
        double depositAmount = 500.00;
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.deposit("Current", depositAmount, principal);

        // Then
        assertEquals(new BigDecimal("1500.00"), testCurrentAccount.getAccountBalance());
        verify(currentAccountDao).save(testCurrentAccount);
        verify(transactionService).saveCurrentDepositTransaction(any(CurrentTransaction.class));
    }

    @Test
    void testDeposit_SavingsAccount_Success() {
        // Given
        double depositAmount = 300.00;
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.deposit("Savings", depositAmount, principal);

        // Then
        assertEquals(new BigDecimal("2300.00"), testSavingsAccount.getAccountBalance());
        verify(savingsAccountDao).save(testSavingsAccount);
        verify(transactionService).saveSavingsDepositTransaction(any(SavingsTransaction.class));
    }

    @Test
    void testDeposit_CurrentAccount_CaseInsensitive() {
        // Given
        double depositAmount = 100.00;
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.deposit("CURRENT", depositAmount, principal);

        // Then
        assertEquals(new BigDecimal("1100.00"), testCurrentAccount.getAccountBalance());
        verify(currentAccountDao).save(testCurrentAccount);
    }

    @Test
    void testDeposit_SavingsAccount_CaseInsensitive() {
        // Given
        double depositAmount = 200.00;
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.deposit("savings", depositAmount, principal);

        // Then
        assertEquals(new BigDecimal("2200.00"), testSavingsAccount.getAccountBalance());
        verify(savingsAccountDao).save(testSavingsAccount);
    }

    @Test
    void testDeposit_InvalidAccountType_NoOperation() {
        // Given
        double depositAmount = 100.00;
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.deposit("InvalidType", depositAmount, principal);

        // Then
        assertEquals(new BigDecimal("1000.00"), testCurrentAccount.getAccountBalance());
        assertEquals(new BigDecimal("2000.00"), testSavingsAccount.getAccountBalance());
        verify(currentAccountDao, never()).save(any());
        verify(savingsAccountDao, never()).save(any());
    }

    @Test
    void testWithdraw_CurrentAccount_Success() {
        // Given
        double withdrawAmount = 200.00;
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.withdraw("Current", withdrawAmount, principal);

        // Then
        assertEquals(new BigDecimal("800.00"), testCurrentAccount.getAccountBalance());
        verify(currentAccountDao).save(testCurrentAccount);
        verify(transactionService).saveCurrentWithdrawTransaction(any(CurrentTransaction.class));
    }

    @Test
    void testWithdraw_SavingsAccount_Success() {
        // Given
        double withdrawAmount = 400.00;
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.withdraw("Savings", withdrawAmount, principal);

        // Then
        assertEquals(new BigDecimal("1600.00"), testSavingsAccount.getAccountBalance());
        verify(savingsAccountDao).save(testSavingsAccount);
        verify(transactionService).saveSavingsWithdrawTransaction(any(SavingsTransaction.class));
    }

    @Test
    void testWithdraw_CurrentAccount_AllowsNegativeBalance() {
        // Given
        double withdrawAmount = 1500.00; // More than available balance
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.withdraw("Current", withdrawAmount, principal);

        // Then
        assertEquals(new BigDecimal("-500.00"), testCurrentAccount.getAccountBalance());
        verify(currentAccountDao).save(testCurrentAccount);
    }

    @Test
    void testWithdraw_SavingsAccount_AllowsNegativeBalance() {
        // Given
        double withdrawAmount = 2500.00; // More than available balance
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.withdraw("Savings", withdrawAmount, principal);

        // Then
        assertEquals(new BigDecimal("-500.00"), testSavingsAccount.getAccountBalance());
        verify(savingsAccountDao).save(testSavingsAccount);
    }

    @Test
    void testWithdraw_InvalidAccountType_NoOperation() {
        // Given
        double withdrawAmount = 100.00;
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.withdraw("InvalidType", withdrawAmount, principal);

        // Then
        assertEquals(new BigDecimal("1000.00"), testCurrentAccount.getAccountBalance());
        assertEquals(new BigDecimal("2000.00"), testSavingsAccount.getAccountBalance());
        verify(currentAccountDao, never()).save(any());
        verify(savingsAccountDao, never()).save(any());
    }

    @Test
    void testDeposit_ZeroAmount() {
        // Given
        double depositAmount = 0.00;
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.deposit("Current", depositAmount, principal);

        // Then
        assertEquals(new BigDecimal("1000.00"), testCurrentAccount.getAccountBalance());
        verify(currentAccountDao).save(testCurrentAccount);
    }

    @Test
    void testWithdraw_ZeroAmount() {
        // Given
        double withdrawAmount = 0.00;
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.withdraw("Savings", withdrawAmount, principal);

        // Then
        assertEquals(new BigDecimal("2000.00"), testSavingsAccount.getAccountBalance());
        verify(savingsAccountDao).save(testSavingsAccount);
    }

    @Test
    void testDeposit_NegativeAmount() {
        // Given
        double depositAmount = -100.00;
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When
        accountService.deposit("Current", depositAmount, principal);

        // Then
        assertEquals(new BigDecimal("900.00"), testCurrentAccount.getAccountBalance());
        verify(currentAccountDao).save(testCurrentAccount);
    }

    @Test
    void testAccountGen_GeneratesValidRange() {
        // Using reflection to test private method
        try {
            java.lang.reflect.Method method = AccountServiceImpl.class.getDeclaredMethod("accountGen");
            method.setAccessible(true);

            for (int i = 0; i < 100; i++) {
                int accountNumber = (int) method.invoke(accountService);
                assertTrue(accountNumber >= 2323 && accountNumber < 232321474);
            }
        } catch (Exception e) {
            fail("Failed to test accountGen method: " + e.getMessage());
        }
    }
}
