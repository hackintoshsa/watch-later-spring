package com.hackintoshsa.watchlaterspring.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.*;

@Component
@Log
public class JwtConfig {


    @Autowired
    private RestTemplate restTemplate;

//    private final String issuerUri = "https://accounts.google.com";

    //@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private final String issuerUri = "https://accounts.google.com";
    private static final String GOOGLE_JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";

    //@Value("${spring.security.oauth2.client.registration.google.client-id}")
    private final String clientId = "166193956668-sqolic5sodfo47e2pg6q7nf63rfs50i1.apps.googleusercontent.com";


    private JwtDecoder jwtDecoder;

    private final ClientRegistrationRepository clientRegistrationRepository;

//    public JwtConfig(ClientRegistrationRepository clientRegistrationRepository) {
//        this.clientRegistrationRepository = clientRegistrationRepository;
//    }

    public JwtConfig(ClientRegistrationRepository clientRegistrationRepository) {
        try {
            this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS_URI)
                    .jwtProcessorCustomizer(customizer ->
                            customizer.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                                    new JWTClaimsSet.Builder().issuer(issuerUri).build(),
                                    new HashSet<>(Arrays.asList("sub", "aud", "exp", "iat"))
                            ))
                    ).build();
//            this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS_URI)
//                    .jwtProcessorCustomizer(customizer ->
//                            customizer.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
//                                            new JWTClaimsSet.Builder().issuer(issuerUri).build(),
//                                            new HashSet<>(Arrays.asList("sub", "aud", "exp", "iat"))
//                                    ))
//                                    .build());
        } catch (HttpClientErrorException.NotFound e) {
            // Log the error if the URL is not found
            log.warning("JWK Set not found at URL: " + GOOGLE_JWKS_URI + e);
            // You can throw an exception, provide a fallback URL, or take any necessary action
        } catch (Exception e) {
            // Handle other potential exceptions
            log.warning("Error occurred while initializing JWT Decoder" + e);
        }
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    // Validate the Google OAuth2 token
    public boolean validateGoogleToken(String token) {
        log.warning("Validating Google token");
        try {
            Jwt jwt = jwtDecoder.decode(token);


            log.info("jwt" + jwt);

            // Security: Verify issuer claim
            if (!issuerUri.equals(jwt.getIssuer().toString())) {
                log.warning("Invalid token issuer: {}" + jwt.getIssuer());
                return false;
            }

            // Validate audience claim
//            String audience = jwt.getClaimAsString("aud");
//            if (!clientId.equals(audience)) {
//                log.warning("Token audience mismatch. Expected: {} Actual: {}" + clientId +audience);
//                return false;
//            }

            // Check expiration
            if (jwt.getExpiresAt().isBefore(Instant.now())) {
                log.warning("Expired token");
                return false;
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warning("Token validation failed: {}" + e.getMessage());
            return false;
        }
    }
    // This method is used to extract user details from the validated token.
    public Optional<Map<String, Object>> getUserInfoFromToken(String token) {
        try {
            //JwtDecoder jwtDecoder = NimbusJ
            // wtDecoder.withJwkSetUri(issuerUri + "/.well-known/jwks.json").build();
            Jwt jwt = jwtDecoder.decode(token);

            // Extract user info from the JWT payload
            String name = jwt.getClaimAsString("name");
            String email = jwt.getClaimAsString("email");
            String picture = jwt.getClaimAsString("picture");
            String sub = jwt.getClaimAsString("sub");


            Map<String, Object> userInfo = Map.of(
                    "name", name,
                    "email", email,
                    "picture", picture,
                    "sub", sub
            );

            return Optional.of(userInfo);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
