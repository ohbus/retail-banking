package xyz.subho.retail.banking.dao;

import org.springframework.data.repository.CrudRepository;

import xyz.subho.retail.banking.model.CurrentAccount;

public interface CurrentAccountDao extends CrudRepository<CurrentAccount, Long> {

	CurrentAccount findByAccountNumber(int accountNumber);
	
}