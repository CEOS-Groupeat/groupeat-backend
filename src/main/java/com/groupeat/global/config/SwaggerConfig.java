package com.groupeat.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private final String prodServerUrl;

    public SwaggerConfig(
            @Value("${app.swagger.prod-server-url}") String prodServerUrl
    ) {
        this.prodServerUrl = prodServerUrl;
    }

    @Bean
    public OpenAPI groupeatAPI() {
        Info info = new Info()
                .title("Groupeat API")
                .description("Groupeat 백엔드 API 명세서입니다.")
                .version("1.0");

        String jwtSchemeName = "bearerAuth";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        Server prodServer = new Server().url(prodServerUrl).description("운영 서버 (HTTPS)");
        Server localServer = new Server().url("http://localhost:8080").description("로컬 테스트용 (HTTP)");

        return new OpenAPI()
                .info(info)
                .addServersItem(prodServer)
                .addServersItem(localServer)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
