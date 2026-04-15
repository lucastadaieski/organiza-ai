package com.organizaai.service;

import com.organizaai.model.TokenBlacklist;
import com.organizaai.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository repository;

    public void invalidarToken(String token, Date expiracaoJwt) {
        LocalDateTime dataExpiracao = expiracaoJwt.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        TokenBlacklist blacklist = new TokenBlacklist();
        blacklist.setToken(token);
        blacklist.setDataExpiracao(dataExpiracao);

        repository.save(blacklist);
    }
}
