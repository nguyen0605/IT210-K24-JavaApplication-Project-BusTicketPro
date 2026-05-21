package com.re.busticketpro.controller;

import com.re.busticketpro.dto.LoginForm;
import com.re.busticketpro.dto.RegisterForm;
import com.re.busticketpro.enums.UserRole;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.UserRepository;
import com.re.busticketpro.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.registerPassenger(form);
        } catch (BusinessException ex) {
            if (ex.getMessage().contains("S\u1ed1 \u0111i\u1ec7n tho\u1ea1i")) {
                bindingResult.rejectValue("phone", "phone.exists", ex.getMessage());
            } else if (ex.getMessage().contains("T\u00ean \u0111\u0103ng nh\u1eadp")) {
                bindingResult.rejectValue("username", "username.exists", ex.getMessage());
            } else {
                bindingResult.reject("register", ex.getMessage());
            }
            return "auth/register";
        }
        redirectAttributes.addFlashAttribute("success", "\u0110\u0103ng k\u00fd th\u00e0nh c\u00f4ng. Vui l\u00f2ng \u0111\u0103ng nh\u1eadp");
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String passengerLogin(@Valid @ModelAttribute("form") LoginForm form, BindingResult bindingResult,
                                 HttpServletRequest request) {
        form.setLoginScope("PASSENGER");
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }
        return authenticate(form, UserRole.PASSENGER, "auth/login", "/", bindingResult, request);
    }

    @PostMapping("/staff/login")
    public String staffLogin(@Valid @ModelAttribute("form") LoginForm form, BindingResult bindingResult,
                             Model model, HttpServletRequest request) {
        form.setLoginScope("STAFF");
        addInternalLoginModel(model, "STAFF");
        if (bindingResult.hasErrors()) {
            return "auth/internal-login";
        }
        return authenticate(form, UserRole.STAFF, "auth/internal-login", "/staff", bindingResult, request);
    }

    @PostMapping("/admin/login")
    public String adminLogin(@Valid @ModelAttribute("form") LoginForm form, BindingResult bindingResult,
                             Model model, HttpServletRequest request) {
        form.setLoginScope("ADMIN");
        addInternalLoginModel(model, "ADMIN");
        if (bindingResult.hasErrors()) {
            return "auth/internal-login";
        }
        return authenticate(form, UserRole.ADMIN, "auth/internal-login", "/admin", bindingResult, request);
    }

    private String authenticate(LoginForm form, UserRole requiredRole, String errorView, String successUrl,
                                BindingResult bindingResult, HttpServletRequest request) {
        try {
            var authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                    form.getUsername().trim(), form.getPassword());
            var authentication = authenticationManager.authenticate(authRequest);
            var user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));
            if (user.getRole() != requiredRole) {
                bindingResult.reject("login.scope", "T\u00e0i kho\u1ea3n kh\u00f4ng c\u00f3 quy\u1ec1n \u0111\u0103ng nh\u1eadp t\u1ea1i khu v\u1ef1c n\u00e0y");
                return errorView;
            }
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            return "redirect:" + successUrl;
        } catch (AuthenticationException ex) {
            bindingResult.reject("login.failed", "T\u00ean \u0111\u0103ng nh\u1eadp ho\u1eb7c m\u1eadt kh\u1ea9u kh\u00f4ng \u0111\u00fang");
            return errorView;
        }
    }

    private void addInternalLoginModel(Model model, String scope) {
        model.addAttribute("loginScope", scope);
        model.addAttribute("loginAction", "STAFF".equals(scope) ? "/staff/login" : "/admin/login");
        model.addAttribute("loginTitle", "STAFF".equals(scope)
                ? "\u0110\u0103ng nh\u1eadp nh\u00e2n vi\u00ean"
                : "\u0110\u0103ng nh\u1eadp qu\u1ea3n tr\u1ecb");
        model.addAttribute("loginSubtitle", "STAFF".equals(scope)
                ? "Khu v\u1ef1c d\u00e0nh cho nh\u00e2n vi\u00ean \u0111\u01b0\u1ee3c c\u1ea5p quy\u1ec1n"
                : "Khu v\u1ef1c d\u00e0nh cho qu\u1ea3n tr\u1ecb vi\u00ean h\u1ec7 th\u1ed1ng");
        model.addAttribute("loginButton", "STAFF".equals(scope)
                ? "\u0110\u0103ng nh\u1eadp nh\u00e2n vi\u00ean"
                : "\u0110\u0103ng nh\u1eadp qu\u1ea3n tr\u1ecb");
        model.addAttribute("loginIcon", "admin_panel_settings");
    }
}
