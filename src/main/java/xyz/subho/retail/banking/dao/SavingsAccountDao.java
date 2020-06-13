package xyz.subho.retail.banking.dao;

import org.springframework.data.repository.CrudRepository;

import xyz.subho.retail.banking.model.SavingsAccount;

public interface SavingsAccountDao extends CrudRepository<SavingsAccount, Long> {

    SavingsAccount findByAccountNumber(int accountNumber);
    
}
