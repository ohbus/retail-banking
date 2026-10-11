package xyz.subho.retail.banking.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import xyz.subho.retail.banking.dao.RoleDao;
import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.security.UserRole;
import xyz.subho.retail.banking.service.UserService;

@Controller
public class HomeController {

	private static final String PATH = "/error";
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
    private final UserService userService;
    private final RoleDao roleDao;

    public HomeController(UserService userService, RoleDao roleDao) {
        this.userService = userService;
        this.roleDao = roleDao;
    }

    @RequestMapping("/")
    public String home() {
    	
        return "redirect:/index";
        
    }

    @RequestMapping("/index")
    public String index() {
    	
        return "index";
        
    }

    @GetMapping("/signup")
    public String signup(Model model) {
    	
        User user = new User();

        model.addAttribute("user", user);

        return "signup";
        
    }

	@PostMapping("/signup")
    public String signupPost(@ModelAttribute("user") User user, Model model) {

        if (userService.checkUserExists(user.getUsername(), user.getEmail())) {

            if (userService.checkEmailExists(user.getEmail())) {
            	
                model.addAttribute("emailExists", true);
                
            }

            if (userService.checkUsernameExists(user.getUsername())) {
            	
                model.addAttribute("usernameExists", true);
                
            }

            return "signup";
            
        } else {
            try {
        	
	            Set<UserRole> userRoles = new HashSet<>();
	            userRoles.add(new UserRole(user, roleDao.findByName("ROLE_USER")));
	
	            userService.createUser(user, userRoles);
	            return "redirect:/";
	        } catch (Exception e) {
        		model.addAttribute("errorSignUp", true);
	            logger.error("Unable to create user {}", user.getUsername(), e);
	            return "signup";
	        }
            
        }
        
    }

    @GetMapping("/userFront")
    public String userFront(Principal principal, Model model) {
    	
        User user = userService.findByUsername(principal.getName());
        CurrentAccount currentAccount = user.getCurrentAccount();
        SavingsAccount savingsAccount = user.getSavingsAccount();

        model.addAttribute("currentAccount", currentAccount);
        model.addAttribute("savingsAccount", savingsAccount);
		/* model.addAttribute("user", user); */

        return "userFront";
        
    }
    
    

    
}
