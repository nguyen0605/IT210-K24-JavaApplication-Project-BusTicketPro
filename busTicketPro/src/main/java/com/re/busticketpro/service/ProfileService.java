package com.re.busticketpro.service;

import com.re.busticketpro.dto.ProfileForm;
import com.re.busticketpro.entity.UserProfile;
import com.re.busticketpro.repository.UserProfileRepository;
import com.re.busticketpro.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;

    public ProfileService(UserRepository userRepository, UserProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public ProfileForm getForm(String username) {
        var profile = userRepository.findByUsername(username).orElseThrow().getProfile();
        ProfileForm form = new ProfileForm();
        form.setFullName(profile.getFullName());
        form.setPhone(profile.getPhone());
        form.setEmail(profile.getEmail());
        form.setAddress(profile.getAddress());
        return form;
    }

    @Transactional
    public void update(String username, ProfileForm form) {
        var user = userRepository.findByUsername(username).orElseThrow();
        UserProfile profile = profileRepository.findByUserId(user.getId()).orElseThrow();
        profile.setFullName(form.getFullName().trim());
        profile.setPhone(form.getPhone().trim());
        profile.setEmail(form.getEmail());
        profile.setAddress(form.getAddress());
    }
}
