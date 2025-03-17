package com.hackintoshsa.watchlaterspring.controllers;

import com.hackintoshsa.watchlaterspring.models.User;
import com.hackintoshsa.watchlaterspring.services.UserService;
import com.hackintoshsa.watchlaterspring.utils.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Log
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    UserService userService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private JwtConfig jwtConfig;


    @GetMapping("/loginSuccess")
    public ResponseEntity<?> getUserInfoAndCreateIfNotExist(@RequestHeader("Authorization") String token) {
        log.info("From API: {}" +token);  // Corrected log statement




        Map<String, Object> response = new HashMap<>();
        String tokenValue = "";
        try {

        if (token != null && token.startsWith("Bearer ")) {
            String rawToken = token.substring("Bearer ".length()).trim();
            System.out.println("Extracted Token: " + rawToken);
            tokenValue = rawToken;
        }
        System.out.println("Received token from @HeaderParam: " + tokenValue );

        if (token == null || token.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Missing authorization token.");
            response.put("statusCode", 400);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!jwtConfig.validateGoogleToken(tokenValue)) {
            response.put("status", "error");
            response.put("message", "Invalid or expired token.");
            response.put("error", "Token validation failed");
            response.put("statusCode", 401);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<Map<String, Object>> userDetailsOptional = jwtConfig.getUserInfoFromToken(tokenValue);

        if (userDetailsOptional .isEmpty()) {
            response.put("message", "Error retrieving user details from token");
            response.put("statusCode", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Failed to extract user info");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Map<String, Object> userDetails = userDetailsOptional.get();
        String name = (String) userDetails.get("name");
        String email = (String) userDetails.get("email");
        String picture = (String) userDetails.get("picture");
        String userId = (String) userDetails.get("sub");


            if (name == null || email == null || picture == null) {
                response.put("message", "Required user details are missing");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                response.put("error", "Missing required user details");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("picture", picture);
            userData.put("sub", userId);

            User user = userService.getOrCreateUser(userData);

            response.put("statusCode", HttpStatus.OK.value());
            response.put("message", "Successfully authenticated, welcome!");
            response.put("error", null);
            response.put("data", userData);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.put("message", "An unexpected error occurred");
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
