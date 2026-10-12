package xyz.subho.retail.banking.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.security.access.prepost.PreAuthorize;

import xyz.subho.retail.banking.dao.RoleDao;
import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.CurrentTransaction;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.SavingsTransaction;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.security.UserRole;
import xyz.subho.retail.banking.security.SessionInvalidationService;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private static final int TRANSACTIONS_PER_PAGE = 50;

	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	private final UserService userService;
	private final TransactionService transactionService;
    private final RoleDao roleDao;
    private final SessionInvalidationService sessionInvalidationService;

    public AdminController(UserService userService, TransactionService transactionService, RoleDao roleDao,
            SessionInvalidationService sessionInvalidationService) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.roleDao = roleDao;
        this.sessionInvalidationService = sessionInvalidationService;
    }

    @InitBinder("user")
    void protectUserFields(WebDataBinder binder) {
        binder.setDisallowedFields("userId", "enabled", "currentAccount*", "savingsAccount*",
                "recipientList*", "userRoles*");
    }

	@GetMapping("/panel")
	@PreAuthorize("hasRole('ADMIN')")
	public String profile(Model model, @RequestParam(defaultValue = "0") int page) {
		Page<User> userPage = userService.findUserPage(PageRequest.of(Math.max(page, 0), TRANSACTIONS_PER_PAGE));
		model.addAttribute("userList", userPage.getContent());
		model.addAttribute("userPage", userPage);

		return "adminPanel";

	}

	@PostMapping("/toggleUser")
	@PreAuthorize("hasRole('ADMIN')")
	public String profileActivator(@ModelAttribute("username") String uname, Model model) {
		User user = userService.findByUsername(uname);
		user.setEnabled(!user.isEnabled());
		
		userService.saveUser(user);
		sessionInvalidationService.expireSessions(uname);
		
		return "redirect:/admin/panel";

	}
	
	@PostMapping("/toggleUserSelf")
	public String profileSelfDeactivator(@ModelAttribute("username") String uname, Model model, Principal principal) {

		if(!principal.getName().equals(uname))
			return "error";

		User user = userService.findByUsername(uname);
		user.setEnabled(!user.isEnabled());
		
		userService.saveUser(user);
		sessionInvalidationService.expireSessions(uname);
		
		return "redirect:/";

	}

	@RequestMapping("/currentAccount")
	@PreAuthorize("hasRole('ADMIN')")
	public String currentAccount(@ModelAttribute("username") String uname, Model model,
			@RequestParam(defaultValue = "0") int page) {
		Page<CurrentTransaction> currentTransactionPage = transactionService
				.findCurrentTransactionPage(uname, PageRequest.of(Math.max(page, 0), TRANSACTIONS_PER_PAGE));

		User user = userService.findByUsername(uname);
		CurrentAccount currentAccount = user.getCurrentAccount();

		model.addAttribute("currentAccount", currentAccount);
		model.addAttribute("currentTransactionList", currentTransactionPage.getContent());
		model.addAttribute("currentTransactionPage", currentTransactionPage);
		model.addAttribute("adminView", true);
		model.addAttribute("viewedUsername", uname);

		return "currentAccount";

	}

	@RequestMapping("/savingsAccount")
	@PreAuthorize("hasRole('ADMIN')")
	public String savingsAccount(@ModelAttribute("username") String uname, Model model,
			@RequestParam(defaultValue = "0") int page) {
		Page<SavingsTransaction> savingsTransactionPage = transactionService
				.findSavingsTransactionPage(uname, PageRequest.of(Math.max(page, 0), TRANSACTIONS_PER_PAGE));
		User user = userService.findByUsername(uname);
		SavingsAccount savingsAccount = user.getSavingsAccount();

		model.addAttribute("savingsAccount", savingsAccount);
		model.addAttribute("savingsTransactionList", savingsTransactionPage.getContent());
		model.addAttribute("savingsTransactionPage", savingsTransactionPage);
		model.addAttribute("adminView", true);
		model.addAttribute("viewedUsername", uname);

		return "savingsAccount";

	}
	
    @GetMapping("/signup")
    @PreAuthorize("hasRole('ADMIN')")
    public String signup(Model model) {
        User user = new User();

        model.addAttribute("user", user);

        return "adminSignup";
        
    }
	
	@PostMapping("/signup")
    @PreAuthorize("hasRole('ADMIN')")
    public String signupPost(@ModelAttribute("user") User user, Model model) {
        if (userService.checkUserExists(user.getUsername(), user.getEmail())) {

            if (userService.checkEmailExists(user.getEmail())) {
            	
                model.addAttribute("emailExists", true);
                
            }

            if (userService.checkUsernameExists(user.getUsername())) {
            	
                model.addAttribute("usernameExists", true);
                
            }

            return "adminSignup";
            
        } else {
            try {
        	
	            Set<UserRole> userRoles = new HashSet<>();
	            userRoles.add(new UserRole(user, roleDao.findByName("ROLE_ADMIN")));
	
	            userService.createUser(user, userRoles);
	            return "redirect:/";
	        } catch (Exception e) {
        		model.addAttribute("errorSignUp", true);
	            logger.error("Unable to create admin user {}", user.getUsername(), e);
	            return "adminSignup";
	        }
            
        }
        
    }

}
