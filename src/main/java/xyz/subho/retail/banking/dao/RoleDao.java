package xyz.subho.retail.banking.dao;

import org.springframework.data.repository.CrudRepository;

import xyz.subho.retail.banking.security.Role;

public interface RoleDao extends CrudRepository<Role, Integer> {

    Role findByName(String name);
    
}