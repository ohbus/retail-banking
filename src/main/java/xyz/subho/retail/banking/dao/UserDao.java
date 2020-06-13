package xyz.subho.retail.banking.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import xyz.subho.retail.banking.model.User;

public interface UserDao extends CrudRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);

    List<User> findAll();
    
}