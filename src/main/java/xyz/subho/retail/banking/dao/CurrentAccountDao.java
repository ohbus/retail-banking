package xyz.subho.retail.banking.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import xyz.subho.retail.banking.model.CurrentAccount;

public interface CurrentAccountDao extends CrudRepository<CurrentAccount, Long> {

    CurrentAccount findByAccountNumber(int accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    CurrentAccount findByAccountNumberForUpdate(int accountNumber);
	
}
