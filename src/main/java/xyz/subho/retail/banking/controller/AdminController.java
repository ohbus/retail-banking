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

import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.CurrentTransaction;
import xyz.subho.retail.banking.model.SavingsAccount;
import xyz.subho.retail.banking.model.SavingsTransaction;
import xyz.subho.retail.banking.model.User;
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

		if (!principal.getName().equals("Admin"))
			return "error";

		List<User> userList = userService.findUserList();
		model.addAttribute("userList", userList);

		return "adminPanel";

	}

	@PostMapping("/toggleUser")
	public String profilePost(@ModelAttribute("username") String uname, Model model, Principal principal) {

		if (!principal.getName().equals("Admin") || uname.equals("Admin"))
			return "error";

		User user = userService.findByUsername(uname);
		user.setEnabled(!user.isEnabled());
		
		userService.saveUser(user);
		
		return "redirect:/admin/panel";

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

}
