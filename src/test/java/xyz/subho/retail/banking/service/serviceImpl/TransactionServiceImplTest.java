package xyz.subho.retail.banking.service.serviceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xyz.subho.retail.banking.dao.*;
import xyz.subho.retail.banking.model.*;
import xyz.subho.retail.banking.service.UserService;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled
class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private UserService userService;

    @Mock
    private CurrentTransactionDao currentTransactionDao;

    @Mock
    private SavingsTransactionDao savingsTransactionDao;

    @Mock
    private CurrentAccountDao currentAccountDao;

    @Mock
    private SavingsAccountDao savingsAccountDao;

    @Mock
    private RecipientDao recipientDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findCurrentTransactionList() {
        User user = new User();
        CurrentAccount currentAccount = new CurrentAccount();
        List<CurrentTransaction> transactions = new ArrayList<>();
        transactions.add(new CurrentTransaction());
        currentAccount.setCurrentTransactionList(transactions);
        user.setCurrentAccount(currentAccount);

        when(userService.findByUsername("testuser")).thenReturn(user);

        List<CurrentTransaction> result = transactionService.findCurrentTransactionList("testuser");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userService, times(1)).findByUsername("testuser");
    }

    @Test
    void findSavingsTransactionList() {
        User user = new User();
        SavingsAccount savingsAccount = new SavingsAccount();
        List<SavingsTransaction> transactions = new ArrayList<>();
        transactions.add(new SavingsTransaction());
        savingsAccount.setSavingsTransactionList(transactions);
        user.setSavingsAccount(savingsAccount);

        when(userService.findByUsername("testuser")).thenReturn(user);

        List<SavingsTransaction> result = transactionService.findSavingsTransactionList("testuser");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userService, times(1)).findByUsername("testuser");
    }

    @Test
    void saveCurrentDepositTransaction() {
        CurrentTransaction transaction = new CurrentTransaction();
        transactionService.saveCurrentDepositTransaction(transaction);
        verify(currentTransactionDao, times(1)).save(transaction);
    }

    @Test
    void saveSavingsDepositTransaction() {
        SavingsTransaction transaction = new SavingsTransaction();
        transactionService.saveSavingsDepositTransaction(transaction);
        verify(savingsTransactionDao, times(1)).save(transaction);
    }

    @Test
    void saveCurrentWithdrawTransaction() {
        CurrentTransaction transaction = new CurrentTransaction();
        transactionService.saveCurrentWithdrawTransaction(transaction);
        verify(currentTransactionDao, times(1)).save(transaction);
    }

    @Test
    void saveSavingsWithdrawTransaction() {
        SavingsTransaction transaction = new SavingsTransaction();
        transactionService.saveSavingsWithdrawTransaction(transaction);
        verify(savingsTransactionDao, times(1)).save(transaction);
    }

    @Test
    void betweenAccountsTransferCurrentToSavings() throws Exception {
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountBalance(new BigDecimal("100.00"));
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal("50.00"));

        transactionService.betweenAccountsTransfer("Current", "Savings", "20.00", currentAccount, savingsAccount);

        assertEquals(new BigDecimal("80.00"), currentAccount.getAccountBalance());
        assertEquals(new BigDecimal("70.00"), savingsAccount.getAccountBalance());
        verify(currentAccountDao, times(1)).save(currentAccount);
        verify(savingsAccountDao, times(1)).save(savingsAccount);
        verify(currentTransactionDao, times(1)).save(any(CurrentTransaction.class));
    }

    @Test
    void betweenAccountsTransferSavingsToCurrent() throws Exception {
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountBalance(new BigDecimal("100.00"));
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal("50.00"));

        transactionService.betweenAccountsTransfer("Savings", "Current", "20.00", currentAccount, savingsAccount);

        assertEquals(new BigDecimal("120.00"), currentAccount.getAccountBalance());
        assertEquals(new BigDecimal("30.00"), savingsAccount.getAccountBalance());
        verify(currentAccountDao, times(1)).save(currentAccount);
        verify(savingsAccountDao, times(1)).save(savingsAccount);
        verify(savingsTransactionDao, times(1)).save(any(SavingsTransaction.class));
    }

    @Test
    void betweenAccountsTransferInvalid() {
        CurrentAccount currentAccount = new CurrentAccount();
        SavingsAccount savingsAccount = new SavingsAccount();

        Exception exception = assertThrows(Exception.class, () -> {
            transactionService.betweenAccountsTransfer("Invalid", "Invalid", "20.00", currentAccount, savingsAccount);
        });

        assertEquals("Invalid Transfer", exception.getMessage());
    }

    @Test
    void findRecipientList() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        User user = new User();
        user.setUsername("testuser");

        Recipient recipient1 = new Recipient();
        recipient1.setUser(user);
        Recipient recipient2 = new Recipient();
        recipient2.setUser(user);
        Recipient recipient3 = new Recipient();
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        recipient3.setUser(otherUser);

        List<Recipient> allRecipients = new ArrayList<>();
        allRecipients.add(recipient1);
        allRecipients.add(recipient2);
        allRecipients.add(recipient3);

        when(recipientDao.findAll()).thenReturn(allRecipients);

        List<Recipient> result = transactionService.findRecipientList(principal);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(recipient1));
        assertTrue(result.contains(recipient2));
        assertFalse(result.contains(recipient3));
    }

    @Test
    void saveRecipient() {
        Recipient recipient = new Recipient();
        when(recipientDao.save(any(Recipient.class))).thenReturn(recipient);

        Recipient savedRecipient = transactionService.saveRecipient(recipient);

        assertNotNull(savedRecipient);
        verify(recipientDao, times(1)).save(recipient);
    }

    @Test
    void findRecipientByName() {
        Recipient recipient = new Recipient();
        when(recipientDao.findByName("testRecipient")).thenReturn(recipient);

        Recipient foundRecipient = transactionService.findRecipientByName("testRecipient");

        assertNotNull(foundRecipient);
        verify(recipientDao, times(1)).findByName("testRecipient");
    }

    @Test
    void deleteRecipientByName() {
        doNothing().when(recipientDao).deleteByName("testRecipient");
        transactionService.deleteRecipientByName("testRecipient");
        verify(recipientDao, times(1)).deleteByName("testRecipient");
    }

    @Test
    void toSomeoneElseTransferCurrentAccount() {
        Recipient recipient = new Recipient();
        recipient.setName("testRecipient");
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountBalance(new BigDecimal("100.00"));
        SavingsAccount savingsAccount = new SavingsAccount();

        transactionService.toSomeoneElseTransfer(recipient, "Current", "50.00", currentAccount, savingsAccount);

        assertEquals(new BigDecimal("50.00"), currentAccount.getAccountBalance());
        verify(currentAccountDao, times(1)).save(currentAccount);
        verify(currentTransactionDao, times(1)).save(any(CurrentTransaction.class));
    }

    @Test
    void toSomeoneElseTransferSavingsAccount() {
        Recipient recipient = new Recipient();
        recipient.setName("testRecipient");
        CurrentAccount currentAccount = new CurrentAccount();
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal("100.00"));

        transactionService.toSomeoneElseTransfer(recipient, "Savings", "50.00", currentAccount, savingsAccount);

        assertEquals(new BigDecimal("50.00"), savingsAccount.getAccountBalance());
        verify(savingsAccountDao, times(1)).save(savingsAccount);
        verify(savingsTransactionDao, times(1)).save(any(SavingsTransaction.class));
    }
}
