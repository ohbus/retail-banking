package xyz.subho.retail.banking.service.serviceImpl;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
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

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final CurrentAccountDao currentAccountDao;
    private final SavingsAccountDao savingsAccountDao;
    private final UserService userService;
    private final TransactionService transactionService;

    public AccountServiceImpl(CurrentAccountDao currentAccountDao, SavingsAccountDao savingsAccountDao,
            UserService userService, TransactionService transactionService) {
        this.currentAccountDao = currentAccountDao;
        this.savingsAccountDao = savingsAccountDao;
        this.userService = userService;
        this.transactionService = transactionService;
    }

    public CurrentAccount createCurrentAccount() {
    	
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setAccountBalance(BigDecimal.ZERO.setScale(2));
        currentAccount.setAccountNumber(accountGen());

        currentAccountDao.save(currentAccount);

        return currentAccountDao.findByAccountNumber(currentAccount.getAccountNumber());
        
    }

    public SavingsAccount createSavingsAccount() {
    	
        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(BigDecimal.ZERO.setScale(2));
        savingsAccount.setAccountNumber(accountGen());

        savingsAccountDao.save(savingsAccount);

        return savingsAccountDao.findByAccountNumber(savingsAccount.getAccountNumber());
        
    }

    public void deposit(String accountType, BigDecimal amount, Principal principal) {
        amount = validateAmount(accountType, amount);
    	
        User user = userService.findByUsername(principal.getName());

        if ("Current".equalsIgnoreCase(accountType)) {
            CurrentAccount currentAccount = user.getCurrentAccount();
            currentAccount.setAccountBalance(currentAccount.getAccountBalance().add(amount));
            currentAccountDao.save(currentAccount);

            Date date = new Date();

            CurrentTransaction currentTransaction = new CurrentTransaction(date, "Deposit to Current Account", "Account", "Finished", amount, currentAccount.getAccountBalance(), currentAccount);
            transactionService.saveCurrentDepositTransaction(currentTransaction);

        } else {
        	
            SavingsAccount savingsAccount = user.getSavingsAccount();
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(amount));
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();
            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Deposit to savings Account", "Account", "Finished", amount, savingsAccount.getAccountBalance(), savingsAccount);
            transactionService.saveSavingsDepositTransaction(savingsTransaction);
            
        }
        
    }

    public void withdraw(String accountType, BigDecimal amount, Principal principal) {
        amount = validateAmount(accountType, amount);
    	
        User user = userService.findByUsername(principal.getName());

        if ("Current".equalsIgnoreCase(accountType)) {
            CurrentAccount currentAccount = user.getCurrentAccount();
            requireFunds(currentAccount.getAccountBalance(), amount);
            currentAccount.setAccountBalance(currentAccount.getAccountBalance().subtract(amount));
            currentAccountDao.save(currentAccount);

            Date date = new Date();

            CurrentTransaction currentTransaction = new CurrentTransaction(date, "Withdraw from Current Account", "Account", "Finished", amount, currentAccount.getAccountBalance(), currentAccount);
            transactionService.saveCurrentWithdrawTransaction(currentTransaction);
            
        } else {
            SavingsAccount savingsAccount = user.getSavingsAccount();
            requireFunds(savingsAccount.getAccountBalance(), amount);
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(amount));
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();
            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Withdraw from savings Account", "Account", "Finished", amount, savingsAccount.getAccountBalance(), savingsAccount);
            transactionService.saveSavingsWithdrawTransaction(savingsTransaction);
            
        }
        
    }

    private int accountGen() {
        int accountNumber;
        do {
            accountNumber = ThreadLocalRandom.current().nextInt(2323, 232321474);
        } while (currentAccountDao.findByAccountNumber(accountNumber) != null
                || savingsAccountDao.findByAccountNumber(accountNumber) != null);
        return accountNumber;
    }

    private BigDecimal validateAmount(String accountType, BigDecimal amount) {
        if (!("Current".equalsIgnoreCase(accountType) || "Savings".equalsIgnoreCase(accountType))) {
            throw new IllegalArgumentException("Invalid account type");
        }
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2) {
            throw new IllegalArgumentException("Amount must be positive with at most two decimal places");
        }
        return amount.setScale(2);
    }

    private void requireFunds(BigDecimal balance, BigDecimal amount) {
        if (balance == null || balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
    }

}
