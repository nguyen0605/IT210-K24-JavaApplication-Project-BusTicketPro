package com.re.busticketpro.controller;

import com.re.busticketpro.exception.BusinessException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {
    @ModelAttribute
    public void csrfToken(CsrfToken csrfToken) {
        if (csrfToken != null) {
            csrfToken.getToken();
        }
    }

    @ExceptionHandler(BusinessException.class)
    public String businessError(BusinessException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error";
    }
}
