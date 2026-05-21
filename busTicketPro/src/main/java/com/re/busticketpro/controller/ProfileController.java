package com.re.busticketpro.controller;

import com.re.busticketpro.dto.ProfileForm;
import com.re.busticketpro.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("form", profileService.getForm(authentication.getName()));
        return "profile/profile";
    }

    @PostMapping
    public String update(@Valid @ModelAttribute("form") ProfileForm form, BindingResult bindingResult,
                         Authentication authentication, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "profile/profile";
        }
        profileService.update(authentication.getName(), form);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật hồ sơ");
        return "redirect:/profile";
    }
}
