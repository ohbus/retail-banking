package xyz.subho.retail.banking.service;

import java.security.Principal;
import java.util.List;

import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.CurrentTransaction;
import xyz.subho.retail.banking.model.Recipient;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.SavingsTransaction;

public interface TransactionService {

    List<CurrentTransaction> findCurrentTransactionList(String username);

    List<SavingsTransaction> findSavingsTransactionList(String username);

    void saveCurrentDepositTransaction(CurrentTransaction currentTransaction);

    void saveSavingsDepositTransaction(SavingsTransaction savingsTransaction);

    void saveCurrentWithdrawTransaction(CurrentTransaction currentTransaction);

    void saveSavingsWithdrawTransaction(SavingsTransaction savingsTransaction);

    void betweenAccountsTransfer(String transferFrom, String transferTo, String amount, CurrentAccount currentAccount, SavingsAccount savingsAccount) throws Exception;

    List<Recipient> findRecipientList(Principal principal);

    Recipient saveRecipient(Recipient recipient);

    Recipient findRecipientByName(String recipientName);

    void deleteRecipientByName(String recipientName);

    void toSomeoneElseTransfer(Recipient recipient, String accountType, String amount, CurrentAccount currentAccount, SavingsAccount savingsAccount);

}