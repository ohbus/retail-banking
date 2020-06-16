package xyz.subho.retail.banking.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.AdminService;
import xyz.subho.retail.banking.service.UserService;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {
	
    @Autowired
    private UserService userService;
    
	public boolean deactivateAccount(String username) {
		
		boolean status = false;
		
		List<User> enabledUsers = userService.findUserList();
		
		//enabledUsers.get(username);
		
		return status;
	}

}
