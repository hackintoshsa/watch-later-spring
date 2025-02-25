package com.hackintoshsa.watchlaterspring.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI watchLaterAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Watch Later - Biskop API")
                        .description("API documentation for the Watch Later feature in Biskop, a streaming platform.")
                        .version("v1.0.0")
                        .termsOfService("https://biskop.com/terms")
                        .contact(new Contact()
                                .name("Biskop Support")
                                .email("info@biskop.com")
                                .url("https://biskop.fun"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT-based authentication for securing API endpoints.")))
                .externalDocs(new ExternalDocumentation()
                        .description("Biskop API GitHub Repository")
                        .url("https://github.com/hackintoshsa/biskop-api"));
    }
}
