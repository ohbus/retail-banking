package xyz.subho.retail.banking.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import xyz.subho.retail.banking.model.SavingsAccount;

public interface SavingsAccountDao extends CrudRepository<SavingsAccount, Long> {

    SavingsAccount findByAccountNumber(int accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    SavingsAccount findByAccountNumberForUpdate(int accountNumber);
    
}
