package com.hackintoshsa.watchlaterspring.services;


import com.hackintoshsa.watchlaterspring.models.User;
import com.hackintoshsa.watchlaterspring.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public User getOrCreateUser(Map<String, Object> user){
        // Fetch the user ID from the map
        String userId = (String) user.get("sub");
        String email = (String) user.get("email");


        Optional<User> existingUser = userRepository.findByEmailOrId(email, userId);

        if (existingUser.isPresent()) {
            return existingUser.get();  // Return the existing user if found
        }

        User saveUser = new User();
        saveUser.setId(userId);
        saveUser.setEmail(email);
        saveUser.setName((String) user.get("name"));  // Set name
        saveUser.setNickname((String) user.get("nickname"));  // Set nickname
        saveUser.setVerified(true);  // Assuming the user is verified
        saveUser.setPicture((String) user.get("picture"));  // Set picture
        return userRepository.save(saveUser);
    }

    public Optional<User> getUserBySub(String sub) {
        return userRepository.findBy(sub);
    }

}
