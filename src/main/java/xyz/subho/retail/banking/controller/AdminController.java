package xyz.subho.retail.banking.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.CurrentTransaction;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.SavingsTransaction;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.AccountService;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/panel")
    public String profile(Principal principal, Model model) {
    	
    	String str = principal.getName();
    	
    	if(!principal.getName().equals("Admin"))
    		return "error403";
    	
        List<User> userList = userService.findUserList();

        model.addAttribute("userList", userList);

        return "adminPanel";
        
    }

    @PostMapping("/deleteUser")
    public String profilePost(@ModelAttribute("user") User newUser, Model model, Principal principal) {
    	
    	if(principal.getName()!="Admin")
    		return "error403";
    	
        User user = userService.findByUsername(newUser.getUsername());
        user.setUsername(newUser.getUsername());
        user.setFirstName(newUser.getFirstName());
        user.setLastName(newUser.getLastName());
        user.setAadhaarId(newUser.getAadhaarId());
        user.setEmail(newUser.getEmail());
        user.setPhone(newUser.getPhone());

        model.addAttribute("user", user);

        userService.saveUser(user);

        return "profile";
        
    }
    
    @RequestMapping("/currentAccount")
    public String currentAccount(Model model, Principal principal) {
    	
    	if(principal.getName()!="Admin")
    		return "error403";
       
    	List<CurrentTransaction> currentTransactionList = transactionService.findCurrentTransactionList(principal.getName());

        User user = userService.findByUsername(principal.getName());
        CurrentAccount currentAccount = user.getCurrentAccount();

        model.addAttribute("currentAccount", currentAccount);
        model.addAttribute("currentTransactionList", currentTransactionList);

        return "currentAccount";
        
    }

    @RequestMapping("/savingsAccount")
    public String savingsAccount(Model model, Principal principal) {
    	
    	if(principal.getName()!="Admin")
    		return "error403";
        
    	List<SavingsTransaction> savingsTransactionList = transactionService.findSavingsTransactionList(principal.getName());
        User user = userService.findByUsername(principal.getName());
        SavingsAccount savingsAccount = user.getSavingsAccount();

        model.addAttribute("savingsAccount", savingsAccount);
        model.addAttribute("savingsTransactionList", savingsTransactionList);

        return "savingsAccount";
        
    }

}
