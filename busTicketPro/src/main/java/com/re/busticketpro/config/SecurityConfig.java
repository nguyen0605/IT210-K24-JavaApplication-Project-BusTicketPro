package com.re.busticketpro.config;

import com.re.busticketpro.enums.UserRole;
import com.re.busticketpro.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    AuthenticationSuccessHandler successHandler(UserRepository userRepository) {
        return (request, response, authentication) -> {
            var user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            String loginScope = request.getParameter("loginScope");
            if (loginScope == null || loginScope.isBlank()) {
                loginScope = scopeFromReferer(request.getHeader("Referer"));
            }

            if (!roleMatchesLoginScope(user.getRole(), loginScope)) {
                SecurityContextHolder.clearContext();
                var session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(loginPath(loginScope) + "?error");
                return;
            }

            if (user.getRole() == UserRole.ADMIN) {
                response.sendRedirect("/admin");
            } else if (user.getRole() == UserRole.STAFF) {
                response.sendRedirect("/staff");
            } else {
                response.sendRedirect("/");
            }
        };
    }

    private static String scopeFromReferer(String referer) {
        if (referer != null && referer.contains("/admin/login")) {
            return "ADMIN";
        }
        if (referer != null && referer.contains("/staff/login")) {
            return "STAFF";
        }
        return "PASSENGER";
    }

    private static boolean roleMatchesLoginScope(UserRole role, String loginScope) {
        return switch (loginScope) {
            case "ADMIN" -> role == UserRole.ADMIN;
            case "STAFF" -> role == UserRole.STAFF;
            default -> role == UserRole.PASSENGER;
        };
    }

    private static String loginPath(String loginScope) {
        return switch (loginScope) {
            case "ADMIN" -> "/admin/login";
            case "STAFF" -> "/staff/login";
            default -> "/login";
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationSuccessHandler successHandler) throws Exception {
        LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
        entryPoints.put(new AntPathRequestMatcher("/admin/**"), new LoginUrlAuthenticationEntryPoint("/admin/login"));
        entryPoints.put(new AntPathRequestMatcher("/staff/**"), new LoginUrlAuthenticationEntryPoint("/staff/login"));
        DelegatingAuthenticationEntryPoint authenticationEntryPoint = new DelegatingAuthenticationEntryPoint(entryPoints);
        authenticationEntryPoint.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/staff/login", "/admin/login", "/trips/**", "/tickets/lookup", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/booking/**", "/profile/**", "/tickets/cancel", "/tickets/my").hasRole("PASSENGER")
                        .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login")
                        .loginProcessingUrl("/security-login")
                        .failureHandler((request, response, exception) -> {
                            String referer = request.getHeader("Referer");
                            if (referer != null && (referer.contains("/staff/login") || referer.contains("/admin/login"))) {
                                response.sendRedirect(referer.split("\\?")[0] + "?error");
                            } else {
                                response.sendRedirect("/login?error");
                            }
                        })
                        .successHandler(successHandler).permitAll())
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint).accessDeniedPage("/access-denied"));
        return http.build();
    }
}
