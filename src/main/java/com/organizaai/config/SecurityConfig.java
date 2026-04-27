package com.organizaai.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // O seu filtro JWT que já validamos e está perfeito
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 1. ÁREA VIP: Rotas públicas gerais (Login, Documentação, etc)
    private static final String[] ROTAS_PUBLICAS_GERAIS = {
            "/auth/**",
            "/auth/login",
            "/auth/register",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/error" // Libera a leitura de erros nativos
    };

    // 2. ÁREA VIP: Rotas do Front-end (HTML, CSS, JS)
    private static final String[] ROTAS_PUBLICAS_FRONTEND = {
            "/home",
            "/registrar",
            "/login",
            "/mfa-setup",
            "/painel",
            "/dashboard",
            "/novo-evento",
            "/esqueci-senha",
            "/redefinir-senha",
            "/js/**",
            "/css/**",
            "/assets/**",
            "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desliga a proteção CSRF, pois usamos tokens JWT (Stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Define que a API não guardará sessão no servidor
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configuração das catracas de acesso
                .authorizeHttpRequests(auth -> auth
                        // Permite que o Spring redirecione erros internamente sem bloquear
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()

                        // Libera os blocos de rotas que definimos lá em cima
                        .requestMatchers(ROTAS_PUBLICAS_GERAIS).permitAll()
                        .requestMatchers(ROTAS_PUBLICAS_FRONTEND).permitAll()

                        // Libera endpoints específicos via GET (como o link público do evento)
                        .requestMatchers(HttpMethod.GET, "/eventos/publico/**").permitAll()

                        // A REGRA FINAL: Qualquer coisa que não estiver nas listas acima, exige Token
                        .anyRequest().authenticated()
                )

                // Encaixa o seu verificador de Token antes do verificador padrão do Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}