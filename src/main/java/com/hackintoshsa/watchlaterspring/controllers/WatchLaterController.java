package com.hackintoshsa.watchlaterspring.controllers;

import com.hackintoshsa.watchlaterspring.models.WatchLater;
import com.hackintoshsa.watchlaterspring.services.WatchLaterService;
import jakarta.websocket.server.PathParam;
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

@RestController
@RequestMapping("/api/watchlater")
public class WatchLaterController {

    @Autowired
    WatchLaterService watchLaterService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;


    @GetMapping("/list/{userId}")
    public ResponseEntity<?> getAllByUser(@PathParam("userId") String userId, @AuthenticationPrincipal Jwt jwt){
        HashMap<String, Object> response = new HashMap<>();

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        // Get the 'sub' claim (or any other claim) from the JWT token
        String userSub = jwt.getClaim("sub"); // 'sub' is usually the user ID

        if (userSub == null || !userSub.equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token does not match user");
        }

        // Proceed with fetching the data
        try {
            // Call your service with userSub (for example, to get watch later data for the user)
            return ResponseEntity.status(HttpStatus.OK).body(watchLaterService.listAllByUser(userSub));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching data");
        }



    }

    @PostMapping("/add")
    public ResponseEntity<?> addWatchLater(@RequestBody WatchLater watchLater, @RequestHeader("userId") String userId){
        return ResponseEntity.status(HttpStatus.OK).body(watchLaterService.addMovieToWatchLater(userId, watchLater));
    }

    @DeleteMapping("/delete/{movieId}")
    public ResponseEntity<?> deleteByMovieId(@PathVariable("movieId") String movieId){
        return ResponseEntity.status(HttpStatus.OK).body(watchLaterService.deleteMovieFromWatchLater(movieId));
    }
}
