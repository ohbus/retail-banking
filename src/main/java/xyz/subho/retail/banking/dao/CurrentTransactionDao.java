package xyz.subho.retail.banking.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import xyz.subho.retail.banking.model.CurrentTransaction;

public interface CurrentTransactionDao extends CrudRepository<CurrentTransaction, Long> {

    List<CurrentTransaction> findAll();

    Page<CurrentTransaction> findByCurrentAccountIdOrderByDateDescIdDesc(Long accountId, Pageable pageable);
    
}
