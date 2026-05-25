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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] ROTAS_PUBLICAS_GERAIS = {
            "/auth/**",
            "/auth/login",
            "/auth/register",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/error"
    };

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

                // ---------------------------------------------------------------------
                // AUDITORIA: REQUISITO 3.1 e 3.2 - FORÇAR COMUNICAÇÃO SEGURA (TLS/HTTPS)
                // Bloqueia qualquer tráfego HTTP em texto plano. O Spring Security
                // abortará ou redirecionará a requisição para a porta segura (8443).
                // ---------------------------------------------------------------------
                .requiresChannel(channel -> channel.anyRequest().requiresSecure())

                // Define que a API não guardará sessão no servidor
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configuração das catracas de acesso
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers(ROTAS_PUBLICAS_GERAIS).permitAll()
                        .requestMatchers(ROTAS_PUBLICAS_FRONTEND).permitAll()
                        .requestMatchers(HttpMethod.GET, "/eventos/publico/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Encaixa o seu verificador de Token antes do verificador padrão do Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}