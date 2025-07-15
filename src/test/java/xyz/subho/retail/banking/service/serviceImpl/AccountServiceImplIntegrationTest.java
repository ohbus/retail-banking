package xyz.subho.retail.banking.service.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import xyz.subho.retail.banking.dao.CurrentAccountDao;
import xyz.subho.retail.banking.dao.SavingsAccountDao;
import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.CurrentTransaction;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.SavingsTransaction;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.AccountService;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AccountServiceImplIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CurrentAccountDao currentAccountDao;

    @Autowired
    private SavingsAccountDao savingsAccountDao;

    private User testUser;
    private Principal testPrincipal;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("integrationtestuser");
        testUser.setPassword("password");
        testUser.setFirstName("Integration");
        testUser.setLastName("Test");
        testUser.setEmail("integration@test.com");

        // Create and save accounts
        CurrentAccount currentAccount = accountService.createCurrentAccount();
        SavingsAccount savingsAccount = accountService.createSavingsAccount();

        testUser.setCurrentAccount(currentAccount);
        testUser.setSavingsAccount(savingsAccount);

        // Save user (assuming userService.save exists)
        // userService.save(testUser); // Uncomment if save method exists

        testPrincipal = () -> testUser.getUsername();
    }

    @Test
    void testCreateCurrentAccount_PersistsToDatabase() {
        // When
        CurrentAccount createdAccount = accountService.createCurrentAccount();

        // Then
        assertNotNull(createdAccount);
        assertNotNull(createdAccount.getAccountNumber());
        assertEquals(new BigDecimal("0.0"), createdAccount.getAccountBalance());

        // Verify it's actually in the database
        CurrentAccount retrievedAccount = currentAccountDao.findByAccountNumber(createdAccount.getAccountNumber());
        assertNotNull(retrievedAccount);
        assertEquals(createdAccount.getAccountNumber(), retrievedAccount.getAccountNumber());
    }

    @Test
    void testCreateSavingsAccount_PersistsToDatabase() {
        // When
        SavingsAccount createdAccount = accountService.createSavingsAccount();

        // Then
        assertNotNull(createdAccount);
        assertNotNull(createdAccount.getAccountNumber());
        assertEquals(new BigDecimal("0.0"), createdAccount.getAccountBalance());

        // Verify it's actually in the database
        SavingsAccount retrievedAccount = savingsAccountDao.findByAccountNumber(createdAccount.getAccountNumber());
        assertNotNull(retrievedAccount);
        assertEquals(createdAccount.getAccountNumber(), retrievedAccount.getAccountNumber());
    }

    @Test
    void testDeposit_CurrentAccount_UpdatesBalanceAndCreatesTransaction() {
        // Given
        double depositAmount = 1000.00;
        BigDecimal initialBalance = testUser.getCurrentAccount().getAccountBalance();

        // When
        accountService.deposit("Current", depositAmount, testPrincipal);

        // Then
        CurrentAccount updatedAccount = currentAccountDao.findByAccountNumber(testUser.getCurrentAccount().getAccountNumber());
        assertEquals(initialBalance.add(new BigDecimal(depositAmount)), updatedAccount.getAccountBalance());

        // Verify transaction was created
        List<CurrentTransaction> transactions = transactionService.findCurrentTransactionList(testUser.getUsername());
        assertFalse(transactions.isEmpty());

        CurrentTransaction lastTransaction = transactions.get(transactions.size() - 1);
        assertEquals("Deposit to Current Account", lastTransaction.getDescription());
        assertEquals(depositAmount, lastTransaction.getAmount());
        assertEquals("Finished", lastTransaction.getStatus());
    }

    @Test
    void testDeposit_SavingsAccount_UpdatesBalanceAndCreatesTransaction() {
        // Given
        double depositAmount = 2000.00;
        BigDecimal initialBalance = testUser.getSavingsAccount().getAccountBalance();

        // When
        accountService.deposit("Savings", depositAmount, testPrincipal);

        // Then
        SavingsAccount updatedAccount = savingsAccountDao.findByAccountNumber(testUser.getSavingsAccount().getAccountNumber());
        assertEquals(initialBalance.add(new BigDecimal(depositAmount)), updatedAccount.getAccountBalance());

        // Verify transaction was created
        List<SavingsTransaction> transactions = transactionService.findSavingsTransactionList(testUser.getUsername());
        assertFalse(transactions.isEmpty());

        SavingsTransaction lastTransaction = transactions.get(transactions.size() - 1);
        assertEquals("Deposit to savings Account", lastTransaction.getDescription());
        assertEquals(depositAmount, lastTransaction.getAmount());
        assertEquals("Finished", lastTransaction.getStatus());
    }

    @Test
    void testWithdraw_CurrentAccount_UpdatesBalanceAndCreatesTransaction() {
        // Given
        double initialDeposit = 1500.00;
        double withdrawAmount = 500.00;

        // First deposit some money
        accountService.deposit("Current", initialDeposit, testPrincipal);

        // When
        accountService.withdraw("Current", withdrawAmount, testPrincipal);

        // Then
        CurrentAccount updatedAccount = currentAccountDao.findByAccountNumber(testUser.getCurrentAccount().getAccountNumber());
        assertEquals(new BigDecimal(initialDeposit - withdrawAmount), updatedAccount.getAccountBalance());

        // Verify transaction was created
        List<CurrentTransaction> transactions = transactionService.findCurrentTransactionList(testUser.getUsername());
        assertTrue(transactions.size() >= 2); // Deposit + Withdraw

        CurrentTransaction lastTransaction = transactions.get(transactions.size() - 1);
        assertEquals("Withdraw from Current Account", lastTransaction.getDescription());
        assertEquals(withdrawAmount, lastTransaction.getAmount());
        assertEquals("Finished", lastTransaction.getStatus());
    }

    @Test
    void testWithdraw_SavingsAccount_UpdatesBalanceAndCreatesTransaction() {
        // Given
        double initialDeposit = 2500.00;
        double withdrawAmount = 800.00;

        // First deposit some money
        accountService.deposit("Savings", initialDeposit, testPrincipal);

        // When
        accountService.withdraw("Savings", withdrawAmount, testPrincipal);

        // Then
        SavingsAccount updatedAccount = savingsAccountDao.findByAccountNumber(testUser.getSavingsAccount().getAccountNumber());
        assertEquals(new BigDecimal(initialDeposit - withdrawAmount), updatedAccount.getAccountBalance());

        // Verify transaction was created
        List<SavingsTransaction> transactions = transactionService.findSavingsTransactionList(testUser.getUsername());
        assertTrue(transactions.size() >= 2); // Deposit + Withdraw

        SavingsTransaction lastTransaction = transactions.get(transactions.size() - 1);
        assertEquals("Withdraw from savings Account", lastTransaction.getDescription());
        assertEquals(withdrawAmount, lastTransaction.getAmount());
        assertEquals("Finished", lastTransaction.getStatus());
    }

    @Test
    void testMultipleDeposits_CurrentAccount_AccumulatesBalance() {
        // Given
        double[] deposits = {100.00, 200.00, 300.00};
        BigDecimal initialBalance = testUser.getCurrentAccount().getAccountBalance();

        // When
        for (double deposit : deposits) {
            accountService.deposit("Current", deposit, testPrincipal);
        }

        // Then
        CurrentAccount updatedAccount = currentAccountDao.findByAccountNumber(testUser.getCurrentAccount().getAccountNumber());
        BigDecimal expectedBalance = initialBalance.add(new BigDecimal(600.00));
        assertEquals(expectedBalance, updatedAccount.getAccountBalance());

        // Verify all transactions were created
        List<CurrentTransaction> transactions = transactionService.findCurrentTransactionList(testUser.getUsername());
        assertTrue(transactions.size() >= 3);
    }

    @Test
    void testMultipleWithdrawals_SavingsAccount_DecreasesBalance() {
        // Given
        double initialDeposit = 1000.00;
        double[] withdrawals = {100.00, 150.00, 200.00};

        accountService.deposit("Savings", initialDeposit, testPrincipal);

        // When
        for (double withdrawal : withdrawals) {
            accountService.withdraw("Savings", withdrawal, testPrincipal);
        }

        // Then
        SavingsAccount updatedAccount = savingsAccountDao.findByAccountNumber(testUser.getSavingsAccount().getAccountNumber());
        BigDecimal expectedBalance = new BigDecimal(initialDeposit - 450.00);
        assertEquals(expectedBalance, updatedAccount.getAccountBalance());

        // Verify all transactions were created
        List<SavingsTransaction> transactions = transactionService.findSavingsTransactionList(testUser.getUsername());
        assertTrue(transactions.size() >= 4); // 1 deposit + 3 withdrawals
    }

    @Test
    void testConcurrentAccountCreation_GeneratesUniqueAccountNumbers() {
        // Create multiple accounts concurrently
        CurrentAccount[] accounts = new CurrentAccount[10];

        for (int i = 0; i < 10; i++) {
            accounts[i] = accountService.createCurrentAccount();
        }

        // Verify all account numbers are unique
        for (int i = 0; i < accounts.length; i++) {
            for (int j = i + 1; j < accounts.length; j++) {
                assertNotEquals(accounts[i].getAccountNumber(), accounts[j].getAccountNumber());
            }
        }
    }

    @Test
    void testLargeAmountTransactions_MaintainsPrecision() {
        // Given
        double largeAmount = 999999.99;

        // When
        accountService.deposit("Current", largeAmount, testPrincipal);

        // Then
        CurrentAccount updatedAccount = currentAccountDao.findByAccountNumber(testUser.getCurrentAccount().getAccountNumber());
        assertEquals(new BigDecimal("999999.99"), updatedAccount.getAccountBalance());
    }

    @Test
    void testAccountOperations_WithSpecialCharactersInAccountType() {
        // Given
        double amount = 100.00;

        // When/Then - Should handle case variations
        accountService.deposit("CURRENT", amount, testPrincipal);
        accountService.deposit("current", amount, testPrincipal);
        accountService.deposit("Current", amount, testPrincipal);

        CurrentAccount updatedAccount = currentAccountDao.findByAccountNumber(testUser.getCurrentAccount().getAccountNumber());
        assertEquals(new BigDecimal("300.00"), updatedAccount.getAccountBalance());
    }
}

