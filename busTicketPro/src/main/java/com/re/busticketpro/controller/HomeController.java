package com.re.busticketpro.controller;

import com.re.busticketpro.dto.LoginForm;
import com.re.busticketpro.dto.TripSearchForm;
import com.re.busticketpro.service.TripSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final TripSearchService tripSearchService;

    public HomeController(TripSearchService tripSearchService) {
        this.tripSearchService = tripSearchService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("searchForm", new TripSearchForm());
        model.addAttribute("locations", tripSearchService.locations());
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        LoginForm form = new LoginForm();
        form.setLoginScope("PASSENGER");
        model.addAttribute("form", form);
        return "auth/login";
    }

    @GetMapping("/staff/login")
    public String staffLogin(Model model) {
        LoginForm form = new LoginForm();
        form.setLoginScope("STAFF");
        model.addAttribute("form", form);
        model.addAttribute("loginScope", "STAFF");
        model.addAttribute("loginAction", "/staff/login");
        model.addAttribute("loginTitle", "\u0110\u0103ng nh\u1eadp nh\u00e2n vi\u00ean");
        model.addAttribute("loginSubtitle", "Khu v\u1ef1c d\u00e0nh cho nh\u00e2n vi\u00ean \u0111\u01b0\u1ee3c c\u1ea5p quy\u1ec1n");
        model.addAttribute("loginButton", "\u0110\u0103ng nh\u1eadp nh\u00e2n vi\u00ean");
        model.addAttribute("loginIcon", "admin_panel_settings");
        return "auth/internal-login";
    }

    @GetMapping("/admin/login")
    public String adminLogin(Model model) {
        LoginForm form = new LoginForm();
        form.setLoginScope("ADMIN");
        model.addAttribute("form", form);
        model.addAttribute("loginScope", "ADMIN");
        model.addAttribute("loginAction", "/admin/login");
        model.addAttribute("loginTitle", "\u0110\u0103ng nh\u1eadp qu\u1ea3n tr\u1ecb");
        model.addAttribute("loginSubtitle", "Khu v\u1ef1c d\u00e0nh cho qu\u1ea3n tr\u1ecb vi\u00ean h\u1ec7 th\u1ed1ng");
        model.addAttribute("loginButton", "\u0110\u0103ng nh\u1eadp qu\u1ea3n tr\u1ecb");
        model.addAttribute("loginIcon", "admin_panel_settings");
        return "auth/internal-login";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("error", "B\u1ea1n kh\u00f4ng c\u00f3 quy\u1ec1n truy c\u1eadp ch\u1ee9c n\u0103ng n\u00e0y");
        return "error";
    }
}
