package xyz.subho.retail.banking.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.security.UserRole;

public interface UserService {

    User findByUsername(String username);

    User findByEmail(String email);

    boolean checkUserExists(String username, String email);

    boolean checkUsernameExists(String username);

    boolean checkEmailExists(String email);

    void save(User user);

    User createUser(User user, Set<UserRole> userRoles);

    User saveUser(User user);

    Page<User> findUserPage(Pageable pageable);

    void enableUser(String username);

    void disableUser(String username);

}
