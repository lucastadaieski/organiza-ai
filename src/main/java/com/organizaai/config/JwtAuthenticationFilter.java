package com.organizaai.config;

import com.organizaai.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        if (request.getServletPath().contains("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Pega o cabeçalho "Authorization"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Verifica se o token existe e começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrai o token (pula os 7 caracteres de "Bearer ")
        jwt = authHeader.substring(7);

        // Aqui vamos extrair o e-mail usando o JwtService que você já criou
        userEmail = jwtService.extrairEmail(jwt);

        // 4. Se o e-mail for válido e o usuário não estiver autenticado ainda
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Se o token for válido, "avisamos" o Spring que o usuário está OK
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userEmail, null, Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // 5. Deixa a requisição seguir viagem
        filterChain.doFilter(request, response);
    }
}
