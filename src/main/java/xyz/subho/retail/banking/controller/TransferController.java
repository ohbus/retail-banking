package xyz.subho.retail.banking.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.Recipient;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;

@Controller
@RequestMapping("/transfer")
public class TransferController {

    private static final int RECIPIENTS_PER_PAGE = 50;
    // shortcut: page 1,000 (50,000 recipients); use cursor pagination before deeper lists are required.
    private static final int MAX_PAGE = 1_000;

    private final TransactionService transactionService;
    private final UserService userService;

    public TransferController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @RequestMapping(value = "/betweenAccounts", method = RequestMethod.GET)
    public String betweenAccounts(Model model) {
    	
        model.addAttribute("transferFrom", "");
        model.addAttribute("transferTo", "");
        model.addAttribute("amount", "");

        return "betweenAccounts";
        
    }

    @RequestMapping(value = "/betweenAccounts", method = RequestMethod.POST)
    public String betweenAccountsPost(
            @ModelAttribute("transferFrom") String transferFrom,
            @ModelAttribute("transferTo") String transferTo,
            @ModelAttribute("amount") String amount,
            Principal principal,
            Model model
    ) {
        try {
            User user = userService.findByUsername(principal.getName());
            CurrentAccount currentAccount = user.getCurrentAccount();
            SavingsAccount savingsAccount = user.getSavingsAccount();
            transactionService.betweenAccountsTransfer(transferFrom, transferTo, amount, currentAccount, savingsAccount);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            model.addAttribute("transferFrom", transferFrom);
            model.addAttribute("transferTo", transferTo);
            model.addAttribute("amount", amount);
            return "betweenAccounts";
        } catch (OptimisticLockingFailureException exception) {
            model.addAttribute("error", "The account changed while processing. Please try again.");
            model.addAttribute("transferFrom", transferFrom);
            model.addAttribute("transferTo", transferTo);
            model.addAttribute("amount", amount);
            return "betweenAccounts";
        }

        return "redirect:/userFront";
        
    }

    @RequestMapping(value = "/recipient", method = RequestMethod.GET)
    public String recipient(Model model, Principal principal,
                            @RequestParam(defaultValue = "0") int page) {
        Page<Recipient> recipientPage = findRecipientPage(principal.getName(), page);

        Recipient recipient = new Recipient();

        model.addAttribute("recipientList", recipientPage.getContent());
        model.addAttribute("recipientPage", recipientPage);
        model.addAttribute("recipient", recipient);

        return "recipient";
        
    }

    @RequestMapping(value = "/recipient/save", method = RequestMethod.POST)
    public String recipientPost(@ModelAttribute("recipient") Recipient recipient, Principal principal, Model model) {

        try {
            User user = userService.findByUsername(principal.getName());
            recipient.setUser(user);
            transactionService.saveRecipient(recipient, principal.getName());
        } catch (IllegalArgumentException exception) {
            Page<Recipient> recipientPage = findRecipientPage(principal.getName(), 0);
            model.addAttribute("recipient", recipient);
            model.addAttribute("recipientList", recipientPage.getContent());
            model.addAttribute("recipientPage", recipientPage);
            model.addAttribute("error", exception.getMessage());
            return "recipient";
        }

        return "redirect:/transfer/recipient";
        
    }

    @RequestMapping(value = "/recipient/edit", method = RequestMethod.GET)
    public String recipientEdit(@RequestParam(value = "recipientId") Long recipientId, Model model, Principal principal) {

        Recipient recipient = transactionService.findRecipientById(recipientId, principal.getName());
        Page<Recipient> recipientPage = findRecipientPage(principal.getName(), 0);

        model.addAttribute("recipientList", recipientPage.getContent());
        model.addAttribute("recipientPage", recipientPage);
        model.addAttribute("recipient", recipient);

        return "recipient";
        
    }

    @RequestMapping(value = "/recipient/delete", method = RequestMethod.POST)
    public String recipientDelete(@RequestParam(value = "recipientId") Long recipientId, Model model, Principal principal) {

        transactionService.deleteRecipientById(recipientId, principal.getName());

        Page<Recipient> recipientPage = findRecipientPage(principal.getName(), 0);

        Recipient recipient = new Recipient();
        model.addAttribute("recipient", recipient);
        model.addAttribute("recipientList", recipientPage.getContent());
        model.addAttribute("recipientPage", recipientPage);

        return "recipient";
        
    }

    @RequestMapping(value = "/toSomeoneElse", method = RequestMethod.GET)
    public String toSomeoneElse(Model model, Principal principal) {
        Page<Recipient> recipientPage = findRecipientPage(principal.getName(), 0);
        model.addAttribute("recipientList", recipientPage.getContent());
        model.addAttribute("recipientPage", recipientPage);
        model.addAttribute("accountType", "");
        return "toSomeoneElse";
    }

    @RequestMapping(value = "/toSomeoneElse", method = RequestMethod.POST)
    public String toSomeoneElsePost(@ModelAttribute("recipientId") Long recipientId,
                                    @ModelAttribute("accountType") String accountType,
                                    @ModelAttribute("amount") String amount,
                                    Principal principal, Model model) {
        try {
            User user = userService.findByUsername(principal.getName());
            Recipient recipient = transactionService.findRecipientById(recipientId, principal.getName());
            transactionService.toSomeoneElseTransfer(recipient, accountType, amount,
                    user.getCurrentAccount(), user.getSavingsAccount());
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            Page<Recipient> recipientPage = findRecipientPage(principal.getName(), 0);
            model.addAttribute("recipientList", recipientPage.getContent());
            model.addAttribute("recipientPage", recipientPage);
            model.addAttribute("recipientId", recipientId);
            model.addAttribute("accountType", accountType);
            model.addAttribute("amount", amount);
            return "toSomeoneElse";
        } catch (OptimisticLockingFailureException exception) {
            model.addAttribute("error", "The account changed while processing. Please try again.");
            Page<Recipient> recipientPage = findRecipientPage(principal.getName(), 0);
            model.addAttribute("recipientList", recipientPage.getContent());
            model.addAttribute("recipientPage", recipientPage);
            model.addAttribute("recipientId", recipientId);
            model.addAttribute("accountType", accountType);
            model.addAttribute("amount", amount);
            return "toSomeoneElse";
        }
        return "redirect:/userFront";
    }

    private Page<Recipient> findRecipientPage(String username, int page) {
        int safePage = Math.min(Math.max(page, 0), MAX_PAGE);
        return transactionService.findRecipientPage(username, PageRequest.of(safePage, RECIPIENTS_PER_PAGE));
    }
    
}
