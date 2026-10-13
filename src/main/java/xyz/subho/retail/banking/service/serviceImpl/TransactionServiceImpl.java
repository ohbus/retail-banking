package xyz.subho.retail.banking.service.serviceImpl;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final int MAX_INTEGER_DIGITS = 17;
    private static final int MAX_AMOUNT_LENGTH = 32;

    private final UserService userService;
    private final CurrentTransactionDao currentTransactionDao;
    private final SavingsTransactionDao savingsTransactionDao;
    private final CurrentAccountDao currentAccountDao;
    private final SavingsAccountDao savingsAccountDao;
    private final RecipientDao recipientDao;

    public TransactionServiceImpl(UserService userService, CurrentTransactionDao currentTransactionDao,
            SavingsTransactionDao savingsTransactionDao, CurrentAccountDao currentAccountDao,
            SavingsAccountDao savingsAccountDao, RecipientDao recipientDao) {
        this.userService = userService;
        this.currentTransactionDao = currentTransactionDao;
        this.savingsTransactionDao = savingsTransactionDao;
        this.currentAccountDao = currentAccountDao;
        this.savingsAccountDao = savingsAccountDao;
        this.recipientDao = recipientDao;
    }


    public Page<CurrentTransaction> findCurrentTransactionPage(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        return currentTransactionDao.findByCurrentAccountIdOrderByDateDescIdDesc(user.getCurrentAccount().getId(), pageable);
    }

    public Page<SavingsTransaction> findSavingsTransactionPage(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        return savingsTransactionDao.findBySavingsAccountIdOrderByDateDescIdDesc(user.getSavingsAccount().getId(), pageable);
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

    public void betweenAccountsTransfer(String transferFrom, String transferTo, String amount, CurrentAccount currentAccount, SavingsAccount savingsAccount) {
        BigDecimal value = validateAmount(amount);
        if ("Current".equalsIgnoreCase(transferFrom) && "Savings".equalsIgnoreCase(transferTo)) {
            currentAccount = lockCurrentAccount(currentAccount);
            savingsAccount = lockSavingsAccount(savingsAccount);
            requireFunds(currentAccount.getAccountBalance(), value);
            currentAccount.setAccountBalance(currentAccount.getAccountBalance().subtract(value));
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(value));
            currentAccountDao.save(currentAccount);
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            CurrentTransaction currentTransaction = new CurrentTransaction(date, "Between account transfer from " + transferFrom + " to " + transferTo, "Account", "Finished", value, currentAccount.getAccountBalance(), currentAccount);
            currentTransactionDao.save(currentTransaction);
            
        } else if ("Savings".equalsIgnoreCase(transferFrom) && "Current".equalsIgnoreCase(transferTo)) {
            currentAccount = lockCurrentAccount(currentAccount);
            savingsAccount = lockSavingsAccount(savingsAccount);
            requireFunds(savingsAccount.getAccountBalance(), value);
            currentAccount.setAccountBalance(currentAccount.getAccountBalance().add(value));
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(value));
            currentAccountDao.save(currentAccount);
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Between account transfer from " + transferFrom + " to " + transferTo, "Transfer", "Finished", value, savingsAccount.getAccountBalance(), savingsAccount);
            savingsTransactionDao.save(savingsTransaction);
            
        } else {
        	
            throw new IllegalArgumentException("Invalid Transfer");
            
        }
        
    }

    public Page<Recipient> findRecipientPage(String username, Pageable pageable) {
        return recipientDao.findByUserUsername(username, pageable);
        
    }

    public Recipient saveRecipient(Recipient recipient, String username) {
        validateRecipient(recipient, username);
        if (recipient.getId() != null) {
            Recipient existing = recipientDao.findById(recipient.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));
            if (existing.getUser() == null || !username.equals(existing.getUser().getUsername())) {
                throw new IllegalArgumentException("Recipient does not belong to the current user");
            }
            existing.setName(recipient.getName());
            existing.setEmail(recipient.getEmail());
            existing.setPhone(recipient.getPhone());
            existing.setAccountNumber(recipient.getAccountNumber());
            existing.setDescription(recipient.getDescription());
            return recipientDao.save(existing);
        }
        User owner = userService.findByUsername(username);
        if (owner == null) {
            throw new IllegalArgumentException("Recipient owner not found");
        }
        recipient.setUser(owner);
        return recipientDao.save(recipient);
        
    }

    private void validateRecipient(Recipient recipient, String username) {
        if (recipient == null || username == null || username.isBlank()
                || isBlank(recipient.getName()) || isBlank(recipient.getEmail())
                || isBlank(recipient.getPhone()) || isBlank(recipient.getAccountNumber())
                || exceedsColumnLength(recipient.getName()) || exceedsColumnLength(recipient.getEmail())
                || exceedsColumnLength(recipient.getPhone()) || exceedsColumnLength(recipient.getAccountNumber())
                || exceedsColumnLength(recipient.getDescription())) {
            throw new IllegalArgumentException("Recipient fields are invalid");
        }
        try {
            if (Integer.parseInt(recipient.getAccountNumber()) <= 0) {
                throw new IllegalArgumentException("Recipient account is invalid");
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Recipient account is invalid", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean exceedsColumnLength(String value) {
        return value != null && value.length() > 255;
    }

    public Recipient findRecipientById(Long recipientId, String username) {
        return recipientDao.findByIdAndUserUsername(recipientId, username);
        
    }

    public void deleteRecipientById(Long recipientId, String username) {
        recipientDao.deleteByIdAndUserUsername(recipientId, username);
        
    }

    public void toSomeoneElseTransfer(Recipient recipient, String accountType, String amount, CurrentAccount currentAccount, SavingsAccount savingsAccount) {
        BigDecimal value = validateAmount(amount);
        if (recipient == null || recipient.getAccountNumber() == null) {
            throw new IllegalArgumentException("Recipient account is required");
        }
        int recipientAccountNumber;
        try {
            recipientAccountNumber = Integer.parseInt(recipient.getAccountNumber());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Recipient account is invalid", exception);
        }
        CurrentAccount recipientCurrentAccount = currentAccountDao.findByAccountNumber(recipientAccountNumber);
        SavingsAccount recipientSavingsAccount = savingsAccountDao.findByAccountNumber(recipientAccountNumber);
        if (recipientCurrentAccount == null && recipientSavingsAccount == null) {
            throw new IllegalArgumentException("Recipient account was not found");
        }
        if ("Current".equalsIgnoreCase(accountType)) {
            if (sameAccount(recipientCurrentAccount, currentAccount)) {
                throw new IllegalArgumentException("Source and recipient accounts must differ");
            }
            if (recipientCurrentAccount != null) {
                CurrentAccount[] lockedAccounts = lockCurrentPair(currentAccount, recipientCurrentAccount);
                currentAccount = lockedAccounts[0];
                recipientCurrentAccount = lockedAccounts[1];
            } else {
                currentAccount = lockCurrentAccount(currentAccount);
                recipientSavingsAccount = lockSavingsAccount(recipientSavingsAccount);
            }
            requireFunds(currentAccount.getAccountBalance(), value);
            currentAccount.setAccountBalance(currentAccount.getAccountBalance().subtract(value));
            if (recipientCurrentAccount != null) {
                recipientCurrentAccount.setAccountBalance(recipientCurrentAccount.getAccountBalance().add(value));
            } else {
                recipientSavingsAccount.setAccountBalance(recipientSavingsAccount.getAccountBalance().add(value));
            }
            currentAccountDao.save(currentAccount);
            if (recipientCurrentAccount != null) {
                currentAccountDao.save(recipientCurrentAccount);
            } else {
                savingsAccountDao.save(recipientSavingsAccount);
            }

            Date date = new Date();

            CurrentTransaction currentTransaction = new CurrentTransaction(date, "Transfer to recipient " + recipient.getName(), "Transfer", "Finished", value, currentAccount.getAccountBalance(), currentAccount);
            currentTransactionDao.save(currentTransaction);
        } else if ("Savings".equalsIgnoreCase(accountType)) {
            if (sameAccount(recipientSavingsAccount, savingsAccount)) {
                throw new IllegalArgumentException("Source and recipient accounts must differ");
            }
            if (recipientCurrentAccount != null) {
                recipientCurrentAccount = lockCurrentAccount(recipientCurrentAccount);
                savingsAccount = lockSavingsAccount(savingsAccount);
            } else {
                SavingsAccount[] lockedAccounts = lockSavingsPair(savingsAccount, recipientSavingsAccount);
                savingsAccount = lockedAccounts[0];
                recipientSavingsAccount = lockedAccounts[1];
            }
            requireFunds(savingsAccount.getAccountBalance(), value);
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(value));
            if (recipientCurrentAccount != null) {
                recipientCurrentAccount.setAccountBalance(recipientCurrentAccount.getAccountBalance().add(value));
            } else {
                recipientSavingsAccount.setAccountBalance(recipientSavingsAccount.getAccountBalance().add(value));
            }
            savingsAccountDao.save(savingsAccount);
            if (recipientCurrentAccount != null) {
                currentAccountDao.save(recipientCurrentAccount);
            } else {
                savingsAccountDao.save(recipientSavingsAccount);
            }

            Date date = new Date();

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Transfer to recipient " + recipient.getName(), "Transfer", "Finished", value, savingsAccount.getAccountBalance(), savingsAccount);
            savingsTransactionDao.save(savingsTransaction);
        } else {
            throw new IllegalArgumentException("Invalid account type");
        }
    }

    private BigDecimal validateAmount(String amount) {
        if (amount == null || amount.isBlank() || amount.length() > MAX_AMOUNT_LENGTH) {
            throw new IllegalArgumentException("Amount is invalid");
        }
        try {
            BigDecimal value = new BigDecimal(amount);
            long integerDigits = (long) value.precision() - value.scale();
            if (value.signum() <= 0 || value.scale() > 2
                    || integerDigits > MAX_INTEGER_DIGITS) {
                throw new IllegalArgumentException("Amount must be positive with at most two decimal places");
            }
            return value.setScale(2);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Amount is invalid", exception);
        }
    }

    private void requireFunds(BigDecimal balance, BigDecimal amount) {
        if (balance == null || balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
    }

    private boolean sameAccount(CurrentAccount first, CurrentAccount second) {
        if (first == null || second == null) {
            return false;
        }
        if (first == second) {
            return true;
        }
        if (first.getId() != null && second.getId() != null) {
            return first.getId().equals(second.getId());
        }
        return first.getAccountNumber() == second.getAccountNumber();
    }

    private boolean sameAccount(SavingsAccount first, SavingsAccount second) {
        if (first == null || second == null) {
            return false;
        }
        if (first == second) {
            return true;
        }
        if (first.getId() != null && second.getId() != null) {
            return first.getId().equals(second.getId());
        }
        return first.getAccountNumber() == second.getAccountNumber();
    }

    private CurrentAccount lockCurrentAccount(CurrentAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Current account not found");
        }
        CurrentAccount locked = currentAccountDao.findByAccountNumberForUpdate(account.getAccountNumber());
        if (locked != null) {
            return locked;
        }
        if (account.getId() == null) {
            return account;
        }
        throw new IllegalArgumentException("Current account not found");
    }

    private SavingsAccount lockSavingsAccount(SavingsAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Savings account not found");
        }
        SavingsAccount locked = savingsAccountDao.findByAccountNumberForUpdate(account.getAccountNumber());
        if (locked != null) {
            return locked;
        }
        if (account.getId() == null) {
            return account;
        }
        throw new IllegalArgumentException("Savings account not found");
    }

    private CurrentAccount[] lockCurrentPair(CurrentAccount first, CurrentAccount second) {
        if (first.getAccountNumber() <= second.getAccountNumber()) {
            return new CurrentAccount[] { lockCurrentAccount(first), lockCurrentAccount(second) };
        }
        CurrentAccount lockedSecond = lockCurrentAccount(second);
        CurrentAccount lockedFirst = lockCurrentAccount(first);
        return new CurrentAccount[] { lockedFirst, lockedSecond };
    }

    private SavingsAccount[] lockSavingsPair(SavingsAccount first, SavingsAccount second) {
        if (first.getAccountNumber() <= second.getAccountNumber()) {
            return new SavingsAccount[] { lockSavingsAccount(first), lockSavingsAccount(second) };
        }
        SavingsAccount lockedSecond = lockSavingsAccount(second);
        SavingsAccount lockedFirst = lockSavingsAccount(first);
        return new SavingsAccount[] { lockedFirst, lockedSecond };
    }
    
}
