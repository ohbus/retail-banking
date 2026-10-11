package xyz.subho.retail.banking.service;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.CurrentTransaction;
import xyz.subho.retail.banking.model.Recipient;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.SavingsTransaction;

public interface TransactionService {

    Page<CurrentTransaction> findCurrentTransactionPage(String username, Pageable pageable);

    Page<SavingsTransaction> findSavingsTransactionPage(String username, Pageable pageable);

    void saveCurrentDepositTransaction(CurrentTransaction currentTransaction);

    void saveSavingsDepositTransaction(SavingsTransaction savingsTransaction);

    void saveCurrentWithdrawTransaction(CurrentTransaction currentTransaction);

    void saveSavingsWithdrawTransaction(SavingsTransaction savingsTransaction);

    void betweenAccountsTransfer(String transferFrom, String transferTo, String amount, CurrentAccount currentAccount, SavingsAccount savingsAccount);

    List<Recipient> findRecipientList(Principal principal);

    Recipient saveRecipient(Recipient recipient, String username);

    Recipient findRecipientById(Long recipientId, String username);

    void deleteRecipientById(Long recipientId, String username);

    void toSomeoneElseTransfer(Recipient recipient, String accountType, String amount, CurrentAccount currentAccount, SavingsAccount savingsAccount);

}
