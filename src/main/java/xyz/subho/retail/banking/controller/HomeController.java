package xyz.subho.retail.banking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import xyz.subho.retail.banking.entity.User;

@Controller
public class HomeController {
	
	@RequestMapping("/")
	public String home()	{
		return "redirect:/index";
	}
	
	@RequestMapping("/index")
	public String index()	{
		return "index";
	}
	
    @GetMapping("/signup")
    public String signup(Model model) {
        User user = new User();

        model.addAttribute("user", user);

        return "signup";
    }

}
