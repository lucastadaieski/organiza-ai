package com.organizaai.config;

import com.organizaai.repository.TokenBlacklistRepository;
import com.organizaai.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistRepository blacklistRepository;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        if ((path.startsWith("/auth") && !path.equals("/auth/logout")) || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // 2. Verifica se o cabeçalho Authorization existe e tem o formato correto (Bearer)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai apenas o token, tirando os 7 primeiros caracteres ("Bearer ")
        final String jwt = authHeader.substring(7);

        // 3. Checa a Blacklist (Logout)
        if (blacklistRepository.existsByToken(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // MUDANÇA: É boa prática retornar JSON para que o front-end consiga ler o erro com facilidade.
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"erro\": \"Sessão encerrada. Faça login novamente.\"}");
            return;
        }

        // 4. Extrai o e-mail do Token
        final String userEmail = jwtService.extrairEmail(jwt);

        // 5. Valida o Token e Autentica o usuário no Spring Security
        // Verifica se conseguiu extrair o e-mail e se o usuário AINDA NÃO está autenticado neste request
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // CORREÇÃO CRÍTICA: Carrega o usuário do banco para pegar suas Roles (ex: ADMIN, USER)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValido(jwt)) {
                // Cria o token de autenticação passando as Roles (userDetails.getAuthorities())
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Adiciona detalhes adicionais da requisição (como IP do usuário, sessão, etc)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Salva a autenticação no contexto de segurança do Spring
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Continua o fluxo da requisição para o Controller
        filterChain.doFilter(request, response);
    }
}