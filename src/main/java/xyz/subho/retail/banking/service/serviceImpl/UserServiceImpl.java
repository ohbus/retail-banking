package xyz.subho.retail.banking.service.serviceImpl;

import java.math.BigDecimal;
import java.util.Set;
import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.SavingsAccount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import xyz.subho.retail.banking.dao.RoleDao;
import xyz.subho.retail.banking.dao.UserDao;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.security.UserRole;
import xyz.subho.retail.banking.service.UserService;
import xyz.subho.retail.banking.service.AccountNumberService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final AccountNumberService accountNumberService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDao userDao, RoleDao roleDao, AccountNumberService accountNumberService,
            BCryptPasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.accountNumberService = accountNumberService;
        this.passwordEncoder = passwordEncoder;
    }

    

    public void save(User user) {
    	
        userDao.save(user);
        
    }

    public User findByUsername(String username) {
    	
        return userDao.findByUsername(username);
        
    }

    public User findByEmail(String email) {
    	
        return userDao.findByEmail(email);
    }


    public User createUser(User user, Set<UserRole> userRoles) {
    	
        User localUser = userDao.findByUsername(user.getUsername());

        if (localUser != null) {
        	
            logger.info("User with username {} already exist. Nothing will be done. ", user.getUsername());
            
        } else {
        	
            String encryptedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encryptedPassword);

            for (UserRole ur : userRoles) {
            	
                roleDao.save(ur.getRole());
                
            }

            user.getUserRoles().addAll(userRoles);

            CurrentAccount currentAccount = new CurrentAccount();
            currentAccount.setAccountBalance(BigDecimal.ZERO.setScale(2));
            currentAccount.setAccountNumber(accountNumberService.next());
            user.setCurrentAccount(currentAccount);

            SavingsAccount savingsAccount = new SavingsAccount();
            savingsAccount.setAccountBalance(BigDecimal.ZERO.setScale(2));
            savingsAccount.setAccountNumber(accountNumberService.next());
            user.setSavingsAccount(savingsAccount);

            localUser = userDao.save(user);
        }

        return localUser;
        
    }

    public boolean checkUserExists(String username, String email) {
    	
        return checkUsernameExists(username) || checkEmailExists(email);
        
    }

    public boolean checkUsernameExists(String username) {
    	
        return null != findByUsername(username);
        
    }

    public boolean checkEmailExists(String email) {
    	
        return null != findByEmail(email);
        

    }

    public User saveUser(User user) {
    	
        return userDao.save(user);
        
    }

    public Page<User> findUserPage(Pageable pageable) {
        return userDao.findAll(pageable);
        
    }

    public void enableUser(String username) {
    	
        User user = findByUsername(username);
        user.setEnabled(true);
        userDao.save(user);
        
    }

    public void disableUser(String username) {
    	
        User user = findByUsername(username);
        user.setEnabled(false);
        logger.info("user status : {}", user.isEnabled());
        userDao.save(user);
        logger.info(username, "{}, is disabled.");
        
    }

}
