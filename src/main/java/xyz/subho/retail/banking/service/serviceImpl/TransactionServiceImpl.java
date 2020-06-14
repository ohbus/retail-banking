package xyz.subho.retail.banking.service.serviceImpl;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.subho.retail.banking.dao.CurrentAccountDao;
import xyz.subho.retail.banking.dao.CurrentTransactionDao;
import xyz.subho.retail.banking.dao.RecipientDao;
import xyz.subho.retail.banking.dao.SavingsAccountDao;
import xyz.subho.retail.banking.dao.SavingsTransactionDao;
import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.CurrentTransaction;
import xyz.subho.retail.banking.model.Recipient;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.SavingsTransaction;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private UserService userService;

    @Autowired
    private CurrentTransactionDao currentTransactionDao;

    @Autowired
    private SavingsTransactionDao savingsTransactionDao;

    @Autowired
    private CurrentAccountDao currentAccountDao;

    @Autowired
    private SavingsAccountDao savingsAccountDao;

    @Autowired
    private RecipientDao recipientDao;


    public List<CurrentTransaction> findCurrentTransactionList(String username) {
    	
        User user = userService.findByUsername(username);
        List<CurrentTransaction> currentTransactionList = user.getCurrentAccount().getCurrentTransactionList();

        return currentTransactionList;
        
    }

    public List<SavingsTransaction> findSavingsTransactionList(String username) {
    	
        User user = userService.findByUsername(username);
        List<SavingsTransaction> savingsTransactionList = user.getSavingsAccount().getSavingsTransactionList();

        return savingsTransactionList;
    }

    public void saveCurrentDepositTransaction(CurrentTransaction currentTransaction) {
    	
        currentTransactionDao.save(currentTransaction);
        
    }

    public void saveSavingsDepositTransaction(SavingsTransaction savingsTransaction) {
    	
        savingsTransactionDao.save(savingsTransaction);
        
    }

    public void saveCurrentWithdrawTransaction(CurrentTransaction currentTransaction) {
    	
        currentTransactionDao.save(currentTransaction);
        
    }

    public void saveSavingsWithdrawTransaction(SavingsTransaction savingsTransaction) {
    	
        savingsTransactionDao.save(savingsTransaction);
        
    }

    public void betweenAccountsTransfer(String transferFrom, String transferTo, String amount, CurrentAccount currentAccount, SavingsAccount savingsAccount) throws Exception {
    	
        if (transferFrom.equalsIgnoreCase("Current") && transferTo.equalsIgnoreCase("Savings")) {
            currentAccount.setAccountBalance(currentAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(new BigDecimal(amount)));
            currentAccountDao.save(currentAccount);
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            CurrentTransaction currentTransaction = new CurrentTransaction(date, "Between account transfer from " + transferFrom + " to " + transferTo, "Account", "Finished", Double.parseDouble(amount), currentAccount.getAccountBalance(), currentAccount);
            currentTransactionDao.save(currentTransaction);
            
        } else if (transferFrom.equalsIgnoreCase("Savings") && transferTo.equalsIgnoreCase("Current")) {
        	
            currentAccount.setAccountBalance(currentAccount.getAccountBalance().add(new BigDecimal(amount)));
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            currentAccountDao.save(currentAccount);
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Between account transfer from " + transferFrom + " to " + transferTo, "Transfer", "Finished", Double.parseDouble(amount), savingsAccount.getAccountBalance(), savingsAccount);
            savingsTransactionDao.save(savingsTransaction);
            
        } else {
        	
            throw new Exception("Invalid Transfer");
            
        }
        
    }

    public List<Recipient> findRecipientList(Principal principal) {
    	
        String username = principal.getName();
        List<Recipient> recipientList = recipientDao.findAll().stream()
                .filter(recipient -> username.equals(recipient.getUser().getUsername()))
                .collect(Collectors.toList());

        return recipientList;
        
    }

    public Recipient saveRecipient(Recipient recipient) {
    	
        return recipientDao.save(recipient);
        
    }

    public Recipient findRecipientByName(String recipientName) {
    	
        return recipientDao.findByName(recipientName);
        
    }

    public void deleteRecipientByName(String recipientName) {
    	
        recipientDao.deleteByName(recipientName);
        
    }

    public void toSomeoneElseTransfer(Recipient recipient, String accountType, String amount, CurrentAccount currentAccount, SavingsAccount savingsAccount) {
    	
        if (accountType.equalsIgnoreCase("Current")) {
            currentAccount.setAccountBalance(currentAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            currentAccountDao.save(currentAccount);

            Date date = new Date();

            CurrentTransaction currentTransaction = new CurrentTransaction(date, "Transfer to recipient " + recipient.getName(), "Transfer", "Finished", Double.parseDouble(amount), currentAccount.getAccountBalance(), currentAccount);
            currentTransactionDao.save(currentTransaction);
            
        } else if (accountType.equalsIgnoreCase("Savings")) {
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Transfer to recipient " + recipient.getName(), "Transfer", "Finished", Double.parseDouble(amount), savingsAccount.getAccountBalance(), savingsAccount);
            savingsTransactionDao.save(savingsTransaction);
            
        }
        
    }
    
}