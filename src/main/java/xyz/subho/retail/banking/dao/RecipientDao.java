package xyz.subho.retail.banking.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import xyz.subho.retail.banking.model.Recipient;

public interface RecipientDao extends CrudRepository<Recipient, Long> {

    Page<Recipient> findByUserUsernameOrderByIdAsc(String username, Pageable pageable);

    Recipient findByIdAndUserUsername(Long recipientId, String username);

    void deleteByIdAndUserUsername(Long recipientId, String username);
}
