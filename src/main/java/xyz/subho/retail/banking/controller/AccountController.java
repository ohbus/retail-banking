package xyz.subho.retail.banking.controller;

import java.security.Principal;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.CurrentTransaction;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.SavingsTransaction;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.AccountService;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;

@Controller
@RequestMapping("/account")
public class AccountController {

    private static final int TRANSACTIONS_PER_PAGE = 50;

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(UserService userService, AccountService accountService,
            TransactionService transactionService) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @RequestMapping("/currentAccount")
    public String currentAccount(Model model, Principal principal,
                                 @RequestParam(defaultValue = "0") int page) {
        Page<CurrentTransaction> currentTransactionPage = transactionService.findCurrentTransactionPage(
                principal.getName(), PageRequest.of(normalizePage(page), TRANSACTIONS_PER_PAGE));

        User user = userService.findByUsername(principal.getName());
        CurrentAccount currentAccount = user.getCurrentAccount();

        model.addAttribute("currentAccount", currentAccount);
        model.addAttribute("currentTransactionList", currentTransactionPage.getContent());
        model.addAttribute("currentTransactionPage", currentTransactionPage);

        return "currentAccount";
        
    }

    @RequestMapping("/savingsAccount")
    public String savingsAccount(Model model, Principal principal,
                                 @RequestParam(defaultValue = "0") int page) {
        Page<SavingsTransaction> savingsTransactionPage = transactionService.findSavingsTransactionPage(
                principal.getName(), PageRequest.of(normalizePage(page), TRANSACTIONS_PER_PAGE));
        User user = userService.findByUsername(principal.getName());
        SavingsAccount savingsAccount = user.getSavingsAccount();

        model.addAttribute("savingsAccount", savingsAccount);
        model.addAttribute("savingsTransactionList", savingsTransactionPage.getContent());
        model.addAttribute("savingsTransactionPage", savingsTransactionPage);

        return "savingsAccount";
        
    }

    @RequestMapping(value = "/deposit", method = RequestMethod.GET)
    public String deposit(Model model) {
        
    	model.addAttribute("accountType", "");
        model.addAttribute("amount", "");

        return "deposit";
        
    }

    @RequestMapping(value = "/deposit", method = RequestMethod.POST)
    public String depositPOST(@ModelAttribute("amount") String amount, @ModelAttribute("accountType") String accountType,
                              Principal principal, Model model) {
        try {
            accountService.deposit(accountType, parseAmount(amount), principal);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            model.addAttribute("accountType", accountType);
            model.addAttribute("amount", amount);
            return "deposit";
        }

        return "redirect:/userFront";
        
    }

    @RequestMapping(value = "/withdraw", method = RequestMethod.GET)
    public String withdraw(Model model) {
    	
        model.addAttribute("accountType", "");
        model.addAttribute("amount", "");

        return "withdraw";
        
    }

    @RequestMapping(value = "/withdraw", method = RequestMethod.POST)
    public String withdrawPOST(@ModelAttribute("amount") String amount, @ModelAttribute("accountType") String accountType,
                               Principal principal, Model model) {
        try {
            accountService.withdraw(accountType, parseAmount(amount), principal);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            model.addAttribute("accountType", accountType);
            model.addAttribute("amount", amount);
            return "withdraw";
        }

        return "redirect:/userFront";
        
    }

    private BigDecimal parseAmount(String amount) {
        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException | NullPointerException exception) {
            throw new IllegalArgumentException("Amount is invalid", exception);
        }
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }
    
}
