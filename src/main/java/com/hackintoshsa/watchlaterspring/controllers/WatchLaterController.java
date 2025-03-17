package com.hackintoshsa.watchlaterspring.controllers;

import com.hackintoshsa.watchlaterspring.models.WatchLater;
import com.hackintoshsa.watchlaterspring.services.WatchLaterService;
import com.hackintoshsa.watchlaterspring.utils.JwtConfig;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/watchlater")
public class WatchLaterController {

    @Autowired
    WatchLaterService watchLaterService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private JwtConfig jwtConfig;


    @GetMapping("/list/{userId}")
    public ResponseEntity<?> getAllByUser(@PathVariable("userId") String userId,
                                          @RequestHeader("Authorization") String token) {
        HashMap<String, Object> response = new HashMap<>();

        // Validate token early in the method
        if (token == null || token.isEmpty() || !token.startsWith("Bearer ")) {
            response.put("message", "Missing or invalid authorization token");
            response.put("statusCode", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String tokenValue = token.substring("Bearer ".length()).trim();
        System.out.println("Extracted Token: " + tokenValue);

        try {
            // Validate the Google token
            if (!jwtConfig.validateGoogleToken(tokenValue)) {
                response.put("status", "error");
                response.put("message", "Invalid or expired token...");
                response.put("error", "Token validation failed");
                response.put("statusCode", 401);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Get user details from the JWT token
            Optional<Map<String, Object>> userDetailsOptional = jwtConfig.getUserInfoFromToken(tokenValue);

            // Check if user details are found in the token
            if (userDetailsOptional.isEmpty()) {
                response.put("message", "Invalid token or unable to parse user details");
                response.put("statusCode", 400);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Get the userId from the token's "sub" claim
            String userSub = userDetailsOptional.get().get("sub").toString();

            // Check if the userId from the token matches the URL path userId
            if (!userSub.equals(userId)) {
                response.put("message", "Token does not match the user ID in the URL");
                response.put("statusCode", 401);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Fetch the movie data for the user (service call)
            return ResponseEntity.status(HttpStatus.OK).body(watchLaterService.listAllByUser(userSub));

        } catch (Exception e) {
            response.put("message", "Error fetching data");
            response.put("statusCode", 500);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addWatchLater(@RequestBody WatchLater watchLater, @RequestHeader("userId") String userId){
        return ResponseEntity.status(HttpStatus.OK).body(watchLaterService.addMovieToWatchLater(userId, watchLater));
    }

    @DeleteMapping("/delete/{movieId}")
    public ResponseEntity<?> deleteByMovieId(@PathVariable("movieId") Integer movieId){
        return ResponseEntity.status(HttpStatus.OK).body(watchLaterService.deleteMovieFromWatchLater(movieId));
    }
}
