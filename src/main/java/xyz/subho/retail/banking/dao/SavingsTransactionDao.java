package xyz.subho.retail.banking.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import xyz.subho.retail.banking.model.SavingsTransaction;

public interface SavingsTransactionDao extends CrudRepository<SavingsTransaction, Long> {

    List<SavingsTransaction> findAll();

    Page<SavingsTransaction> findBySavingsAccountIdOrderByDateDescIdDesc(Long accountId, Pageable pageable);
    
}
