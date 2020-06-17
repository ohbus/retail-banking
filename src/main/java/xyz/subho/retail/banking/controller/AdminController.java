package xyz.subho.retail.banking.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import xyz.subho.retail.banking.dao.RoleDao;
import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.CurrentTransaction;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.SavingsTransaction;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.security.UserRole;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserService userService;

	@Autowired
	private TransactionService transactionService;
	
    @Autowired
    private RoleDao roleDao;

	@GetMapping("/panel")
	public String profile(Principal principal, Model model) {

		if (!principal.getName().equals("Admin"))
			return "error";

		List<User> userList = userService.findUserList();
		model.addAttribute("userList", userList);

		return "adminPanel";

	}

	@PostMapping("/toggleUser")
	public String profileActivator(@ModelAttribute("username") String uname, Model model, Principal principal) {

		if (!principal.getName().equals("Admin") || uname.equals("Admin"))
			return "error";

		User user = userService.findByUsername(uname);
		user.setEnabled(!user.isEnabled());
		
		userService.saveUser(user);
		
		return "redirect:/admin/panel";

	}
	
	@PostMapping("/toggleUserSelf")
	public String profileSelfDeactivator(@ModelAttribute("username") String uname, Model model, Principal principal) {

		if(!principal.getName().equals(uname))
			return "error";

		User user = userService.findByUsername(uname);
		user.setEnabled(!user.isEnabled());
		
		userService.saveUser(user);
		
		return "redirect:/";

	}

	@RequestMapping("/currentAccount")
	public String currentAccount(@ModelAttribute("username") String uname, Model model, Principal principal) {

		if (!principal.getName().equals("Admin"))
			return "error";

		List<CurrentTransaction> currentTransactionList = transactionService
				.findCurrentTransactionList(uname);

		User user = userService.findByUsername(uname);
		CurrentAccount currentAccount = user.getCurrentAccount();

		model.addAttribute("currentAccount", currentAccount);
		model.addAttribute("currentTransactionList", currentTransactionList);

		return "currentAccount";

	}

	@RequestMapping("/savingsAccount")
	public String savingsAccount(@ModelAttribute("username") String uname, Model model, Principal principal) {

		if (!principal.getName().equals("Admin"))
			return "error";

		List<SavingsTransaction> savingsTransactionList = transactionService
				.findSavingsTransactionList(uname);
		User user = userService.findByUsername(uname);
		SavingsAccount savingsAccount = user.getSavingsAccount();

		model.addAttribute("savingsAccount", savingsAccount);
		model.addAttribute("savingsTransactionList", savingsTransactionList);

		return "savingsAccount";

	}
	
    @GetMapping("/signup")
    public String signup(Model model, Principal principal) {
    	
		if (!principal.getName().equals("Admin"))
			return "error";
    	
        User user = new User();

        model.addAttribute("user", user);

        return "adminSignup";
        
    }
	
    @SuppressWarnings("finally")
	@PostMapping("/signup")
    public String signupPost(@ModelAttribute("user") User user, Model model, Principal principal) {
    	
		if (!principal.getName().equals("Admin"))
			return "error";

        if (userService.checkUserExists(user.getUsername(), user.getEmail())) {

            if (userService.checkEmailExists(user.getEmail())) {
            	
                model.addAttribute("emailExists", true);
                
            }

            if (userService.checkUsernameExists(user.getUsername())) {
            	
                model.addAttribute("usernameExists", true);
                
            }

            return "adminSignup";
            
        } else {
        	try	{
        	
	            Set<UserRole> userRoles = new HashSet<>();
	            userRoles.add(new UserRole(user, roleDao.findByName("ROLE_ADMIN")));
	
	            userService.createUser(user, userRoles);
        	}
        	catch(Exception e)	{
        		model.addAttribute("errorSignUp", true);
        		e.printStackTrace();
        	}
        	finally	{
        		model.addAttribute("successSignUp", true);
        		return "redirect:/";
        	}
            
        }
        
    }

}
