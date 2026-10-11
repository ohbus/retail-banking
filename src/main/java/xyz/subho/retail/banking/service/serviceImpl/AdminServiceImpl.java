package xyz.subho.retail.banking.service.serviceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import xyz.subho.retail.banking.service.AdminService;
import xyz.subho.retail.banking.service.UserService;
import xyz.subho.retail.banking.model.User;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

	private final UserService userService;

	public AdminServiceImpl(UserService userService) {
		this.userService = userService;
	}
	
	public boolean deactivateAccount(String username) {
		User user = userService.findByUsername(username);
		if (user == null || !user.isEnabled()) {
			return false;
		}
		user.setEnabled(false);
		userService.saveUser(user);
		return true;
	}

}
