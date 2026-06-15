package escrims.controller.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI escrimsOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .info(new Info()
                        .title("eScrims API")
                        .version("1.0.0")
                        .description("API REST para gestionar scrims competitivos, postulaciones, confirmaciones, scheduler, notificaciones y estadisticas."));
    }

    @Bean
    public OperationCustomizer hideAuthorizationHeaderParameter() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() != null) {
                operation.setParameters(operation.getParameters().stream()
                        .filter(this::isNotAuthorizationHeader)
                        .toList());
            }
            return operation;
        };
    }

    private boolean isNotAuthorizationHeader(Parameter parameter) {
        return !"header".equals(parameter.getIn()) || !"Authorization".equalsIgnoreCase(parameter.getName());
    }
}
