package xyz.subho.retail.banking.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
    	
        User user = userService.findByUsername(principal.getName());

        model.addAttribute("user", user);

        return "profile";
        
    }

    @PostMapping("/profile")
    public String profilePost(@ModelAttribute("user") User newUser, Model model, Principal principal) {
    	
        User user = userService.findByUsername(principal.getName());
        if (hasBlankProfileField(newUser)) {
            model.addAttribute("user", user);
            model.addAttribute("validationError", "All profile fields are required");
            return "profile";
        }

        String existingEmail = user.getEmail();
        user.setFirstName(newUser.getFirstName());
        user.setLastName(newUser.getLastName());
        user.setAadhaarId(newUser.getAadhaarId());
        user.setEmail(newUser.getEmail());
        user.setPhone(newUser.getPhone());

        model.addAttribute("user", user);

        try {
            userService.saveUser(user);
        } catch (DataIntegrityViolationException exception) {
            user.setEmail(existingEmail);
            model.addAttribute("emailExists", true);
        }

        return "profile";
        
    }

    private boolean hasBlankProfileField(User user) {
        return user == null || user.getFirstName() == null || user.getFirstName().isBlank()
                || user.getLastName() == null || user.getLastName().isBlank()
                || user.getAadhaarId() == null || user.getAadhaarId().isBlank()
                || user.getEmail() == null || user.getEmail().isBlank()
                || user.getPhone() == null || user.getPhone().isBlank()
                || exceedsTextLength(user.getFirstName()) || exceedsTextLength(user.getLastName())
                || exceedsTextLength(user.getAadhaarId()) || exceedsTextLength(user.getEmail())
                || exceedsTextLength(user.getPhone());
    }

    private boolean exceedsTextLength(String value) {
        return value != null && value.length() > 255;
    }

}
