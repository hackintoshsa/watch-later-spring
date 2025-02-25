package com.hackintoshsa.watchlaterspring.configs;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${biskop.logoutRedirectUrl}")
    private String logoutRedirectUrl;

    @Value("${biskop.allowedOrigins}")
    private List<String> allowedOrigins;

    @Value("${biskop.homeUrl}")
    private String homeUrl;

    private final ClientRegistrationRepository clientRegistrationRepository;



    // Constructor injection for ClientRegistrationRepository
    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    private LogoutSuccessHandler logoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);

        logoutSuccessHandler.setPostLogoutRedirectUri(logoutRedirectUrl);
        return logoutSuccessHandler;
    }
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
    config.setExposedHeaders(List.of("Authorization"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Configure CORS
                //.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // Session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/auth/**").permitAll()  // Require authentication for /auth endpoints
                                .anyRequest().permitAll())  // Require authentication for all other endpoints
//                .oauth2Login(oauth -> oauth
//                        .successHandler((request, response, authentication) -> {
//                            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
//                            String idToken = ((OidcUser) oauth2User).getIdToken().getTokenValue(); // Get Google ID Token
//
//                            response.sendRedirect("http://localhost:4200?token=" + idToken);
//                        })
//                        .defaultSuccessUrl("http://localhost:4200"))
                .oauth2Login(oauth -> oauth
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                            String idToken = ((OidcUser) oauth2User).getIdToken().getTokenValue();

                            // Send token as JSON response
//                            response.setContentType("application/json");
//                            response.setStatus(HttpStatus.OK.value());
//                            response.getWriter().write("{\"token\": \"" + idToken + "\"}");
                            response.sendRedirect("http://localhost:4200/home?token=" + idToken);
                        })
                )
                .exceptionHandling(exceptionHandler ->exceptionHandler.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Enable OAuth2 login with defaults
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthenticationConverter()))
//                )  // Enable JWT validation for resource server
                .logout(logout -> logout
                        .logoutUrl("/logout").permitAll().clearAuthentication(true)
                        //.logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())  // Configure logout success handler
                        .invalidateHttpSession(true)  // Invalidate session
                        .deleteCookies("auth_code", "JSESSIONID", "refreshToken", "Authorization"));  // Delete JSESSIONID cookie

        return http.build();
    }
}