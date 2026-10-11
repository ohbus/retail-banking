package xyz.subho.retail.banking.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import xyz.subho.retail.banking.model.User;

public interface UserDao extends CrudRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);

    Page<User> findAll(Pageable pageable);
    
}
