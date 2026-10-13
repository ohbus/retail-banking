package xyz.subho.retail.banking.service.serviceImpl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

import xyz.subho.retail.banking.dao.UserDao;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.security.UserRole;
import xyz.subho.retail.banking.service.UserService;
import xyz.subho.retail.banking.service.AccountNumberService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final int MAX_TEXT_LENGTH = 255;
    private static final int MAX_PASSWORD_BYTES = 72;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;
    private final AccountNumberService accountNumberService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDao userDao, AccountNumberService accountNumberService,
            BCryptPasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.accountNumberService = accountNumberService;
        this.passwordEncoder = passwordEncoder;
    }

    

    public void save(User user) {
    	
        userDao.save(user);
        
    }

    public User findByUsername(String username) {
        if (username == null || username.length() > MAX_TEXT_LENGTH || username.isBlank()) {
            return null;
        }
        return userDao.findByUsername(username);
        
    }

    public User findByEmail(String email) {
        if (email == null || email.length() > MAX_TEXT_LENGTH || email.isBlank()) {
            return null;
        }
        return userDao.findByEmail(email);
    }


    public User createUser(User user, Set<UserRole> userRoles) {
    	
        if (user == null) {
            throw new IllegalArgumentException("Registration is required");
        }
        validateRegistration(user);
        User localUser = userDao.findByUsername(user.getUsername());

        if (localUser != null) {
        	
            logger.info("User with username {} already exist. Nothing will be done. ", user.getUsername());
            
        } else {

            String encryptedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encryptedPassword);

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

    private void validateRegistration(User user) {
        if (user == null || isBlank(user.getUsername()) || isBlank(user.getPassword())
                || user.getPassword().length() < 12 || exceedsPasswordBytes(user.getPassword())
                || isBlank(user.getFirstName()) || isBlank(user.getLastName())
                || isBlank(user.getAadhaarId()) || isBlank(user.getEmail()) || isBlank(user.getPhone())
                || exceedsTextLength(user.getUsername()) || exceedsTextLength(user.getFirstName())
                || exceedsTextLength(user.getLastName()) || exceedsTextLength(user.getAadhaarId())
                || exceedsTextLength(user.getEmail()) || exceedsTextLength(user.getPhone())) {
            throw new IllegalArgumentException("All registration fields are required and the password must be at least 12 characters");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean exceedsTextLength(String value) {
        return value != null && value.length() > MAX_TEXT_LENGTH;
    }

    private boolean exceedsPasswordBytes(String value) {
        return value != null && value.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES;
    }

    public boolean checkUserExists(String username, String email) {
        if (isBlank(username) || isBlank(email) || exceedsTextLength(username) || exceedsTextLength(email)) {
            return false;
        }
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
        return userDao.findAllByOrderByUserIdAsc(pageable);
        
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
        logger.info("{}, is disabled.", username);
        
    }

}
