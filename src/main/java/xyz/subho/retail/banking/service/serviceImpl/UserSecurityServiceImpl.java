package xyz.subho.retail.banking.service.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import xyz.subho.retail.banking.dao.UserDao;
import xyz.subho.retail.banking.model.User;

@Service
public class UserSecurityServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserSecurityServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	
        User user = userDao.findByUsername(username);
        
        if (user == null) {
        	
            logger.warn("Username {} not found", username);
            throw new UsernameNotFoundException("Username " + username + " not found");
            
        }
        
        return user;
        
    }
    
}
