package com.re.busticketpro.service;

import com.re.busticketpro.dto.RegisterForm;
import com.re.busticketpro.entity.User;
import com.re.busticketpro.entity.UserProfile;
import com.re.busticketpro.enums.UserRole;
import com.re.busticketpro.enums.UserStatus;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.UserProfileRepository;
import com.re.busticketpro.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerPassenger(RegisterForm form) {
        String username = form.getUsername().trim();
        String phone = form.getPhone().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("T\u00ean \u0111\u0103ng nh\u1eadp \u0111\u00e3 t\u1ed3n t\u1ea1i");
        }
        if (userProfileRepository.existsByPhone(phone)) {
            throw new BusinessException("S\u1ed1 \u0111i\u1ec7n tho\u1ea1i \u0111\u00e3 \u0111\u01b0\u1ee3c s\u1eed d\u1ee5ng");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(UserRole.PASSENGER);
        user.setStatus(UserStatus.ACTIVE);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName(form.getFullName().trim());
        profile.setPhone(phone);
        profile.setEmail(form.getEmail());
        profile.setAddress(form.getAddress());
        user.setProfile(profile);
        userRepository.save(user);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Kh\u00f4ng t\u00ecm th\u1ea5y t\u00e0i kho\u1ea3n"));
    }
}
