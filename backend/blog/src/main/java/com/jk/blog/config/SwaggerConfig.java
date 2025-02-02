package com.jk.blog.config;

import com.jk.blog.constants.SecurityConstants;
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

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .name(SecurityConstants.SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(SecurityConstants.SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SecurityConstants.SECURITY_SCHEME_NAME, createAPIKeyScheme()))
                .info(new Info()
                        .title("Blog API Documentation")
                        .description("API documentation for the Blog application")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Jitendra")
                                .url("https://www.jkblog.com/support")
                                .email("support@jkblog.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Blog Wiki Documentation")
                        .url("https://www.jkblog.com/wiki"));
    }
}