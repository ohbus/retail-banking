package xyz.subho.retail.banking.service.serviceImpl;

import org.junit.jupiter.api.BeforeEach;
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

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void findsCurrentTransactionsAsAPage() {
        User user = new User();
        CurrentAccount currentAccount = new CurrentAccount();
        List<CurrentTransaction> transactions = new ArrayList<>();
        transactions.add(new CurrentTransaction());
        currentAccount.setId(1L);
        user.setCurrentAccount(currentAccount);

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(currentTransactionDao.findByCurrentAccountIdOrderByDateDescIdDesc(1L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(transactions));

        Page<CurrentTransaction> result = transactionService.findCurrentTransactionPage("testuser", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userService, times(1)).findByUsername("testuser");
    }

    @Test
    void findsSavingsTransactionsAsAPage() {
        User user = new User();
        SavingsAccount savingsAccount = new SavingsAccount();
        List<SavingsTransaction> transactions = new ArrayList<>();
        transactions.add(new SavingsTransaction());
        savingsAccount.setId(2L);
        user.setSavingsAccount(savingsAccount);

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(savingsTransactionDao.findBySavingsAccountIdOrderByDateDescIdDesc(2L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(transactions));

        Page<SavingsTransaction> result = transactionService.findSavingsTransactionPage("testuser", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
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
    void betweenAccountsTransferRejectsInvalidDestination() {
        CurrentAccount currentAccount = new CurrentAccount();
        SavingsAccount savingsAccount = new SavingsAccount();

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.betweenAccountsTransfer("Current", "Other", "20.00",
                        currentAccount, savingsAccount));

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.betweenAccountsTransfer("Savings", "Other", "20.00",
                        currentAccount, savingsAccount));
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

        when(recipientDao.findByUserUsername("testuser")).thenReturn(List.of(recipient1, recipient2));

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
        User user = new User();
        user.setUsername("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(recipientDao.save(any(Recipient.class))).thenReturn(recipient);

        Recipient savedRecipient = transactionService.saveRecipient(recipient, "testuser");

        assertNotNull(savedRecipient);
        verify(recipientDao, times(1)).save(recipient);
    }

    @Test
    void findRecipientById() {
        Recipient recipient = new Recipient();
        when(recipientDao.findByIdAndUserUsername(1L, "testuser")).thenReturn(recipient);

        Recipient foundRecipient = transactionService.findRecipientById(1L, "testuser");

        assertNotNull(foundRecipient);
        verify(recipientDao, times(1)).findByIdAndUserUsername(1L, "testuser");
    }

    @Test
    void deleteRecipientById() {
        doNothing().when(recipientDao).deleteByIdAndUserUsername(1L, "testuser");
        transactionService.deleteRecipientById(1L, "testuser");
        verify(recipientDao, times(1)).deleteByIdAndUserUsername(1L, "testuser");
    }

    @Test
    void toSomeoneElseTransferCurrentAccount() {
        Recipient recipient = new Recipient();
        recipient.setName("testRecipient");
        recipient.setAccountNumber("54321");
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountBalance(new BigDecimal("100.00"));
        SavingsAccount savingsAccount = new SavingsAccount();
        CurrentAccount recipientAccount = new CurrentAccount();
        recipientAccount.setAccountNumber(54321);
        recipientAccount.setAccountBalance(new BigDecimal("20.00"));
        when(currentAccountDao.findByAccountNumber(54321)).thenReturn(recipientAccount);

        transactionService.toSomeoneElseTransfer(recipient, "Current", "50.00", currentAccount, savingsAccount);

        assertEquals(new BigDecimal("50.00"), currentAccount.getAccountBalance());
        verify(currentAccountDao, times(2)).save(any(CurrentAccount.class));
        verify(currentTransactionDao, times(1)).save(any(CurrentTransaction.class));
    }

    @Test
    void toSomeoneElseTransferSavingsAccount() {
        Recipient recipient = new Recipient();
        recipient.setName("testRecipient");
        recipient.setAccountNumber("54321");
        CurrentAccount currentAccount = new CurrentAccount();
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal("100.00"));
        SavingsAccount recipientAccount = new SavingsAccount();
        recipientAccount.setAccountNumber(54321);
        recipientAccount.setAccountBalance(new BigDecimal("20.00"));
        when(savingsAccountDao.findByAccountNumber(54321)).thenReturn(recipientAccount);

        transactionService.toSomeoneElseTransfer(recipient, "Savings", "50.00", currentAccount, savingsAccount);

        assertEquals(new BigDecimal("50.00"), savingsAccount.getAccountBalance());
        verify(savingsAccountDao, times(2)).save(any(SavingsAccount.class));
        verify(savingsTransactionDao, times(1)).save(any(SavingsTransaction.class));
    }

    @Test
    void rejectsInvalidTransferAmountBeforeMutation() {
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountBalance(new BigDecimal("100.00"));
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal("50.00"));

        assertThrows(IllegalArgumentException.class, () -> transactionService
                .betweenAccountsTransfer("Current", "Savings", "-1.00", currentAccount, savingsAccount));
        verifyNoInteractions(currentAccountDao, savingsAccountDao, currentTransactionDao, savingsTransactionDao);
    }

    @Test
    void rejectsInsufficientFundsBeforeMutation() {
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountBalance(new BigDecimal("10.00"));
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal("50.00"));

        assertThrows(IllegalArgumentException.class, () -> transactionService
                .betweenAccountsTransfer("Current", "Savings", "20.00", currentAccount, savingsAccount));
        verifyNoInteractions(currentAccountDao, savingsAccountDao, currentTransactionDao, savingsTransactionDao);
    }

    @Test
    void rejectsRecipientOwnedByAnotherUser() {
        Recipient submitted = new Recipient();
        submitted.setId(1L);
        User owner = new User();
        owner.setUsername("otheruser");
        Recipient existing = new Recipient();
        existing.setId(1L);
        existing.setUser(owner);
        when(recipientDao.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.saveRecipient(submitted, "testuser"));
        verify(recipientDao, never()).save(any(Recipient.class));
    }

    @Test
    void rejectsRecipientUpdateWhenIdDoesNotExist() {
        Recipient submitted = new Recipient();
        submitted.setId(99L);
        when(recipientDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.saveRecipient(submitted, "testuser"));
        verify(recipientDao, never()).save(any(Recipient.class));
    }

    @Test
    void rejectsRecipientUpdateWithoutAnOwner() {
        Recipient submitted = new Recipient();
        submitted.setId(99L);
        Recipient existing = new Recipient();
        existing.setId(99L);
        when(recipientDao.findById(99L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.saveRecipient(submitted, "testuser"));
        verify(recipientDao, never()).save(any(Recipient.class));
    }

    @Test
    void updatesAnOwnedRecipientWithoutChangingItsOwner() {
        User owner = new User();
        owner.setUsername("alice");
        Recipient existing = new Recipient();
        existing.setId(1L);
        existing.setUser(owner);
        Recipient submitted = new Recipient();
        submitted.setId(1L);
        submitted.setName("updated");
        when(recipientDao.findById(1L)).thenReturn(Optional.of(existing));
        when(recipientDao.save(existing)).thenReturn(existing);

        assertEquals(existing, transactionService.saveRecipient(submitted, "alice"));
        assertEquals("updated", existing.getName());
        assertEquals(owner, existing.getUser());
    }

    @Test
    void rejectsInvalidRecipientAccountInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(null, "Current", "1.00",
                        new CurrentAccount(), new SavingsAccount()));

        Recipient missingAccount = new Recipient();
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(missingAccount, "Current", "1.00",
                        new CurrentAccount(), new SavingsAccount()));

        Recipient malformedAccount = new Recipient();
        malformedAccount.setAccountNumber("not-an-account");
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(malformedAccount, "Current", "1.00",
                        new CurrentAccount(), new SavingsAccount()));

        Recipient unknownAccount = new Recipient();
        unknownAccount.setAccountNumber("99999");
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(unknownAccount, "Current", "1.00",
                        new CurrentAccount(), new SavingsAccount()));

        Recipient validAccount = new Recipient();
        validAccount.setAccountNumber("54321");
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(validAccount, "Current", "not-an-amount",
                        new CurrentAccount(), new SavingsAccount()));

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(validAccount, "Current", "1.001",
                        new CurrentAccount(), new SavingsAccount()));
    }

    @Test
    void rejectsInvalidTypeAndSelfTransfer() {
        CurrentAccount source = new CurrentAccount();
        source.setAccountNumber(12345);
        source.setAccountBalance(new BigDecimal("10.00"));
        SavingsAccount savings = new SavingsAccount();
        Recipient recipient = new Recipient();
        recipient.setAccountNumber("54321");
        CurrentAccount target = new CurrentAccount();
        target.setAccountNumber(54321);
        when(currentAccountDao.findByAccountNumber(54321)).thenReturn(target);
        when(savingsAccountDao.findByAccountNumber(54321)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(recipient, "Other", "1.00", source, savings));

        recipient.setAccountNumber("12345");
        when(currentAccountDao.findByAccountNumber(12345)).thenReturn(source);
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(recipient, "Current", "1.00", source, savings));
    }

    @Test
    void rejectsNullTransferTypesWithoutThrowingNullPointerException() {
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountBalance(new BigDecimal("10.00"));
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal("10.00"));

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.betweenAccountsTransfer(null, "Savings", "1.00",
                        currentAccount, savingsAccount));
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(new Recipient(), null, "1.00",
                        currentAccount, savingsAccount));
    }

    @Test
    void rejectsNullTransferAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(new Recipient(), "Current", null,
                        new CurrentAccount(), new SavingsAccount()));
    }

    @Test
    void rejectsSelfTransferWhenCurrentAccountsShareId() {
        CurrentAccount source = new CurrentAccount();
        source.setId(7L);
        source.setAccountBalance(new BigDecimal("10.00"));
        Recipient recipient = new Recipient();
        recipient.setAccountNumber("54321");
        CurrentAccount target = new CurrentAccount();
        target.setId(7L);
        when(currentAccountDao.findByAccountNumber(54321)).thenReturn(target);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(recipient, "Current", "1.00",
                        source, new SavingsAccount()));
    }

    @Test
    void rejectsSelfTransferWhenSavingsAccountIsTheSameInstance() {
        SavingsAccount source = new SavingsAccount();
        source.setAccountBalance(new BigDecimal("10.00"));
        Recipient recipient = new Recipient();
        recipient.setAccountNumber("54321");
        when(savingsAccountDao.findByAccountNumber(54321)).thenReturn(source);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(recipient, "Savings", "1.00",
                        new CurrentAccount(), source));
    }

    @Test
    void rejectsSelfTransferWhenCurrentAccountIsTheSameInstance() {
        CurrentAccount source = new CurrentAccount();
        source.setAccountBalance(new BigDecimal("10.00"));
        Recipient recipient = new Recipient();
        recipient.setAccountNumber("54321");
        when(currentAccountDao.findByAccountNumber(54321)).thenReturn(source);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(recipient, "Current", "1.00",
                        source, new SavingsAccount()));
    }

    @Test
    void transfersBetweenDistinctCurrentAccountsWithIds() {
        CurrentAccount source = new CurrentAccount();
        source.setId(1L);
        source.setAccountBalance(new BigDecimal("10.00"));
        CurrentAccount target = new CurrentAccount();
        target.setId(2L);
        target.setAccountNumber(54321);
        target.setAccountBalance(new BigDecimal("5.00"));
        Recipient recipient = new Recipient();
        recipient.setAccountNumber("54321");
        when(currentAccountDao.findByAccountNumber(54321)).thenReturn(target);

        transactionService.toSomeoneElseTransfer(recipient, "Current", "1.00",
                source, new SavingsAccount());

        assertEquals(new BigDecimal("9.00"), source.getAccountBalance());
        assertEquals(new BigDecimal("6.00"), target.getAccountBalance());
    }

    @Test
    void transfersWhenOnlyCurrentRecipientHasAnId() {
        CurrentAccount source = new CurrentAccount();
        source.setAccountBalance(new BigDecimal("10.00"));
        CurrentAccount target = new CurrentAccount();
        target.setId(2L);
        target.setAccountNumber(54321);
        target.setAccountBalance(new BigDecimal("5.00"));
        Recipient recipient = new Recipient();
        recipient.setAccountNumber("54321");
        when(currentAccountDao.findByAccountNumber(54321)).thenReturn(target);

        transactionService.toSomeoneElseTransfer(recipient, "Current", "1.00",
                source, new SavingsAccount());

        assertEquals(new BigDecimal("9.00"), source.getAccountBalance());
        assertEquals(new BigDecimal("6.00"), target.getAccountBalance());
    }

    @Test
    void rejectsSelfTransferWhenSavingsAccountsShareAccountNumber() {
        SavingsAccount source = new SavingsAccount();
        source.setAccountNumber(12345);
        source.setAccountBalance(new BigDecimal("10.00"));
        SavingsAccount target = new SavingsAccount();
        target.setAccountNumber(12345);
        Recipient recipient = new Recipient();
        recipient.setAccountNumber("54321");
        when(savingsAccountDao.findByAccountNumber(54321)).thenReturn(target);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(recipient, "Savings", "1.00",
                        new CurrentAccount(), source));
    }

    @Test
    void transfersBetweenDistinctSavingsAccountsWithIds() {
        SavingsAccount source = new SavingsAccount();
        source.setId(1L);
        source.setAccountBalance(new BigDecimal("10.00"));
        SavingsAccount target = new SavingsAccount();
        target.setId(2L);
        target.setAccountNumber(54321);
        target.setAccountBalance(new BigDecimal("5.00"));
        Recipient recipient = new Recipient();
        recipient.setAccountNumber("54321");
        when(savingsAccountDao.findByAccountNumber(54321)).thenReturn(target);

        transactionService.toSomeoneElseTransfer(recipient, "Savings", "1.00",
                new CurrentAccount(), source);

        assertEquals(new BigDecimal("9.00"), source.getAccountBalance());
        assertEquals(new BigDecimal("6.00"), target.getAccountBalance());
    }

    @Test
    void transfersWhenOnlySavingsRecipientHasAnId() {
        SavingsAccount source = new SavingsAccount();
        source.setAccountBalance(new BigDecimal("10.00"));
        SavingsAccount target = new SavingsAccount();
        target.setId(2L);
        target.setAccountNumber(54321);
        target.setAccountBalance(new BigDecimal("5.00"));
        Recipient recipient = new Recipient();
        recipient.setAccountNumber("54321");
        when(savingsAccountDao.findByAccountNumber(54321)).thenReturn(target);

        transactionService.toSomeoneElseTransfer(recipient, "Savings", "1.00",
                new CurrentAccount(), source);

        assertEquals(new BigDecimal("9.00"), source.getAccountBalance());
        assertEquals(new BigDecimal("6.00"), target.getAccountBalance());
    }

    @Test
    void rejectsTransferWithNullSourceBalance() {
        CurrentAccount source = new CurrentAccount();
        CurrentAccount target = new CurrentAccount();
        target.setAccountNumber(54321);
        Recipient recipient = new Recipient();
        recipient.setAccountNumber("54321");
        when(currentAccountDao.findByAccountNumber(54321)).thenReturn(target);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(recipient, "Current", "1.00",
                        source, new SavingsAccount()));
    }

    @Test
    void rejectsTransferWithInsufficientFunds() {
        CurrentAccount source = new CurrentAccount();
        source.setAccountBalance(new BigDecimal("1.00"));
        CurrentAccount target = new CurrentAccount();
        target.setAccountNumber(54321);
        Recipient recipient = new Recipient();
        recipient.setAccountNumber("54321");
        when(currentAccountDao.findByAccountNumber(54321)).thenReturn(target);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.toSomeoneElseTransfer(recipient, "Current", "2.00",
                        source, new SavingsAccount()));
        verify(currentAccountDao, never()).save(any(CurrentAccount.class));
    }

    @Test
    void transfersCurrentFundsToSavingsRecipient() {
        CurrentAccount source = new CurrentAccount();
        source.setAccountBalance(new BigDecimal("20.00"));
        SavingsAccount target = new SavingsAccount();
        target.setAccountNumber(54321);
        target.setAccountBalance(new BigDecimal("5.00"));
        Recipient recipient = new Recipient();
        recipient.setName("Bob");
        recipient.setAccountNumber("54321");
        when(savingsAccountDao.findByAccountNumber(54321)).thenReturn(target);

        transactionService.toSomeoneElseTransfer(recipient, "Current", "10.00", source, new SavingsAccount());

        assertEquals(new BigDecimal("10.00"), source.getAccountBalance());
        assertEquals(new BigDecimal("15.00"), target.getAccountBalance());
    }

    @Test
    void transfersSavingsFundsToCurrentRecipient() {
        SavingsAccount source = new SavingsAccount();
        source.setAccountBalance(new BigDecimal("20.00"));
        CurrentAccount target = new CurrentAccount();
        target.setAccountNumber(54321);
        target.setAccountBalance(new BigDecimal("5.00"));
        Recipient recipient = new Recipient();
        recipient.setName("Bob");
        recipient.setAccountNumber("54321");
        when(currentAccountDao.findByAccountNumber(54321)).thenReturn(target);

        transactionService.toSomeoneElseTransfer(recipient, "Savings", "10.00", new CurrentAccount(), source);

        assertEquals(new BigDecimal("10.00"), source.getAccountBalance());
        assertEquals(new BigDecimal("15.00"), target.getAccountBalance());
    }
}
